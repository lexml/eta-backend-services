package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.xml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
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
        assertThat(blocos).hasSize(4);
        assertThat(blocos.get(0).attributeValue("text-align")).isEqualTo("center");
        assertThat(blocos.get(1).attributeValue("space-before")).isEqualTo("48pt");
        assertThat(blocos.get(2).attributeValue("space-before")).isEqualTo("72pt");
        // Articulação: formatação da citação de dispositivos da emenda, sem espaço antes (margin-bottom do preâmbulo)
        Element articulacao = blocos.get(3);
        assertThat(articulacao.attributeValue("text-indent")).isEqualTo("2.5cm");
        assertThat(articulacao.attributeValue("text-align")).isEqualTo("justify");
        assertThat(articulacao.attributeValue("line-height")).isEqualTo("150%");
        assertThat(articulacao.attributeValue("space-before")).isNull();
    }

    @Test
    void artigoComRotuloEmNegritoNoMesmoBlocoDoCaput() throws Exception {
        List<Element> dispositivos = articulacao(xml(ARTICULACAO_E_ALTERACAO)).elements();

        Element artigo1 = dispositivos.get(0);
        Element rotulo = (Element) artigo1.elements().get(0);
        assertThat(rotulo.attributeValue("font-weight")).isEqualTo("bold");
        assertThat(rotulo.getText()).isEqualTo("Art. 1º");
        // span com xlink:href sai como texto simples
        assertThat(texto(artigo1)).startsWith("Art. 1º Esta Lei altera a Lei nº 9.394, de 20 de dezembro de 1996, para");
        assertThat(artigo1.asXML()).doesNotContain("basic-link").doesNotContain("urn:lex");
        // Dispositivos sem margens próprias: não há espaço extra entre eles
        for (Element dispositivo : dispositivos) {
            assertThat(dispositivo.attributes()).isEmpty();
        }
    }

    @Test
    void dispositivosNaOrdemDoDocumento() throws Exception {
        List<String> textos = textosDosDispositivos(articulacao(xml(CAPITULO_E_SECAO)));

        assertPrefixosEmOrdem(textos,
                "Art. 3º Observada a disponibilidade orçamentária e financeira",
                "§ 1º",
                "§ 2º Os recursos de que trata o caput:",
                "I – serão repassados pelo Ministério da Fazenda aos agentes financeiros",
                "II –",
                "§ 3º");
    }

    @Test
    void hierarquiaIncisoAlineaItem() throws Exception {
        String xml = lexmlComArticulacao("<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Caput:</p>"
                + "<Inciso id=\"art1_cpt_inc1\"><Rotulo>I –</Rotulo><p>inciso:</p>"
                + "<Alinea id=\"art1_cpt_inc1_ali1\"><Rotulo>a)</Rotulo><p>alínea:</p>"
                + "<Item id=\"art1_cpt_inc1_ali1_ite1\"><Rotulo>1.</Rotulo><p>item;</p></Item></Alinea>"
                + "<Alinea id=\"art1_cpt_inc1_ali2\"><Rotulo>b)</Rotulo><p>outra alínea.</p></Alinea></Inciso></Caput>"
                + "<Paragrafo id=\"art1_par1u\"><Rotulo>Parágrafo único.</Rotulo><p>Com <i>itálico</i>.</p></Paragrafo></Artigo>");

        Element articulacao = articulacao(xml);

        assertThat(textosDosDispositivos(articulacao)).containsExactly(
                "Art. 1º Caput:", "I – inciso:", "a) alínea:", "1. item;", "b) outra alínea.", "Parágrafo único. Com itálico.");
        assertThat(articulacao.asXML()).contains("<fo:inline font-style=\"italic\">itálico</fo:inline>");
    }

    @Test
    void capituloCentralizadoComRotuloEmNegritoSeguidoDoArtigo() throws Exception {
        List<Element> blocos = articulacao(xml(CAPITULO_E_SECAO)).elements();

        Element capitulo = blocos.get(0);
        assertThat(capitulo.attributeValue("text-align")).isEqualTo("center");
        assertThat(capitulo.attributeValue("text-indent")).isEqualTo("0");
        // Rótulo e nome na mesma página, e o título junto do conteúdo seguinte
        assertThat(capitulo.attributeValue("keep-together.within-page")).isEqualTo("always");
        assertThat(capitulo.attributeValue("keep-with-next.within-page")).isEqualTo("always");
        assertThat(capitulo.attributeValue("space-before")).isNull();
        assertThat(capitulo.attributeValue("space-after")).isNull();
        List<Element> linhas = capitulo.elements();
        assertThat(linhas).extracting(Element::getText).containsExactly("CAPÍTULO I", "DISPOSIÇÕES PRELIMINARES");
        assertThat(linhas.get(0).attributeValue("font-weight")).isEqualTo("bold");
        assertThat(linhas.get(1).attributeValue("font-weight")).isNull();
        assertThat(texto(blocos.get(1))).startsWith("Art. 1º Fica instituído o Programa");
    }

    @Test
    void capituloSecaoEArtigoNaOrdemComSecaoEmNegrito() throws Exception {
        List<Element> blocos = articulacao(xml(CAPITULO_E_SECAO)).elements();
        int capitulo3 = indiceDoTitulo(blocos, "CAPÍTULO III");

        assertThat(linhasDoTitulo(blocos.get(capitulo3))).containsExactly("CAPÍTULO III", "DOS PARTICIPANTES DO DESENROLA ADIMPLENTES");
        Element secao = blocos.get(capitulo3 + 1);
        assertThat(linhasDoTitulo(secao)).containsExactly("Seção I", "Dos beneficiários");
        for (Element linha : secao.elements()) {
            assertThat(linha.attributeValue("font-weight")).isEqualTo("bold");
        }
        // O <b> do nome no documento não gera fo:inline: a linha inteira já é negrito
        assertThat(secao.asXML()).doesNotContain("fo:inline");
        assertThat(texto(blocos.get(capitulo3 + 2))).startsWith("Art. 4º");
    }

    @Test
    void parteLivroETituloEmMaiusculasComAcentos() throws Exception {
        String xml = lexmlComArticulacao("<Parte id=\"prt1\"><Rotulo>Parte Geral</Rotulo><NomeAgrupador>das disposições gerais</NomeAgrupador>"
                + "<Livro id=\"prt1_liv1\"><Rotulo>Livro I</Rotulo><NomeAgrupador>Das Pessoas</NomeAgrupador>"
                + "<Titulo id=\"prt1_liv1_tit1\"><Rotulo>Título 1º</Rotulo><NomeAgrupador>Da <i>ação</i> e da exceção</NomeAgrupador>"
                + "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Texto.</p></Caput></Artigo>"
                + "</Titulo></Livro></Parte>");

        List<Element> blocos = articulacao(xml).elements();

        assertThat(linhasDoTitulo(blocos.get(0))).containsExactly("PARTE GERAL", "DAS DISPOSIÇÕES GERAIS");
        assertThat(linhasDoTitulo(blocos.get(1))).containsExactly("LIVRO I", "DAS PESSOAS");
        assertThat(linhasDoTitulo(blocos.get(2))).containsExactly("TÍTULO 1º", "DA AÇÃO E DA EXCEÇÃO");
        assertThat(blocos.get(2).asXML()).doesNotContain("fo:inline");
        assertThat(texto(blocos.get(3))).isEqualTo("Art. 1º Texto.");
    }

    @Test
    void subsecaoEmNegritoAgrupadorSemNomeEAgrupamentoSemTitulo() throws Exception {
        String xml = lexmlComArticulacao("<Capitulo id=\"cap1\"><Rotulo>CAPÍTULO I</Rotulo>"
                + "<Secao id=\"cap1_sec1\"><Rotulo>Seção I</Rotulo><NomeAgrupador>Das regras</NomeAgrupador>"
                + "<Subsecao id=\"cap1_sec1_sub1\"><Rotulo>Subseção I</Rotulo><NomeAgrupador>Das exceções</NomeAgrupador>"
                + "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Primeiro.</p></Caput></Artigo>"
                + "</Subsecao></Secao></Capitulo>"
                + "<Agrupamento id=\"agr1\"><Rotulo>Grupo</Rotulo><NomeAgrupador>Genérico</NomeAgrupador>"
                + "<Artigo id=\"art2\"><Rotulo>Art. 2º</Rotulo><Caput id=\"art2_cpt\"><p>Segundo.</p></Caput></Artigo></Agrupamento>");

        List<Element> blocos = articulacao(xml).elements();

        assertThat(blocos).hasSize(5);
        // Capítulo sem nome: só a linha do rótulo
        assertThat(linhasDoTitulo(blocos.get(0))).containsExactly("CAPÍTULO I");
        assertThat(linhasDoTitulo(blocos.get(1))).containsExactly("Seção I", "Das regras");
        Element subsecao = blocos.get(2);
        assertThat(linhasDoTitulo(subsecao)).containsExactly("Subseção I", "Das exceções");
        for (Element linha : subsecao.elements()) {
            assertThat(linha.attributeValue("font-weight")).isEqualTo("bold");
        }
        assertThat(texto(blocos.get(3))).isEqualTo("Art. 1º Primeiro.");
        // Agrupamento genérico: só atravessado, sem título
        assertThat(texto(blocos.get(4))).isEqualTo("Art. 2º Segundo.");
    }

    @Test
    void alteracaoDeNormaOmitida() throws Exception {
        String texto = texto(articulacao(xml(ARTICULACAO_E_ALTERACAO)));

        assertThat(texto).contains("Art. 2º A Lei nº 9.394, de 20 de dezembro de 1996, passa a vigorar acrescida do seguinte art. 12-A:")
                .contains("Art. 3º Esta Lei entra em vigor na data de sua publicação.")
                .doesNotContain("Art. 12-A.");
    }

    @Test
    void penaETituloDeDispositivoOmitidos() throws Exception {
        String texto = texto(articulacao(xml(PENA_E_TITULO_DISPOSITIVO)));

        assertThat(texto).contains("Art. 2º Desviar, apropriar-se, utilizar")
                .doesNotContain("Pena –")
                .doesNotContain("Desvio ou apropriação de recursos e insumos da saúde");
    }

    @Test
    void semAspasEnvolvendoAArticulacao() throws Exception {
        List<String> textos = textosDosDispositivos(articulacao(xml(PENA_E_TITULO_DISPOSITIVO)));

        assertThat(textos.get(0)).startsWith("Art. 1º");
        assertThat(textos.get(textos.size() - 1)).doesNotEndWith("”").doesNotEndWith("\"");
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
    void elementoAusenteNaoGeraBlocoVazio() throws Exception {
        String xml = lexml("<Epigrafe id=\"epigrafe\">   </Epigrafe><Ementa id=\"ementa\">Dispõe sobre o tema.</Ementa>");

        List<Element> blocos = transformar(xml).getRootElement().elements();

        assertThat(blocos).hasSize(2);
        assertThat(blocos.get(0).attributeValue("margin-left")).isEqualTo("6.5cm");
        assertThat(blocos.get(1).attributeValue("text-indent")).isEqualTo("2.5cm");
    }

    @Test
    void documentoSemParteInicialImprimeSoAArticulacao() throws Exception {
        Element raiz = transformar(lexml("")).getRootElement();

        assertThat(raiz.getQualifiedName()).isEqualTo("fo:block");
        assertThat(raiz.elements()).hasSize(1);
        assertThat(textosDosDispositivos((Element) raiz.elements().get(0))).containsExactly("Art. 1º Texto do artigo.");
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

    /** Bloco da articulação: o que tem recuo de primeira linha de 2,5cm na raiz do fragmento. */
    private Element articulacao(String xml) throws DocumentException {
        List<Element> blocos = transformar(xml).getRootElement().elements();
        return blocos.stream().filter(b -> "2.5cm".equals(b.attributeValue("text-indent"))
                && b.attributeValue("space-before") == null).findFirst().orElseThrow(AssertionError::new);
    }

    /** Texto de cada dispositivo (bloco filho da articulação), com espaços normalizados. */
    private static List<String> textosDosDispositivos(Element articulacao) {
        List<String> textos = new ArrayList<>();
        for (Element dispositivo : articulacao.elements()) {
            textos.add(texto(dispositivo));
        }
        return textos;
    }

    /** Linhas (rótulo e nome) do bloco de título de um agrupador. */
    private static List<String> linhasDoTitulo(Element titulo) {
        List<String> linhas = new ArrayList<>();
        for (Element linha : titulo.elements()) {
            linhas.add(linha.getText());
        }
        return linhas;
    }

    /** Índice do bloco de título de agrupador cuja primeira linha é o rótulo informado. */
    private static int indiceDoTitulo(List<Element> blocos, String rotulo) {
        for (int i = 0; i < blocos.size(); i++) {
            List<Element> linhas = blocos.get(i).elements();
            if ("center".equals(blocos.get(i).attributeValue("text-align")) && !linhas.isEmpty()
                    && rotulo.equals(linhas.get(0).getText())) {
                return i;
            }
        }
        throw new AssertionError("Título de agrupador não encontrado: " + rotulo);
    }

    /** Verifica que há, em sequência, dispositivos consecutivos que começam com cada prefixo informado. */
    private static void assertPrefixosEmOrdem(List<String> textos, String... prefixos) {
        int inicio = -1;
        for (int i = 0; i < textos.size() && inicio < 0; i++) {
            if (textos.get(i).startsWith(prefixos[0])) {
                inicio = i;
            }
        }
        assertThat(inicio).as("dispositivo iniciado por '%s'", prefixos[0]).isNotNegative();
        for (int j = 1; j < prefixos.length; j++) {
            assertThat(textos.get(inicio + j)).startsWith(prefixos[j]);
        }
    }

    /** Documento LexML mínimo com a parte inicial informada e um artigo na articulação. */
    private static String lexml(String parteInicial) {
        return documento(parteInicial.isEmpty() ? "" : "<ParteInicial>" + parteInicial + "</ParteInicial>",
                "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Texto do artigo.</p></Caput></Artigo>");
    }

    /** Documento LexML mínimo sem parte inicial e com a articulação informada. */
    private static String lexmlComArticulacao(String articulacao) {
        return documento("", articulacao);
    }

    private static String documento(String parteInicial, String articulacao) {
        return "<LexML xmlns=\"http://www.lexml.gov.br/1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"
                + "<Metadado><Identificacao URN=\"urn:lex:br:senado.federal:projeto.lei:2026;1\"/></Metadado>"
                + "<ProjetoNorma><Norma>" + parteInicial + "<Articulacao>" + articulacao + "</Articulacao></Norma></ProjetoNorma></LexML>";
    }

}
