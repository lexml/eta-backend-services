package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Passo comum aos geradores de PDF e de RTF: valida o JSON, converte para XML e monta o XSL-FO completo
 * (conteúdo pela XSLT e página pelo Velocity).
 */
class DocumentoArticuladoFoGerador {

    static final String NAMESPACE_LEXML = "http://www.lexml.gov.br/1.0";

    /** XSL-FO pronto para o FOP e o documento-articulado.xml de onde ele saiu. */
    static final class FoGerado {
        final String xslFo;
        final String xml;

        FoGerado(String xslFo, String xml) {
            this.xslFo = xslFo;
            this.xml = xml;
        }
    }

    private final ConversorDocumentoArticulado conversor;
    private final DocumentoArticuladoConteudoTransformer conteudoTransformer = new DocumentoArticuladoConteudoTransformer();
    private final DocumentoArticuladoTemplateProcessor templateProcessor = new DocumentoArticuladoTemplateProcessor();
    private final ObjectMapper mapper = new ObjectMapper();

    DocumentoArticuladoFoGerador(ConversorDocumentoArticulado conversor) {
        this.conversor = Objects.requireNonNull(conversor, "conversor");
    }

    /** @throws EtaBackendException se o JSON for inválido ou não for um documento LexML */
    JsonNode validar(String json) {
        if (json == null) {
            throw new EtaBackendException("O documento articulado não foi informado.");
        }
        JsonNode documento;
        try {
            documento = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new EtaBackendException("O documento articulado não é um JSON válido.", e);
        }
        JsonNode nome = documento == null ? null : documento.path("name");
        String urn = documento == null ? "" : documento.path("value").path("metadado").path("identificacao").path("urn").asText("");
        if (nome == null || !NAMESPACE_LEXML.equals(nome.path("namespaceURI").asText())
                || !"LexML".equals(nome.path("localPart").asText()) || urn.trim().isEmpty()) {
            throw new EtaBackendException("O conteúdo não é um documento LexML em JSON (documento-articulado.json).");
        }
        return documento;
    }

    /** @param documento o resultado de {@link #validar(String)} para o mesmo {@code json} */
    FoGerado gerar(String json, JsonNode documento) {
        String xml = conversor.jsonParaXml(json);
        ParametrosImpressaoDocumentoArticulado parametros =
                ParametrosImpressaoDocumentoArticulado.de(OpcoesImpressaoDocumentoArticulado.de(documento));
        // Anexo de parecer (issue #80): sem justificação; o fecho e as assinaturas o FechoDocumentoArticulado já omite
        String conteudoFo = conteudoTransformer.transformar(xml, parametros, !FechoDocumentoArticulado.anexoParecer(documento));
        String xslFo = templateProcessor.processar(documento, parametros, conteudoFo);
        return new FoGerado(xslFo, xml);
    }

}
