package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;

import com.fasterxml.jackson.databind.JsonNode;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Aplica o template Velocity do documento articulado, produzindo o XSL-FO do PDF. Sem classes de domínio: os dados
 * que o template usa (metadados
 * do PDF, cabeçalho, alertas, local e data e assinaturas) são lidos do documento-articulado.json aqui e passados como valores simples.
 */
class DocumentoArticuladoTemplateProcessor {

    static final String TEMPLATE = "/documentoarticulado/template-velocity-documento-articulado.xml";

    private static final String APLICACAO_PADRAO = "LexEdit";
    private static final ZoneOffset FUSO_BRASILIA = ZoneOffset.ofHours(-3);
    static final String BRASAO = "/static/img/brasao.jpg";

    /** Brasão da República em base64, lido do classpath na primeira vez em que é usado. */
    private static String brasao;

    /**
     * @param documento documento-articulado.json
     * @param parametros tamanhos de fonte e espaçamentos
     * @param conteudoFo fragmento XSL-FO do conteúdo impresso, inserido no {@code fo:flow} sem ser
     *        reinterpretado pelo Velocity ({@code $} e {@code #} do texto saem literalmente)
     */
    String processar(JsonNode documento, ParametrosImpressaoDocumentoArticulado parametros, String conteudoFo) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("titulo", StringEscapeUtils.escapeXml10(titulo(documento)));
        ctx.put("aplicacao", StringEscapeUtils.escapeXml10(aplicacao(documento)));
        ctx.put("dataIso", OffsetDateTime.now(FUSO_BRASILIA).truncatedTo(ChronoUnit.SECONDS).toString());
        ctx.put("tamanhoFonte", parametros.getTamanhoFonte());
        ctx.put("conteudoFo", conteudoFo);

        // Cabeçalho da primeira página (issue #74): opções de impressão do documento, com os padrões do editor
        OpcoesImpressaoDocumentoArticulado opcoes = OpcoesImpressaoDocumentoArticulado.de(documento);
        boolean imprimirBrasao = opcoes.isImprimirBrasao();
        String textoCabecalho = opcoes.getTextoCabecalho();
        ctx.put("imprimirBrasao", imprimirBrasao);
        ctx.put("brasao", imprimirBrasao ? brasao() : "");
        ctx.put("casaLegislativa", casaLegislativa(documento.path("value").path("metadado").path("identificacao").path("urn").asText("")));
        ctx.put("textoCabecalho", StringEscapeUtils.escapeXml10(textoCabecalho));
        ctx.put("temCabecalho", imprimirBrasao || !textoCabecalho.isEmpty());

        // Alertas e pendências (issue #79), antes do conteúdo
        AlertasDocumentoArticulado alertas = AlertasDocumentoArticulado.de(documento);
        ctx.put("possuiRevisoes", alertas.isPossuiRevisoes());
        ctx.put("possuiComentarios", alertas.isPossuiComentarios());
        List<String> pendencias = new ArrayList<>();
        for (String pendencia : alertas.getPendencias()) {
            pendencias.add(StringEscapeUtils.escapeXml10(pendencia));
        }
        ctx.put("pendencias", pendencias);

        // Local e data e assinaturas (issue #76), depois do conteúdo
        FechoDocumentoArticulado fecho = FechoDocumentoArticulado.de(documento);
        ctx.put("local", StringEscapeUtils.escapeXml10(fecho.getLocal()));
        ctx.put("dataPorExtenso", StringEscapeUtils.escapeXml10(fecho.getData()));
        ctx.put("espacoAntesFecho", parametros.getEspacoAntesFecho());
        ctx.put("assinaturas", paresDeAssinaturas(fecho.getAssinaturas()));

        StringWriter resultado = new StringWriter();
        criarVelocityEngine().evaluate(ctx, resultado, "documentoArticulado", carregarTemplate());
        return resultado.toString();
    }

    /**
     * Assinaturas em pares, uma linha da tabela de duas colunas por par (a última sozinha quando o número é ímpar),
     * Mapas, e não as próprias assinaturas, porque o Velocity
     * só chama métodos públicos de classes públicas; os textos vão escapados para XML.
     */
    static List<List<Map<String, String>>> paresDeAssinaturas(List<FechoDocumentoArticulado.Assinatura> assinaturas) {
        List<List<Map<String, String>>> pares = new ArrayList<>();
        for (int i = 0; i < assinaturas.size(); i += 2) {
            List<Map<String, String>> par = new ArrayList<>();
            for (FechoDocumentoArticulado.Assinatura assinatura : assinaturas.subList(i, Math.min(i + 2, assinaturas.size()))) {
                par.add(assinatura(assinatura));
            }
            pares.add(par);
        }
        return pares;
    }

    private static Map<String, String> assinatura(FechoDocumentoArticulado.Assinatura assinatura) {
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("nome", StringEscapeUtils.escapeXml10(assinatura.getNome()));
        valores.put("partidoUF", StringEscapeUtils.escapeXml10(assinatura.getPartidoUF()));
        valores.put("cargo", StringEscapeUtils.escapeXml10(assinatura.getCargo()));
        return valores;
    }

    /** Texto da epígrafe com espaços normalizados ou, na falta dela, a URN de identificação. */
    static String titulo(JsonNode documento) {
        JsonNode epigrafe = documento.path("value").path("projetoNorma").path("norma").path("parteInicial").path("epigrafe");
        StringBuilder texto = new StringBuilder();
        acumularTexto(epigrafe.path("content"), texto);
        String titulo = texto.toString().replaceAll("\\s+", " ").trim();
        return titulo.isEmpty() ? documento.path("value").path("metadado").path("identificacao").path("urn").asText() : titulo;
    }

    /** Concatena os textos de um {@code content} jsonix, incluindo os de elementos inline aninhados. */
    private static void acumularTexto(JsonNode content, StringBuilder texto) {
        for (JsonNode item : content) {
            if (item.isTextual()) {
                texto.append(item.asText());
            } else {
                acumularTexto(item.path("value").path("content"), texto);
            }
        }
    }

    /** Aplicação registrada em {@code lexedit:Metadado}, se houver. */
    private static String aplicacao(JsonNode documento) {
        for (JsonNode proprietario : documento.path("value").path("metadado").path("metadadoProprietario")) {
            for (JsonNode metadado : proprietario.path("any")) {
                String aplicacao = metadado.path("value").path("aplicacao").asText("");
                if (!aplicacao.trim().isEmpty()) {
                    return aplicacao.trim();
                }
            }
        }
        return APLICACAO_PADRAO;
    }

    /**
     * Nome da casa legislativa pela autoridade da URN ({@code urn:lex:br:<autoridade>:...}); vazio quando a
     * autoridade não é uma das casas (ex.: {@code federal}, das medidas provisórias).
     */
    static String casaLegislativa(String urn) {
        String[] partes = urn.trim().split(":");
        String autoridade = partes.length > 3 ? partes[3] : "";
        switch (autoridade) {
            case "senado.federal":
                return "SENADO FEDERAL";
            case "camara.deputados":
                return "CÂMARA DOS DEPUTADOS";
            case "congresso.nacional":
                return "CONGRESSO NACIONAL";
            default:
                return "";
        }
    }

    private static synchronized String brasao() {
        if (brasao == null) {
            brasao = carregarBrasao();
        }
        return brasao;
    }

    /** Brasão de {@code /static/img/}. Faltar no classpath é erro de empacotamento. */
    private static String carregarBrasao() {
        try (InputStream in = DocumentoArticuladoTemplateProcessor.class.getResourceAsStream(BRASAO)) {
            if (in == null) {
                throw new EtaBackendException("Imagem do brasão não encontrada: " + BRASAO);
            }
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));
        } catch (IOException e) {
            throw new EtaBackendException("Não foi possível ler a imagem do brasão " + BRASAO, e);
        }
    }

    private static String carregarTemplate() {
        try (InputStream in = DocumentoArticuladoTemplateProcessor.class.getResourceAsStream(TEMPLATE)) {
            if (in == null) {
                throw new EtaBackendException("Template não encontrado: " + TEMPLATE);
            }
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EtaBackendException("Não foi possível ler o template " + TEMPLATE, e);
        }
    }

    private VelocityEngine criarVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        // Força o Velocity a usar o SLF4J (que o Logback implementa)
        ve.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME, getClass().getName());
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, CommonsLogLogChute.class.getName());
        ve.init();
        return ve;
    }

}
