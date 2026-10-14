package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

        // Traço de 50% e 0,5pt, como na emenda; o XSL-FO exige os static-content (cabeçalho e separador) antes do flow
        assertThat(filhos).extracting(Element::getName).containsExactly("static-content", "static-content", "flow");
        Element separador = (Element) sequencia.selectSingleNode("*[@flow-name='xsl-footnote-separator']");
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

    // Cabeçalho (issue #74) ---------------------------------------------------------------------------------------

    @Test
    void cabecalhoComBrasaoCasaETextoNaPrimeiraPagina() throws Exception {
        Document xml = new SAXReader().read(new StringReader(processar()));

        // Primeira página: region-before de 3cm e corpo a 4cm (valores da emenda); demais páginas sem cabeçalho
        Element primeira = (Element) xml.selectSingleNode("//*[local-name()='simple-page-master'][@master-name='firstPage']");
        assertThat(atributo(primeira, "region-body", "margin-top")).isEqualTo("4cm");
        assertThat(atributo(primeira, "region-before", "extent")).isEqualTo("3cm");
        assertThat(atributo(primeira, "region-before", "region-name")).isEqualTo("header-principal");
        Element demais = (Element) xml.selectSingleNode("//*[local-name()='simple-page-master'][@master-name='restPage']");
        assertThat(demais.selectNodes("*[local-name()='region-before']")).isEmpty();

        Element cabecalho = cabecalho(xml);
        Element brasao = (Element) cabecalho.selectSingleNode(".//*[local-name()='external-graphic']");
        assertThat(brasao.attributeValue("src")).startsWith("url('data:image/jpeg;base64,/9j/").endsWith("')");
        Element blocoDoBrasao = (Element) brasao.getParent();
        assertThat(blocoDoBrasao.attributeValue("text-align")).isEqualTo("center");
        assertThat(blocoDoBrasao.attributeValue("margin-bottom")).isEqualTo("2pt");
        Element casa = (Element) blocoDoBrasao.selectSingleNode("*[local-name()='block']");
        assertThat(casa.getText()).isEqualTo("SENADO FEDERAL");
        assertThat(casa.attributeValue("font-family")).isEqualTo("ITC-StoneSansStd");
        assertThat(casa.attributeValue("font-size")).isEqualTo("11pt");
        assertThat(casa.attributeValue("margin-top")).isEqualTo("3pt");
        // Texto do cabeçalho centralizado, depois do brasão
        Element texto = (Element) cabecalho.elements().get(1);
        assertThat(texto.getText()).isEqualTo("Gabinete do Senador Davi Alcolumbre");
        assertThat(texto.attributeValue("text-align")).isEqualTo("center");
    }

    @ParameterizedTest
    @CsvSource({ "urn:lex:br:senado.federal:projeto.lei:2026;1,SENADO FEDERAL",
            "urn:lex:br:camara.deputados:projeto.lei:2026;1,CÂMARA DOS DEPUTADOS",
            "urn:lex:br:congresso.nacional:projeto.decreto.legislativo:2026;1,CONGRESSO NACIONAL",
            "urn:lex:br:federal:medida.provisoria:2026;999,", "'',", "urn:lex:br,", "urn:lex:br:senado.federal,SENADO FEDERAL" })
    void casaLegislativaPelaAutoridadeDaUrn(String urn, String casa) {
        assertThat(DocumentoArticuladoTemplateProcessor.casaLegislativa(urn)).isEqualTo(casa == null ? "" : casa);
    }

    @Test
    void autoridadeSemCasaImprimeSoOBrasao() throws Exception {
        identificacao().put("urn", "urn:lex:br:federal:medida.provisoria:2026;999");

        Element cabecalho = cabecalho(new SAXReader().read(new StringReader(processar())));
        Element blocoDoBrasao = (Element) cabecalho.elements().get(0);
        assertThat(blocoDoBrasao.selectNodes(".//*[local-name()='external-graphic']")).hasSize(1);
        assertThat(blocoDoBrasao.selectNodes("*[local-name()='block']")).isEmpty();
    }

    @Test
    void soOTextoSemBrasao() throws Exception {
        opcoesImpressao().put("imprimirBrasao", false);

        Element cabecalho = cabecalho(new SAXReader().read(new StringReader(processar())));
        assertThat(cabecalho.selectNodes(".//*[local-name()='external-graphic']")).isEmpty();
        assertThat(cabecalho.elements()).extracting(Element::getText).containsExactly("Gabinete do Senador Davi Alcolumbre");
        assertThat(cabecalho.getStringValue()).doesNotContain("SENADO FEDERAL");
    }

    @Test
    void semBrasaoESemTextoNaoHaCabecalho() throws Exception {
        opcoesImpressao().put("imprimirBrasao", false).put("textoCabecalho", "  ");

        Document xml = new SAXReader().read(new StringReader(processar()));
        Element primeira = (Element) xml.selectSingleNode("//*[local-name()='simple-page-master'][@master-name='firstPage']");
        assertThat(atributo(primeira, "region-body", "margin-top")).isEqualTo("1cm");
        assertThat(primeira.selectNodes("*[local-name()='region-before']")).isEmpty();
        assertThat(xml.selectNodes("//*[@flow-name='header-principal']")).isEmpty();
    }

    @Test
    void textoDoCabecalhoEscapado() throws Exception {
        opcoesImpressao().put("textoCabecalho", "Gabinete \"A\" & <B>");

        String fo = processar();
        new SAXReader().read(new StringReader(fo));
        assertThat(fo).contains("Gabinete &quot;A&quot; &amp; &lt;B&gt;");
    }

    @Test
    void opcoesAusentesValemOPadraoDoEditor() throws Exception {
        // Sem OpcoesImpressao: com brasão e sem texto
        ((ObjectNode) metadadoLexedit()).remove("opcoesImpressao");

        Element cabecalho = cabecalho(new SAXReader().read(new StringReader(processar())));
        assertThat(cabecalho.selectNodes(".//*[local-name()='external-graphic']")).hasSize(1);
        assertThat(cabecalho.elements()).hasSize(1);
        assertThat(cabecalho.getStringValue().trim()).isEqualTo("SENADO FEDERAL");
    }

    @Test
    void atributosAusentesValemOPadraoDoEditor() throws Exception {
        opcoesImpressao().remove("imprimirBrasao");
        opcoesImpressao().remove("textoCabecalho");

        Element cabecalho = cabecalho(new SAXReader().read(new StringReader(processar())));
        assertThat(cabecalho.selectNodes(".//*[local-name()='external-graphic']")).hasSize(1);
        assertThat(cabecalho.elements()).hasSize(1);
    }

    // Local e data e assinaturas (issue #76) ----------------------------------------------------------------------

    @Test
    void fechoDepoisDoConteudoComOsValoresDaEmenda() throws Exception {
        Document xml = new SAXReader().read(new StringReader(processar()));
        Element fluxo = (Element) xml.selectSingleNode("//*[local-name()='flow']");

        // Depois do fragmento do conteúdo, um block-container com o fecho e as assinaturas
        assertThat(((Element) fluxo.elements().get(0)).getText()).isEqualTo("Conteúdo gerado");
        Element container = (Element) fluxo.elements().get(1);
        assertThat(container.getName()).isEqualTo("block-container");
        Element fecho = fecho(xml);
        assertThat(fecho.getParent()).isSameAs(container);
        assertThat(fecho.getText()).isEqualTo("Sala das Sessões, 24 de abril de 2026.");
        assertThat(fecho.attributeValue("space-before")).isEqualTo("34pt");
        assertThat(fecho.attributeValue("text-indent")).isEqualTo("2.5cm");
        assertThat(fecho.attributeValue("keep-with-next.within-page")).isEqualTo("always");
        assertThat(fecho.attributeValue("keep-with-previous")).isEqualTo("10");
    }

    @Test
    void espacoAntesDoFechoComReduzir() throws Exception {
        opcoesImpressao().put("reduzirEspacoEntreLinhas", true);
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.de(OpcoesImpressaoDocumentoArticulado.de(documento));

        Document xml = new SAXReader().read(new StringReader(processor.processar(documento, parametros, CONTEUDO_FO)));
        assertThat(fecho(xml).attributeValue("space-before")).isEqualTo("26pt");
    }

    @Test
    void dataVaziaOuAusenteComEspacosParaPreencher() throws Exception {
        String esperado = "Sala das Sessões, " + espacos(8) + " de " + espacos(40) + " de " + espacos(16) + ".";

        metadadoLexedit().put("data", "");
        assertThat(fecho(new SAXReader().read(new StringReader(processar()))).getText()).isEqualTo(esperado);
        metadadoLexedit().remove("data");
        assertThat(fecho(new SAXReader().read(new StringReader(processar()))).getText()).isEqualTo(esperado);
    }

    @Test
    void assinaturasEmDuasColunasComAUltimaSozinha() throws Exception {
        parlamentares().removeAll().add(parlamentar("Davi Alcolumbre", "M", "UNIÃO", "AP", "SF", "Presidente do Senado Federal"))
                .add(parlamentar("Soraya Thronicke", "F", "PSB", "MS", "SF", ""))
                .add(parlamentar("Maria Souza", "F", "PL", "RJ", "CD", ""));

        Element tabela = (Element) new SAXReader().read(new StringReader(processar()))
                .selectSingleNode("//*[local-name()='table'][@role='Signatários']");
        assertThat(tabela.getParent().attributeValue("font-weight")).isEqualTo("bold");
        assertThat(tabela.selectNodes("*[local-name()='table-column']")).extracting(c -> ((Element) c).attributeValue("column-width"))
                .containsExactly("proportional-column-width(50)", "proportional-column-width(50)");
        List<Node> linhas = tabela.selectNodes(".//*[local-name()='table-row']");
        assertThat(linhas).hasSize(2);
        List<Node> primeira = ((Element) linhas.get(0)).selectNodes("*[local-name()='table-cell']");
        assertThat(primeira).hasSize(2).allSatisfy(c -> {
            assertThat(((Element) c).attributeValue("text-align")).isEqualTo("center");
            assertThat(((Element) c).attributeValue("number-columns-spanned")).isNull();
        });
        assertThat(blocos((Element) primeira.get(0))).containsExactly("Senador Davi Alcolumbre", "(UNIÃO - AP)", "Presidente do Senado Federal");
        assertThat(blocos((Element) primeira.get(1))).containsExactly("Senadora Soraya Thronicke", "(PSB - MS)");
        assertThat(((Element) ((Element) primeira.get(0)).elements().get(0)).attributeValue("margin-top")).isEqualTo("30pt");
        Element ultima = (Element) ((Element) linhas.get(1)).selectSingleNode("*[local-name()='table-cell']");
        assertThat(ultima.attributeValue("number-columns-spanned")).isEqualTo("2");
        assertThat(blocos(ultima)).containsExactly("Deputada Maria Souza", "(PL - RJ)");
    }

    @Test
    void semPartidoEUfQuandoNaoImprimir() throws Exception {
        ((ObjectNode) metadadoLexedit().path("autoria")).put("imprimirPartidoUF", false);

        assertThat(processar()).contains("Senador Davi Alcolumbre").doesNotContain("(UNIÃO - AP)").doesNotContain("(PSB - MS)");
    }

    @Test
    void semLocalNemAutoriaNadaImpresso() throws Exception {
        metadadoLexedit().remove("local");
        metadadoLexedit().remove("autoria");

        String fo = processar();
        new SAXReader().read(new StringReader(fo));
        assertThat(fo).doesNotContain("Sala das Sessões").doesNotContain("Signatários").doesNotContain("role=\"fecho\"")
                .doesNotContain("block-container");
    }

    @Test
    void autoriaPorComissaoSoOFecho() throws Exception {
        ObjectNode autoria = metadadoLexedit().putObject("autoria");
        autoria.put("tipo", "Comissão").putObject("colegiadoAutor").put("nome", "Comissão de Assuntos Econômicos").put("sigla", "CAE");

        String fo = processar();
        assertThat(fecho(new SAXReader().read(new StringReader(fo))).getText()).isEqualTo("Sala das Sessões, 24 de abril de 2026.");
        assertThat(fo).doesNotContain("Signatários").doesNotContain("Comissão de Assuntos Econômicos");
    }

    @Test
    void localENomesEscapados() throws Exception {
        metadadoLexedit().put("local", "Sala \"A\" & <B>");
        parlamentares().removeAll().add(parlamentar("Fulano & <Cia>", "M", "P&P", "UF", "SF", "Líder \"X\""));

        Document xml = new SAXReader().read(new StringReader(processar()));
        assertThat(fecho(xml).getText()).isEqualTo("Sala \"A\" & <B>, 24 de abril de 2026.");
        assertThat(xml.getRootElement().getStringValue()).contains("Senador Fulano & <Cia>").contains("(P&P - UF)").contains("Líder \"X\"");
    }

    private static Element fecho(Document xml) {
        Element fecho = (Element) xml.selectSingleNode("//*[local-name()='block'][@role='fecho']");
        assertThat(fecho).as("bloco do fecho").isNotNull();
        return fecho;
    }

    /** Textos dos blocos de uma célula de assinatura. */
    private static List<String> blocos(Element celula) {
        List<String> textos = new ArrayList<>();
        for (Element bloco : celula.elements()) {
            textos.add(bloco.getText());
        }
        return textos;
    }

    private static String espacos(int quantidade) {
        return String.join("", Collections.nCopies(quantidade, " "));
    }

    private ArrayNode parlamentares() {
        return (ArrayNode) metadadoLexedit().path("autoria").path("parlamentares").path("parlamentar");
    }

    private static ObjectNode parlamentar(String nome, String sexo, String partido, String uf, String casa, String cargo) {
        return new ObjectMapper().createObjectNode().put("nome", nome).put("sexo", sexo).put("siglaPartido", partido)
                .put("siglaUF", uf).put("siglaCasaLegislativa", casa).put("cargo", cargo);
    }

    private static Element cabecalho(Document xml) {
        Element cabecalho = (Element) xml.selectSingleNode("//*[local-name()='static-content'][@flow-name='header-principal']");
        assertThat(cabecalho).as("static-content do cabeçalho").isNotNull();
        return cabecalho;
    }

    private static String atributo(Element pai, String filho, String atributo) {
        Element elemento = (Element) pai.selectSingleNode("*[local-name()='" + filho + "']");
        assertThat(elemento).as(filho).isNotNull();
        return elemento.attributeValue(atributo);
    }

    private ObjectNode metadadoLexedit() {
        return (ObjectNode) documento.path("value").path("metadado").path("metadadoProprietario").path(0).path("any").path(0).path("value");
    }

    private ObjectNode opcoesImpressao() {
        return (ObjectNode) metadadoLexedit().path("opcoesImpressao");
    }

    private ObjectNode identificacao() {
        return (ObjectNode) documento.path("value").path("metadado").path("identificacao");
    }

    private String processar() {
        return processor.processar(documento, ParametrosImpressaoDocumentoArticulado.padrao(), CONTEUDO_FO);
    }

    private ObjectNode parteInicial() {
        JsonNode norma = documento.path("value").path("projetoNorma").path("norma");
        return (ObjectNode) norma.path("parteInicial");
    }

}
