package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;

@EnabledIfSystemProperty(named = "jsonix-lexml.cli", matches = ".+")
class DocumentoArticuladoParserIntegracaoCliTest {

    private static final Pattern REGEX_URN_VALIDA = Pattern.compile(
            "^urn:lex:br:[^:\\s]+:[^:\\s]+:\\d{4}(?:-\\d{2}-\\d{2})?;\\d+(?:@[^!\\s]+)?$");

    private final ObjectMapper mapper = new ObjectMapper();

    private DocumentoArticuladoParser criarParser(ConversorDocumentoArticulado conversor) {
        return new DocumentoArticuladoParser(conversor);
    }

    private ConversorDocumentoArticulado criarConversor() {
        String cli = System.getProperty("jsonix-lexml.cli");
        return new ConversorDocumentoArticuladoCli(Paths.get(cli), Duration.ofSeconds(30));
    }

    @Test
    void deveConverterDocxParaJsonixValidoNoLexmlEta() throws IOException {
        DocumentoArticuladoParser parser = criarParser(criarConversor());
        byte[] docx = DocxTesteBuilder.comParagrafos(
                "PROJETO DE LEI Nº 456, DE 2024",
                "Institui a política de dados abertos no âmbito federal.",
                "O CONGRESSO NACIONAL decreta:",
                "Art. 1º Fica instituída a política nacional de dados abertos.",
                "Parágrafo único. Os dados serão disponibilizados em formato estruturado.",
                "Art. 2º Esta Lei entra em vigor na data de sua publicação.",
                "Justificação",
                "Texto justificativo.");

        ResultadoParseProposicao resultado = parser.parseDocx(new ByteArrayInputStream(docx));

        JsonNode root = assertInvariantesJsonix(resultado.getJsonix());
        assertThat(root.path("value").path("projetoNorma").path("norma").path("parteInicial")
                .path("ementa").toString())
                .contains("política de dados abertos");
        assertThat(resultado.temAvisos()).isFalse();
    }

    @Test
    void deveConverterTxtParaJsonixPreservandoAvisosNaoFatais() throws IOException {
        DocumentoArticuladoParser parser = criarParser(criarConversor());
        String txt = String.join("\n",
                "Dispõe sobre diretrizes para auditoria de sistemas públicos.",
                "Art. 1º Os sistemas deverão manter trilha de auditoria.",
                "Art. 3º Artigo com descontinuidade intencional.");

        ResultadoParseProposicao resultado = parser.parseTxt(txt);

        assertInvariantesJsonix(resultado.getJsonix());
        assertThat(resultado.temAvisos()).isTrue();
    }

    @Test
    void deveConverterXmlLexmlParaJsonixNormalizandoUrnPreservandoMetadadoProprietario() throws IOException {
        DocumentoArticuladoParser parser = criarParser(criarConversor());
        String xmlExemplo = lerRecurso("/documentoarticulado/documento-articulado-exemplo.xml");

        ResultadoParseProposicao resultado = parser.parseXml(xmlExemplo);

        JsonNode root = assertInvariantesJsonix(resultado.getJsonix());
        assertThat(root.path("value").path("metadado").path("metadadoProprietario")).isNotEmpty();
    }

    @Test
    void deveConverterPdfGeradoPelaBibliotecaParaJsonix() throws IOException {
        ConversorDocumentoArticulado conversor = criarConversor();
        DocumentoArticuladoPdfGenerator gerador = new DocumentoArticuladoPdfGenerator(conversor);
        String jsonExemplo = lerRecurso("/documentoarticulado/documento-articulado-exemplo.json");

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        gerador.generate(jsonExemplo, pdfOut);

        DocumentoArticuladoParser parser = criarParser(conversor);
        ResultadoParseProposicao resultado = parser.parsePdf(new ByteArrayInputStream(pdfOut.toByteArray()));

        JsonNode root = mapper.readTree(resultado.getJsonix());
        assertThat(root.path("name").path("namespaceURI").asText()).isEqualTo("http://www.lexml.gov.br/1.0");
        assertThat(root.path("name").path("localPart").asText()).isEqualTo("LexML");
        JsonNode lXhier = root.path("value").path("projetoNorma").path("norma").path("articulacao").path("lXhier");
        assertThat(lXhier.isArray()).isTrue();
        assertThat(lXhier).isNotEmpty();
    }

    private JsonNode assertInvariantesJsonix(String jsonix) throws IOException {
        JsonNode root = mapper.readTree(jsonix);
        assertThat(root.path("name").path("namespaceURI").asText()).isEqualTo("http://www.lexml.gov.br/1.0");
        assertThat(root.path("name").path("localPart").asText()).isEqualTo("LexML");
        String urn = root.path("value").path("metadado").path("identificacao").path("urn").asText();
        assertThat(urn).matches(REGEX_URN_VALIDA);
        JsonNode lXhier = root.path("value").path("projetoNorma").path("norma").path("articulacao").path("lXhier");
        assertThat(lXhier.isArray()).isTrue();
        assertThat(lXhier).isNotEmpty();
        return root;
    }

    private static String lerRecurso(String caminho) throws IOException {
        try (InputStream is = DocumentoArticuladoParserIntegracaoCliTest.class.getResourceAsStream(caminho)) {
            assertThat(is).as("Recurso %s", caminho).isNotNull();
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

}

