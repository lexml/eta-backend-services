package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class DocumentoArticuladoTemplateProcessorTest {

    private final DocumentoArticuladoTemplateProcessor processor = new DocumentoArticuladoTemplateProcessor();

    private ObjectNode documento;

    @BeforeEach
    void setUp() throws Exception {
        documento = (ObjectNode) new ObjectMapper().readTree(recurso(JSON_EXEMPLO));
    }

    @Test
    void geraXslFoBemFormadoComMetadadosPdfa() throws Exception {
        String fo = processor.processar(documento);

        Document xml = new SAXReader().read(new StringReader(fo));
        assertThat(xml.getRootElement().getName()).isEqualTo("root");
        assertThat(fo)
                .contains("<pdfaid:part>3</pdfaid:part>")
                .contains("<pdfaid:conformance>B</pdfaid:conformance>")
                .contains("<check:hash>00000000000000000000000000000000</check:hash>")
                .contains("<xmp:CreatorTool>LexEdit</xmp:CreatorTool>")
                .contains("<fo:block/>");
        assertThat(fo).doesNotContain("$titulo").doesNotContain("$dataIso").doesNotContain("$aplicacao");
    }

    @Test
    void tituloPelaEpigrafe() {
        String fo = processor.processar(documento);

        assertThat(fo).contains("<rdf:li xml:lang=\"x-default\">PROJETO DE LEI Nº , DE</rdf:li>");
    }

    @Test
    void tituloPelaUrnSemEpigrafe() {
        parteInicial().remove("epigrafe");

        String fo = processor.processar(documento);

        assertThat(fo).contains("<rdf:li xml:lang=\"x-default\">urn:lex:br:senado.federal:projeto.lei:999999;9999</rdf:li>");
    }

    @Test
    void tituloComCaracteresEspeciaisEhEscapado() throws Exception {
        ArrayNode content = ((ObjectNode) parteInicial().path("epigrafe")).putArray("content");
        content.add("PROJETO DE LEI \"A\" & <B>");

        String fo = processor.processar(documento);

        new SAXReader().read(new StringReader(fo));
        assertThat(fo).contains("PROJETO DE LEI &quot;A&quot; &amp; &lt;B&gt;");
    }

    @Test
    void naoContemTextoDosDispositivos() {
        String fo = processor.processar(documento);

        assertThat(fo)
                .doesNotContain("Fica instituído")
                .doesNotContain("Esta Lei entra em vigor")
                .doesNotContain("Institui o Programa");
    }

    private ObjectNode parteInicial() {
        JsonNode norma = documento.path("value").path("projetoNorma").path("norma");
        return (ObjectNode) norma.path("parteInicial");
    }

}
