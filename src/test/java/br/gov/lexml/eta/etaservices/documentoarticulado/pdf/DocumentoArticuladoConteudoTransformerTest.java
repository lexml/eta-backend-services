package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.xml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Testa a XSLT do conteúdo (documento-articulado.xml -> fragmento XSL-FO) com os documentos da issue #72.
 */
class DocumentoArticuladoConteudoTransformerTest {

    private static final String NBSP = " ";

    private final DocumentoArticuladoConteudoTransformer transformer = new DocumentoArticuladoConteudoTransformer();

    @ParameterizedTest
    @ValueSource(strings = { ARTICULACAO_E_ALTERACAO, CAPITULO_E_SECAO, PENA_E_TITULO_DISPOSITIVO })
    void fragmentoBemFormadoComRaizFoBlockEParteInicialNaOrdem(String nome) throws Exception {
        Element raiz = transformar(xml(nome)).getRootElement();

        assertThat(raiz.getQualifiedName()).isEqualTo("fo:block");
        List<Element> blocos = raiz.elements();
        assertThat(blocos).hasSize(3);
        assertThat(blocos.get(0).attributeValue("text-align")).isEqualTo("center");
        assertThat(blocos.get(1).attributeValue("space-before")).isEqualTo("48pt");
        assertThat(blocos.get(2).attributeValue("space-before")).isEqualTo("72pt");
    }

    @Test
    void epigrafeCentralizadaEmNegritoNoTamanhoDeDestaqueEmUmaLinha() throws Exception {
        Element epigrafe = bloco(ARTICULACAO_E_ALTERACAO, 0);

        assertThat(epigrafe.attributeValue("font-weight")).isEqualTo("bold");
        assertThat(epigrafe.attributeValue("font-size")).isEqualTo("16pt");
        // "PROJETO DE LEI Nº 999, DE 2026 " no documento: sem o espaço final e com espaços não quebráveis
        assertThat(epigrafe.getText()).isEqualTo(String.join(NBSP, "PROJETO", "DE", "LEI", "Nº", "999,", "DE", "2026"));
    }

    @Test
    void ementaComRecuoESpanComoTextoSimples() throws Exception {
        Element ementa = bloco(ARTICULACAO_E_ALTERACAO, 1);

        assertThat(ementa.attributeValue("margin-left")).isEqualTo("6.5cm");
        assertThat(ementa.attributeValue("text-indent")).isEqualTo("0");
        assertThat(ementa.attributeValue("text-align")).isEqualTo("justify");
        assertThat(ementa.attributeValue("line-height")).isEqualTo("150%");
        assertThat(texto(ementa))
                .startsWith("Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece as diretrizes")
                .doesNotContain("\"").doesNotContain("“");
        assertThat(ementa.asXML()).doesNotContain("basic-link").doesNotContain("urn:lex");
    }

    @Test
    void ementaPreservaNegritoEItalico() throws Exception {
        String xml = lexml("<Ementa id=\"ementa\">Altera o <i>caput</i> do art. 1º da Lei, <b>com urgência</b>.</Ementa>");

        Element ementa = (Element) transformar(xml).getRootElement().elements().get(0);

        assertThat(texto(ementa)).isEqualTo("Altera o caput do art. 1º da Lei, com urgência.");
        List<Element> inlines = ementa.elements();
        assertThat(inlines).hasSize(2);
        assertThat(inlines.get(0).attributeValue("font-style")).isEqualTo("italic");
        assertThat(inlines.get(0).getText()).isEqualTo("caput");
        assertThat(inlines.get(1).attributeValue("font-weight")).isEqualTo("bold");
        assertThat(inlines.get(1).getText()).isEqualTo("com urgência");
    }

    @Test
    void preambuloSomenteTextoSemNegrito() throws Exception {
        Element preambulo = bloco(CAPITULO_E_SECAO, 2);

        assertThat(preambulo.attributeValue("text-indent")).isEqualTo("2.5cm");
        assertThat(preambulo.attributeValue("text-align")).isEqualTo("justify");
        List<Element> paragrafos = preambulo.elements();
        assertThat(paragrafos).hasSize(1);
        assertThat(paragrafos.get(0).attributeValue("margin-bottom")).isEqualTo("0.6em");
        assertThat(paragrafos.get(0).getText()).isEqualTo("O PRESIDENTE DA REPÚBLICA, no uso da atribuição que lhe confere o art. 62 "
                + "da Constituição, adota a seguinte Medida Provisória, com força de lei:");
        assertThat(preambulo.asXML()).doesNotContain("fo:inline").doesNotContain("bold");
    }

    @Test
    void preambuloComVariosParagrafos() throws Exception {
        String xml = lexml("<Preambulo id=\"preambulo\"><p> Primeiro   parágrafo. </p><p><i>Segundo</i> parágrafo.</p></Preambulo>");

        Element preambulo = (Element) transformar(xml).getRootElement().elements().get(0);

        assertThat(preambulo.attributeValue("space-before")).isEqualTo("72pt");
        assertThat(preambulo.elements()).extracting(e -> ((Element) e).getText())
                .containsExactly("Primeiro parágrafo.", "Segundo parágrafo.");
    }

    @Test
    void naoImprimeTextoDosDispositivos() throws Exception {
        assertThat(transformer.transformar(xml(ARTICULACAO_E_ALTERACAO), ParametrosImpressaoDocumentoArticulado.padrao()))
                .doesNotContain("assegurar aos pais");
        assertThat(transformer.transformar(xml(CAPITULO_E_SECAO), ParametrosImpressaoDocumentoArticulado.padrao()))
                .doesNotContain("Fica instituído o Programa");
        assertThat(transformer.transformar(xml(PENA_E_TITULO_DISPOSITIVO), ParametrosImpressaoDocumentoArticulado.padrao()))
                .doesNotContain("Esta Lei tipifica");
    }

    @Test
    void elementoAusenteNaoGeraBlocoVazio() throws Exception {
        String xml = lexml("<Epigrafe id=\"epigrafe\">   </Epigrafe><Ementa id=\"ementa\">Dispõe sobre o tema.</Ementa>");

        List<Element> blocos = transformar(xml).getRootElement().elements();

        assertThat(blocos).hasSize(1);
        assertThat(blocos.get(0).attributeValue("margin-left")).isEqualTo("6.5cm");
    }

    @Test
    void documentoSemParteInicialGeraApenasARaiz() throws Exception {
        Element raiz = transformar(lexml("")).getRootElement();

        assertThat(raiz.getQualifiedName()).isEqualTo("fo:block");
        assertThat(raiz.elements()).isEmpty();
    }

    @Test
    void xmlInvalido() {
        assertThatThrownBy(() -> transformer.transformar("<LexML><sem-fechamento>", ParametrosImpressaoDocumentoArticulado.padrao()))
                .isInstanceOf(EtaBackendException.class);
    }

    private Document transformar(String xml) throws DocumentException {
        return DocumentHelper.parseText(transformer.transformar(xml, ParametrosImpressaoDocumentoArticulado.padrao()));
    }

    private Element bloco(String nome, int indice) throws DocumentException {
        return (Element) transformar(xml(nome)).getRootElement().elements().get(indice);
    }

    private static String texto(Element elemento) {
        return elemento.getStringValue().replaceAll("\\s+", " ").trim();
    }

    /** Documento LexML mínimo com a parte inicial informada e um artigo na articulação. */
    private static String lexml(String parteInicial) {
        return "<LexML xmlns=\"http://www.lexml.gov.br/1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"
                + "<Metadado><Identificacao URN=\"urn:lex:br:senado.federal:projeto.lei:2026;1\"/></Metadado>"
                + "<ProjetoNorma><Norma>"
                + (parteInicial.isEmpty() ? "" : "<ParteInicial>" + parteInicial + "</ParteInicial>")
                + "<Articulacao><Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Texto do artigo.</p></Caput></Artigo></Articulacao>"
                + "</Norma></ProjetoNorma></LexML>";
    }

}
