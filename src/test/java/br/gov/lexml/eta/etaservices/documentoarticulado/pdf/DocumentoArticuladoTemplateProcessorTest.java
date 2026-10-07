package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class DocumentoArticuladoTemplateProcessorTest {

    private static final String CONTEUDO_FO = "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">Conteúdo gerado</fo:block>";

    private final DocumentoArticuladoTemplateProcessor processor = new DocumentoArticuladoTemplateProcessor();

    private ObjectNode documento;

    @BeforeEach
    void setUp() throws Exception {
        documento = (ObjectNode) new ObjectMapper().readTree(recurso(JSON_EXEMPLO));
    }

    @Test
    void geraXslFoBemFormadoComMetadadosPdfa() throws Exception {
        String fo = processar();

        Document xml = new SAXReader().read(new StringReader(fo));
        assertThat(xml.getRootElement().getName()).isEqualTo("root");
        assertThat(fo)
                .contains("<pdfaid:part>3</pdfaid:part>")
                .contains("<pdfaid:conformance>B</pdfaid:conformance>")
                .contains("<check:hash>00000000000000000000000000000000</check:hash>")
                .contains("<xmp:CreatorTool>LexEdit</xmp:CreatorTool>");
        assertThat(fo).doesNotContain("$titulo").doesNotContain("$dataIso").doesNotContain("$aplicacao");
    }

    @Test
    void insereOConteudoNoFluxoComOTamanhoDeFonte() throws Exception {
        String fo = processar();

        Document xml = new SAXReader().read(new StringReader(fo));
        Element fluxo = (Element) xml.selectSingleNode("//*[local-name()='flow']");
        assertThat(fluxo.attributeValue("font-size")).isEqualTo("14pt");
        assertThat(fluxo.getStringValue()).contains("Conteúdo gerado");
    }

    @Test
    void separadorDasNotasDeRodapeAntesDoFluxo() throws Exception {
        Document xml = new SAXReader().read(new StringReader(processar()));
        Element sequencia = (Element) xml.selectSingleNode("//*[local-name()='page-sequence']");
        java.util.List<Element> filhos = sequencia.elements();

        // Traço de 50% e 0,5pt, como na emenda; o XSL-FO exige o static-content antes do flow
        assertThat(filhos).extracting(Element::getName).containsExactly("static-content", "flow");
        Element separador = filhos.get(0);
        assertThat(separador.attributeValue("flow-name")).isEqualTo("xsl-footnote-separator");
        Element traco = (Element) separador.selectSingleNode(".//*[local-name()='leader']");
        assertThat(traco.attributeValue("leader-pattern")).isEqualTo("rule");
        assertThat(traco.attributeValue("leader-length")).isEqualTo("50%");
        assertThat(traco.attributeValue("rule-thickness")).isEqualTo("0.5pt");
    }

    @Test
    void conteudoNaoEhReinterpretadoPeloVelocity() {
        String conteudo = "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">Custo de $valor #if(x) ## nota</fo:block>";

        String fo = processor.processar(documento, ParametrosImpressaoDocumentoArticulado.padrao(), conteudo);

        assertThat(fo).contains("Custo de $valor #if(x) ## nota");
    }

    @Test
    void tituloPelaEpigrafe() {
        String fo = processar();

        assertThat(fo).contains("<rdf:li xml:lang=\"x-default\">PROJETO DE LEI Nº , DE</rdf:li>");
    }

    @Test
    void tituloPelaUrnSemEpigrafe() {
        parteInicial().remove("epigrafe");

        String fo = processar();

        assertThat(fo).contains("<rdf:li xml:lang=\"x-default\">urn:lex:br:senado.federal:projeto.lei:999999;9999</rdf:li>");
    }

    @Test
    void tituloComCaracteresEspeciaisEhEscapado() throws Exception {
        ArrayNode content = ((ObjectNode) parteInicial().path("epigrafe")).putArray("content");
        content.add("PROJETO DE LEI \"A\" & <B>");

        String fo = processar();

        new SAXReader().read(new StringReader(fo));
        assertThat(fo).contains("PROJETO DE LEI &quot;A&quot; &amp; &lt;B&gt;");
    }

    @Test
    void naoContemTextoDosDispositivos() {
        String fo = processar();

        assertThat(fo)
                .doesNotContain("Fica instituído")
                .doesNotContain("Esta Lei entra em vigor")
                .doesNotContain("Institui o Programa");
    }

    private String processar() {
        return processor.processar(documento, ParametrosImpressaoDocumentoArticulado.padrao(), CONTEUDO_FO);
    }

    private ObjectNode parteInicial() {
        JsonNode norma = documento.path("value").path("projetoNorma").path("norma");
        return (ObjectNode) norma.path("parteInicial");
    }

}
