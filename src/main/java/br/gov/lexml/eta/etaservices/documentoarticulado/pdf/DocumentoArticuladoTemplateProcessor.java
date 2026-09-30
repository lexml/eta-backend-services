package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Aplica o template Velocity do documento articulado, produzindo o XSL-FO do PDF. Derivado de
 * {@code printing.pdf.VelocityTemplateProcessor}, sem classes de domínio: o template recebe o
 * documento-articulado.json como {@code Map}.
 */
class DocumentoArticuladoTemplateProcessor {

    static final String TEMPLATE = "/documentoarticulado/template-velocity-documento-articulado.xml";

    private static final String APLICACAO_PADRAO = "LexEdit";
    private static final ZoneOffset FUSO_BRASILIA = ZoneOffset.ofHours(-3);

    private final ObjectMapper mapper = new ObjectMapper();

    String processar(JsonNode documento) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("documento", mapper.convertValue(documento, new TypeReference<Map<String, Object>>() {}));
        ctx.put("titulo", StringEscapeUtils.escapeXml10(titulo(documento)));
        ctx.put("aplicacao", StringEscapeUtils.escapeXml10(aplicacao(documento)));
        ctx.put("dataIso", OffsetDateTime.now(FUSO_BRASILIA).truncatedTo(ChronoUnit.SECONDS).toString());

        StringWriter resultado = new StringWriter();
        criarVelocityEngine().evaluate(ctx, resultado, "documentoArticulado", carregarTemplate());
        return resultado.toString();
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
