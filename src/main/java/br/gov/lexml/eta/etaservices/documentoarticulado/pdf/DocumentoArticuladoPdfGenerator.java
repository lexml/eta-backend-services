package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.BytesUtil;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Gera o PDF/A-3B de uma proposição a partir do documento-articulado.json, embutindo o
 * documento-articulado.xml (LexML) como anexo. O texto impresso é gerado a partir desse XML pela XSLT
 * do conteúdo: parte inicial (epígrafe, ementa e preâmbulo) e articulação. Justificação, local e data e
 * assinaturas ainda não são impressos.
 */
public class DocumentoArticuladoPdfGenerator {

    static final String NAMESPACE_LEXML = "http://www.lexml.gov.br/1.0";

    private static final byte[] HASH_PLACEHOLDER =
            "<check:hash>00000000000000000000000000000000".getBytes(StandardCharsets.UTF_8);
    private static final int TAMANHO_TAG_HASH = "<check:hash>".getBytes(StandardCharsets.UTF_8).length;

    private final ConversorDocumentoArticulado conversor;
    private final DocumentoArticuladoConteudoTransformer conteudoTransformer = new DocumentoArticuladoConteudoTransformer();
    private final DocumentoArticuladoTemplateProcessor templateProcessor = new DocumentoArticuladoTemplateProcessor();
    private final DocumentoArticuladoFopProcessor fopProcessor = new DocumentoArticuladoFopProcessor();
    private final ObjectMapper mapper = new ObjectMapper();

    public DocumentoArticuladoPdfGenerator(ConversorDocumentoArticulado conversor) {
        this.conversor = Objects.requireNonNull(conversor, "conversor");
    }

    /**
     * @param json conteúdo do documento-articulado.json
     * @param outputStream destino do PDF; nada é escrito se a geração falhar
     * @throws EtaBackendException se o JSON não for um documento LexML ou se a conversão/geração falhar
     * @throws IOException se houver erro ao escrever no destino
     */
    public void generate(String json, OutputStream outputStream) throws IOException {
        JsonNode documento = validar(json);
        String xml = conversor.jsonParaXml(json);
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.padrao();
        String conteudoFo = conteudoTransformer.transformar(xml, parametros);
        String xslFo = templateProcessor.processar(documento, parametros, conteudoFo);
        byte[] pdf = fopProcessor.gerarPdf(xslFo, xml);
        inserirHash(pdf);

        outputStream.write(pdf);
        outputStream.flush();
    }

    private JsonNode validar(String json) {
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

    /** Substitui o placeholder do {@code check:hash} no XMP pelo MD5 do PDF (derivado de PdfGeneratorBean). */
    private static void inserirHash(byte[] pdf) {
        int i = BytesUtil.lastIndexOf(pdf, HASH_PLACEHOLDER);
        if (i >= 0) {
            byte[] md5 = md5Hex(pdf).getBytes(StandardCharsets.UTF_8);
            System.arraycopy(md5, 0, pdf, i + TAMANHO_TAG_HASH, md5.length);
        }
    }

    private static String md5Hex(byte[] bytes) {
        try {
            StringBuilder hex = new StringBuilder();
            for (byte b : MessageDigest.getInstance("MD5").digest(bytes)) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new EtaBackendException("MD5 indisponível.", e);
        }
    }

}
