package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

        // Traço de 50% e 0,5pt; o XSL-FO exige os static-content (cabeçalho e separador) antes do flow
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

        // Primeira página: region-before de 3cm e corpo a 4cm; demais páginas sem cabeçalho
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

    // Alertas e pendências (issue #79) ----------------------------------------------------------------------------

    private static final String AVISO_REVISAO = "Este documento contém marcas de revisão e não deve ser protocolado até que estas sejam removidas.";
    private static final String AVISO_COMENTARIOS = "Este documento contém comentários e não deve ser protocolado até que sejam removidos.";
    private static final String AVISO_PENDENCIAS =
            "Este documento contém pendências de preenchimento e não deve ser protocolado até que estas sejam resolvidas:";

    @Test
    void tresCaixasAntesDoConteudoNaOrdemEComOEstiloDosAlertas() throws Exception {
        pendencias("Não foi informado um texto de justificação.", "Remover comentários.");

        List<Element> filhos = fluxo(processar()).elements();
        Element revisao = filhos.get(0);
        Element comentarios = filhos.get(1);
        Element pendencias = filhos.get(2);
        assertThat(filhos.get(3).getText()).isEqualTo("Conteúdo gerado");
        assertThat(revisao.getTextTrim()).isEqualTo(AVISO_REVISAO);
        assertThat(comentarios.getTextTrim()).isEqualTo(AVISO_COMENTARIOS);
        assertThat(pendencias.getTextTrim()).isEqualTo(AVISO_PENDENCIAS);
        for (Element caixa : new Element[] { revisao, comentarios, pendencias }) {
            assertThat(caixa.attributeValue("background-color")).isEqualTo("yellow");
            assertThat(caixa.attributeValue("color")).isEqualTo("red");
            assertThat(caixa.attributeValue("font-size")).isEqualTo("12pt");
            assertThat(caixa.attributeValue("font-weight")).isEqualTo("bold");
            assertThat(caixa.attributeValue("padding")).isEqualTo(".1cm");
            assertThat(caixa.attributeValue("space-after")).isEqualTo(".3cm");
        }
        assertThat(revisao.attributeValue("text-align")).isEqualTo("center");
        assertThat(comentarios.attributeValue("text-align")).isEqualTo("center");
        assertThat(pendencias.attributeValue("text-align")).isEqualTo("left");
        assertThat(pendencias.attributeValue("padding-left")).isEqualTo(".25cm");
        // Lista sem negrito, com marcador, um item por pendência como registrada (mesmo repetindo a caixa de comentários)
        Element lista = (Element) pendencias.selectSingleNode("*[local-name()='list-block']");
        assertThat(lista.attributeValue("font-weight")).isEqualTo("normal");
        assertThat(lista.attributeValue("margin-left")).isEqualTo(".5cm");
        assertThat(lista.selectNodes(".//*[local-name()='list-item-label']/*")).extracting(n -> ((Element) n).getText())
                .containsExactly("•", "•");
        assertThat(lista.selectNodes(".//*[local-name()='list-item-body']/*")).extracting(n -> ((Element) n).getText())
                .containsExactly("Não foi informado um texto de justificação.", "Remover comentários.");
    }

    @Test
    void semDadosNenhumaCaixa() throws Exception {
        metadadoLexedit().remove("revisoesTextuais");
        metadadoLexedit().remove("revisoesArticulacao");
        metadadoLexedit().remove("comentarios");
        metadadoLexedit().remove("pendencias");

        String fo = processar();
        assertThat(fluxo(fo).elements().get(0).getText()).isEqualTo("Conteúdo gerado");
        assertThat(fo).doesNotContain("background-color=\"yellow\"").doesNotContain("não deve ser protocolado");
    }

    @Test
    void soPendencias() throws Exception {
        metadadoLexedit().remove("revisoesTextuais");
        metadadoLexedit().remove("revisoesArticulacao");
        metadadoLexedit().remove("comentarios");
        pendencias("Não foi informado um texto de justificação.");

        List<Element> filhos = fluxo(processar()).elements();
        assertThat(filhos.get(0).getTextTrim()).isEqualTo(AVISO_PENDENCIAS);
        assertThat(filhos.get(1).getText()).isEqualTo("Conteúdo gerado");
    }

    @Test
    void soRevisaoDaArticulacao() throws Exception {
        metadadoLexedit().remove("revisoesTextuais");
        metadadoLexedit().remove("comentarios");
        metadadoLexedit().remove("pendencias");

        List<Element> filhos = fluxo(processar()).elements();
        assertThat(filhos.get(0).getTextTrim()).isEqualTo(AVISO_REVISAO);
        assertThat(filhos.get(1).getText()).isEqualTo("Conteúdo gerado");
    }

    @Test
    void caixasEm12ptMesmoComFonte18() throws Exception {
        opcoesImpressao().put("tamanhoFonte", 18);
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.de(OpcoesImpressaoDocumentoArticulado.de(documento));

        Element fluxo = fluxo(processor.processar(documento, parametros, CONTEUDO_FO));
        assertThat(fluxo.attributeValue("font-size")).isEqualTo("18pt");
        assertThat(fluxo.elements().get(0).attributeValue("font-size")).isEqualTo("12pt");
    }

    @Test
    void pendenciaEscapada() throws Exception {
        pendencias("Revisar \"A\" & <B>.");

        Document xml = new SAXReader().read(new StringReader(processar()));
        assertThat(xml.selectSingleNode("//*[local-name()='list-item-body']/*").getText()).isEqualTo("Revisar \"A\" & <B>.");
    }

    private void pendencias(String... textos) {
        ArrayNode lista = metadadoLexedit().putObject("pendencias").putArray("pendencia");
        for (String texto : textos) {
            lista.add(texto);
        }
    }

    private static Element fluxo(String fo) throws Exception {
        return (Element) new SAXReader().read(new StringReader(fo)).selectSingleNode("//*[local-name()='flow']");
    }

    // Local e data e assinaturas (issue #76) ----------------------------------------------------------------------

    @Test
    void fechoDepoisDoConteudoComRecuoEEspacoAntes() throws Exception {
        Document xml = new SAXReader().read(new StringReader(processar()));
        Element fluxo = (Element) xml.selectSingleNode("//*[local-name()='flow']");

        // Depois do fragmento do conteúdo (o último antes dele, depois dos alertas), um block-container com o fecho e
        // as assinaturas
        List<Element> filhos = fluxo.elements();
        Element container = filhos.get(filhos.size() - 1);
        assertThat(filhos.get(filhos.size() - 2).getText()).isEqualTo("Conteúdo gerado");
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

    /** Sem data válida, só o local seguido de vírgula (issue #81), sem espaços para preencher. */
    @ParameterizedTest
    @ValueSource(strings = { "", "24/04/2026", "ausente" })
    void dataVaziaAusenteOuInvalidaSoComOLocal(String data) throws Exception {
        if ("ausente".equals(data)) {
            metadadoLexedit().remove("data");
        } else {
            metadadoLexedit().put("data", data);
        }

        assertThat(fecho(new SAXReader().read(new StringReader(processar()))).getText()).isEqualTo("Sala das Sessões,");
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
