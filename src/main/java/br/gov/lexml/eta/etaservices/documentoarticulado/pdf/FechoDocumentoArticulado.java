package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Local, data e assinaturas do documento (issue #76), lidos do {@code lexedit:Metadado} do
 * documento-articulado.json (especificações 03 e 04 do lexml-eta). A representação textual do LexML
 * ({@code ParteFinal}) não é usada.
 * <p>
 * Só a autoria de parlamentares ({@code tipo="Parlamentar"}) gera assinaturas; a de comissão, não. Tratamento: casa
 * {@code CD} -> Deputado/Deputada, senão Senador/Senadora; sexo {@code M} -> masculino, senão feminino.
 * <p>
 * Em anexo de parecer ({@code anexoParecer} verdadeiro, issue #80), o fecho é o mesmo de qualquer documento: só a
 * justificação deixa de ser impressa ({@link #anexoParecer(JsonNode)}, usado pelo gerador).
 */
final class FechoDocumentoArticulado {

    private static final String NAMESPACE_LEXEDIT = "http://www.lexml.gov.br/lexedit/1.0";

    /** Data por extenso em português, com localidade fixa (não depende da localidade padrão da JVM). */
    private static final DateTimeFormatter DATA_POR_EXTENSO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

    private final String local;
    private final String data;
    private final List<Assinatura> assinaturas;

    private FechoDocumentoArticulado(String local, String data, List<Assinatura> assinaturas) {
        this.local = local;
        this.data = data;
        this.assinaturas = Collections.unmodifiableList(assinaturas);
    }

    /** Local, data e assinaturas do documento; vazios quando o documento não os tem. */
    static FechoDocumentoArticulado de(JsonNode documento) {
        JsonNode metadado = metadadoLexedit(documento);
        return new FechoDocumentoArticulado(texto(metadado.path("local")), dataPorExtenso(texto(metadado.path("data"))),
                assinaturas(metadado.path("autoria")));
    }

    /**
     * Valor do {@code lexedit:Metadado}, localizado pelo nome e namespace do elemento; nó ausente quando o documento
     * não o tem. Também usado por {@link AlertasDocumentoArticulado}.
     */
    static JsonNode metadadoLexedit(JsonNode documento) {
        for (JsonNode proprietario : documento.path("value").path("metadado").path("metadadoProprietario")) {
            for (JsonNode metadado : proprietario.path("any")) {
                JsonNode nome = metadado.path("name");
                if (NAMESPACE_LEXEDIT.equals(nome.path("namespaceURI").asText()) && "Metadado".equals(nome.path("localPart").asText())) {
                    return metadado.path("value");
                }
            }
        }
        return documento.path("ausente");
    }

    /**
     * Se o documento é anexo de parecer ({@code lexedit:Metadado/@anexoParecer}, especificação 13 do lexml-eta): só o
     * booleano {@code true}; ausente (que indica falso, pela issue #80), falso ou de outro tipo vale falso.
     */
    static boolean anexoParecer(JsonNode documento) {
        JsonNode anexoParecer = metadadoLexedit(documento).path("anexoParecer");
        return anexoParecer.isBoolean() && anexoParecer.booleanValue();
    }

    private static String texto(JsonNode no) {
        return no.isTextual() ? no.textValue().trim() : "";
    }

    /** "24 de abril de 2026"; vazio quando a data falta ou não está no formato aaaa-mm-dd. */
    private static String dataPorExtenso(String data) {
        try {
            return data.isEmpty() ? "" : LocalDate.parse(data).format(DATA_POR_EXTENSO);
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    private static List<Assinatura> assinaturas(JsonNode autoria) {
        List<Assinatura> assinaturas = new ArrayList<>();
        if (!"Parlamentar".equals(autoria.path("tipo").asText())) {
            return assinaturas;
        }
        boolean imprimirPartidoUF = autoria.path("imprimirPartidoUF").asBoolean(false);
        JsonNode parlamentares = autoria.path("parlamentares").path("parlamentar");
        if (!parlamentares.isArray()) {
            return assinaturas;
        }
        for (JsonNode parlamentar : parlamentares) {
            String nome = texto(parlamentar.path("nome"));
            if (nome.isEmpty()) {
                continue;
            }
            String partido = texto(parlamentar.path("siglaPartido"));
            String uf = texto(parlamentar.path("siglaUF"));
            assinaturas.add(new Assinatura(tratamento(parlamentar) + " " + nome,
                    imprimirPartidoUF && !(partido.isEmpty() && uf.isEmpty()) ? "(" + partido + " - " + uf + ")" : "",
                    texto(parlamentar.path("cargo"))));
        }
        return assinaturas;
    }

    private static String tratamento(JsonNode parlamentar) {
        boolean masculino = "M".equals(texto(parlamentar.path("sexo")));
        if ("CD".equals(texto(parlamentar.path("siglaCasaLegislativa")))) {
            return masculino ? "Deputado" : "Deputada";
        }
        return masculino ? "Senador" : "Senadora";
    }

    /** Local do fecho, sem espaços nas pontas; vazio quando não informado (sem fecho). */
    String getLocal() {
        return local;
    }

    /** Data por extenso; vazia quando ausente, vazia ou inválida (o fecho sai com espaços para preencher). */
    String getData() {
        return data;
    }

    /** Assinaturas dos parlamentares, na ordem do documento; vazia sem autoria de parlamentares. */
    List<Assinatura> getAssinaturas() {
        return assinaturas;
    }

    /** Assinatura de um parlamentar, com os textos impressos (sem escape para XML). */
    static final class Assinatura {

        private final String nome;
        private final String partidoUF;
        private final String cargo;

        Assinatura(String nome, String partidoUF, String cargo) {
            this.nome = nome;
            this.partidoUF = partidoUF;
            this.cargo = cargo;
        }

        /** Tratamento e nome, ex.: "Senador Davi Alcolumbre". */
        String getNome() {
            return nome;
        }

        /** "(PARTIDO - UF)", ou vazio quando não deve ser impresso. */
        String getPartidoUF() {
            return partidoUF;
        }

        /** Cargo, ou vazio. */
        String getCargo() {
            return cargo;
        }

    }

}
