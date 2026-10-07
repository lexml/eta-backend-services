package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.REMISSOES_INTERNAS;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.xml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
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
        // span com xlink:href (URN) sai como link para o portal normas.leg.br
        assertThat(texto(artigo1)).startsWith("Art. 1º Esta Lei altera a Lei nº 9.394, de 20 de dezembro de 1996, para");
        assertThat(unicoLink(artigo1).attributeValue("external-destination"))
                .isEqualTo("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394')");
        // Dispositivos sem margens próprias: não há espaço extra entre eles (o bloco de alteração tem só o recuo)
        for (Element dispositivo : dispositivos) {
            if ("3cm".equals(dispositivo.attributeValue("margin-left"))) {
                assertThat(dispositivo.attributes()).extracting(a -> a.getName()).containsOnly("margin-left", "text-indent");
            } else {
                assertThat(dispositivo.attributes()).isEmpty();
            }
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
    void blocoDeAlteracaoRecuadoComAspasENotaPelosAtributos() throws Exception {
        List<Element> blocos = articulacao(xml(ARTICULACAO_E_ALTERACAO)).elements();
        int indice = indiceDaAlteracao(blocos);
        Element alteracao = blocos.get(indice);

        assertThat(alteracao.attributeValue("margin-left")).isEqualTo("3cm");
        assertThat(alteracao.attributeValue("text-indent")).isEqualTo("1.5cm");
        assertThat(alteracao.attributeValue("space-before")).isNull();
        assertThat(alteracao.attributeValue("space-after")).isNull();
        assertThat(texto(blocos.get(indice - 1)))
                .isEqualTo("Art. 2º A Lei nº 9.394, de 20 de dezembro de 1996, passa a vigorar acrescida do seguinte art. 12-A:");
        assertThat(texto(blocos.get(indice + 1))).startsWith("Art. 3º Esta Lei entra em vigor");

        List<Element> dispositivos = alteracao.elements();
        Element primeiro = dispositivos.get(0);
        // Aspas de abertura antes do rótulo e fora do negrito
        assertThat(texto(primeiro)).startsWith("“Art. 12-A. As instituições de ensino");
        assertThat(primeiro.content().get(0).getText()).isEqualTo("“");
        Element rotulo = (Element) primeiro.elements().get(0);
        assertThat(rotulo.getText()).isEqualTo("Art. 12-A.");
        assertThat(rotulo.attributeValue("font-weight")).isEqualTo("bold");
        // Fechamento só no último dispositivo, colado ao texto, com a nota e nada depois
        String ultimo = texto(dispositivos.get(dispositivos.size() - 1));
        assertThat(ultimo).startsWith("§ 4º").endsWith("diálogo entre escola e família.” (NR)");
        String textoDoBloco = texto(alteracao);
        assertThat(textoDoBloco.chars().filter(c -> c == '“').count()).isEqualTo(1);
        assertThat(textoDoBloco.chars().filter(c -> c == '”').count()).isEqualTo(1);
    }

    @Test
    void textoOmitidoOmissisEFechamentoNoParagrafo() throws Exception {
        List<Element> blocos = articulacao(xml(PENA_E_TITULO_DISPOSITIVO)).elements();
        List<Element> dispositivos = blocos.get(indiceDaAlteracao(blocos)).elements();

        assertThat(dispositivos).hasSize(3);
        // Caput com textoOmitido: rótulo do artigo seguido de linha pontilhada
        assertThat(texto(dispositivos.get(0))).isEqualTo("“Art. 327.");
        assertThat(temLinhaPontilhada(dispositivos.get(0))).isTrue();
        // Omissis: só a linha pontilhada
        assertThat(texto(dispositivos.get(1))).isEmpty();
        assertThat(temLinhaPontilhada(dispositivos.get(1))).isTrue();
        assertThat(texto(dispositivos.get(2)))
                .isEqualTo("§ 3º A pena será aumentada da metade se o crime for praticado contra a administração da saúde pública.” (NR)");
    }

    @Test
    void incisoComTextoOmitidoOmissisEFechamentoNoOmissis() throws Exception {
        List<Element> blocos = articulacao(xml(CAPITULO_E_SECAO)).elements();
        List<Element> dispositivos = blocos.get(indiceDaAlteracao(blocos)).elements();
        List<String> textos = new ArrayList<>();
        for (Element dispositivo : dispositivos) {
            textos.add(texto(dispositivo));
        }

        assertThat(textos).hasSize(6);
        assertThat(textos.get(0)).isEqualTo("“Art. 7º");
        assertThat(textos.get(1)).isEqualTo("I –");
        assertThat(textos.get(2)).isEmpty();
        assertThat(textos.get(3)).startsWith("k) pessoas físicas beneficiárias do Fundo de Financiamento Estudantil");
        assertThat(textos.get(4)).startsWith("l) pessoas físicas participantes do Programa Extraordinário");
        // Omissis final com o fechamento das aspas e a nota na mesma linha
        assertThat(textos.get(5)).isEqualTo("” (NR)");
        for (int i : new int[] { 0, 1, 2, 5 }) {
            assertThat(temLinhaPontilhada(dispositivos.get(i))).as("linha pontilhada no bloco %d", i).isTrue();
        }
    }

    @Test
    void semAspasForaDosAtributosEFechamentoNoUltimoParagrafo() throws Exception {
        String xml = lexmlComArticulacao("<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Altera:</p>"
                + "<Alteracao id=\"art1_cpt_alt1\">"
                + "<Artigo id=\"art1_cpt_alt1_art5\"><Rotulo>Art. 5º</Rotulo><Caput id=\"art1_cpt_alt1_art5_cpt\"><p>Sem aspas.</p></Caput>"
                + "<Paragrafo id=\"art1_cpt_alt1_art5_par1u\" fechaAspas=\"s\" notaAlteracao=\"NR\"><Rotulo>Parágrafo único.</Rotulo>"
                + "<p>Primeiro.</p><p>Segundo <i>trecho</i>. </p></Paragrafo></Artigo>"
                + "</Alteracao></Caput></Artigo>");

        List<Element> blocos = articulacao(xml).elements();
        List<Element> dispositivos = blocos.get(indiceDaAlteracao(blocos)).elements();

        assertThat(dispositivos).extracting(e -> texto(e))
                .containsExactly("Art. 5º Sem aspas.", "Parágrafo único. Primeiro.", "Segundo trecho.” (NR)");
        // O espaço do fim do texto não fica antes das aspas
        assertThat(dispositivos.get(2).asXML()).contains(".” (NR)");
    }

    @Test
    void tituloDeDispositivoEmNegritoAntesDoArtigoEPenaAposOCaput() throws Exception {
        List<Element> blocos = articulacao(xml(PENA_E_TITULO_DISPOSITIVO)).elements();
        List<String> textos = textosDosDispositivos(articulacao(xml(PENA_E_TITULO_DISPOSITIVO)));
        int indiceTitulo = textos.indexOf("Desvio ou apropriação de recursos e insumos da saúde");

        assertThat(indiceTitulo).isNotNegative();
        Element titulo = blocos.get(indiceTitulo);
        assertThat(titulo.attributeValue("font-weight")).isEqualTo("bold");
        assertThat(titulo.attributeValue("keep-with-next.within-page")).isEqualTo("always");
        // Mesma formatação dos dispositivos: herda recuo e justificação, não é centralizado
        assertThat(titulo.attributeValue("text-align")).isNull();
        assertThat(titulo.attributeValue("text-indent")).isNull();
        assertThat(titulo.elements()).isEmpty();
        assertPrefixosEmOrdem(textos,
                "Desvio ou apropriação de recursos e insumos da saúde",
                "Art. 2º Desviar, apropriar-se, utilizar",
                "Pena – reclusão, de 4 (quatro) a 12 (doze) anos, e multa.",
                "§ 1º");

        Element pena = blocos.get(indiceTitulo + 2);
        assertThat(pena.attributes()).isEmpty();
        Element rotuloPena = (Element) pena.elements().get(0);
        assertThat(rotuloPena.getText()).isEqualTo("Pena –");
        assertThat(rotuloPena.attributeValue("font-weight")).isNull();
    }

    @Test
    void penaNoFimDoParagrafo() throws Exception {
        List<String> textos = textosDosDispositivos(articulacao(xml(PENA_E_TITULO_DISPOSITIVO)));

        int paragrafo3 = -1;
        for (int i = 0; i < textos.size() && paragrafo3 < 0; i++) {
            if (textos.get(i).startsWith("§ 3º")) {
                paragrafo3 = i;
            }
        }
        assertThat(paragrafo3).isNotNegative();
        assertThat(textos.get(paragrafo3 + 1)).isEqualTo("Pena – reclusão, de 6 (seis) a 14 (quatorze) anos, e multa.");
        assertThat(textos).filteredOn(t -> t.startsWith("Pena –")).hasSize(5);
        assertThat(textos).filteredOn(t -> t.equals("Fraude em registros e sistemas da saúde pública")
                || t.equals("Inobservância ilícita da ordem de atendimento")).hasSize(2);
    }

    @Test
    void tituloDeDispositivoEmParagrafoEVaziosSemBloco() throws Exception {
        String xml = lexmlComArticulacao("<Artigo id=\"art1\"><TituloDispositivo>   </TituloDispositivo><Rotulo>Art. 1º</Rotulo>"
                + "<Caput id=\"art1_cpt\"><p>Caput.</p><Pena id=\"art1_cpt_pena\"><Rotulo> </Rotulo><p>  </p></Pena></Caput>"
                + "<Paragrafo id=\"art1_par1u\"><TituloDispositivo>Forma <i>qualificada</i></TituloDispositivo>"
                + "<Rotulo>Parágrafo único.</Rotulo><p>Texto do parágrafo.</p></Paragrafo></Artigo>");

        List<Element> blocos = articulacao(xml).elements();

        // Título vazio e pena vazia (sem rótulo e sem texto) não geram bloco
        assertThat(textosDosDispositivos(articulacao(xml)))
                .containsExactly("Art. 1º Caput.", "Forma qualificada", "Parágrafo único. Texto do parágrafo.");
        Element tituloDoParagrafo = blocos.get(1);
        assertThat(tituloDoParagrafo.attributeValue("font-weight")).isEqualTo("bold");
        assertThat(tituloDoParagrafo.asXML()).contains("<fo:inline font-style=\"italic\">qualificada</fo:inline>");
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
    void ementaComRecuoELinkParaANormaReferida() throws Exception {
        Element ementa = bloco(ARTICULACAO_E_ALTERACAO, 1);

        assertThat(ementa.attributeValue("margin-left")).isEqualTo("6.5cm");
        assertThat(ementa.attributeValue("text-indent")).isEqualTo("0");
        assertThat(ementa.attributeValue("text-align")).isEqualTo("justify");
        assertThat(ementa.attributeValue("line-height")).isEqualTo("150%");
        assertThat(texto(ementa))
                .startsWith("Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece as diretrizes")
                .doesNotContain("\"").doesNotContain("“");
        Element link = unicoLink(ementa);
        assertThat(link.attributeValue("external-destination"))
                .isEqualTo("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394')");
        assertThat(link.attributeValue("color")).isEqualTo("#808080");
        assertThat(link.attributeValue("text-decoration")).isNull();
        assertThat(link.getText()).isEqualTo("Lei nº 9.394, de 20 de dezembro de 1996");
    }

    @Test
    void remissaoExternaDentroDoBlocoDeAlteracao() throws Exception {
        List<Element> blocos = articulacao(xml(ARTICULACAO_E_ALTERACAO)).elements();

        List<String> destinos = new ArrayList<>();
        for (Node link : blocos.get(indiceDaAlteracao(blocos)).selectNodes(".//*[local-name()='basic-link']")) {
            destinos.add(((Element) link).attributeValue("external-destination"));
        }
        assertThat(destinos).contains("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:1990-07-13;8069!art58')",
                "url('https://normas.leg.br/?urn=urn:lex:br:federal:constituicao:1988-10-05;1988')");
    }

    @Test
    void remissaoInternaComAlvoAusenteSemLink() throws Exception {
        Document fo = transformar(recurso(XML_EXEMPLO));

        // O art. 7º foi excluído: a remissão fica como texto, sem link e sem destino
        assertThat(texto(fo.getRootElement())).contains("observado o disposto no art. 7º desta Lei");
        assertThat(fo.selectNodes("//*[local-name()='basic-link'][contains(., '7º')]")).isEmpty();
        assertThat(fo.selectNodes("//*[@internal-destination='art7' or @id='art7']")).isEmpty();

        // As demais remissões internas do exemplo são válidas: link e destino (artigo, parágrafo, inciso,
        // capítulo e subseção); o art. 1º é alvo de uma remissão da articulação e de outra da justificação.
        // Um id por alvo e nenhum outro id
        List<String> internos = new ArrayList<>();
        for (Node link : fo.selectNodes("//*[local-name()='basic-link'][@internal-destination]")) {
            internos.add(((Element) link).attributeValue("internal-destination"));
        }
        assertThat(internos).containsExactlyInAnyOrder("art1_par1", "art1_par1_inc2", "tit1_cap1", "art1",
                "tit2_cap2_sec1_sub2", "art1");
        List<String> ids = new ArrayList<>();
        for (Node id : fo.selectNodes("//@id")) {
            ids.add(id.getText());
        }
        assertThat(ids).containsExactlyInAnyOrder("art1_par1", "art1_par1_inc2", "tit1_cap1", "art1", "tit2_cap2_sec1_sub2");
    }

    @Test
    void remissoesInternasValidasComDestinoSoNosAlvos() throws Exception {
        Document fo = transformar(xml(REMISSOES_INTERNAS));

        // Destinos: rótulo do Art. 1º, bloco do § 1º e bloco do título do Capítulo I; nenhum outro id
        List<String> ids = new ArrayList<>();
        for (Node id : fo.selectNodes("//@id")) {
            ids.add(id.getText());
        }
        assertThat(ids).containsExactlyInAnyOrder("art1", "art1_par1", "cap1");
        Element rotuloArt1 = (Element) fo.selectSingleNode("//*[@id='art1']");
        assertThat(rotuloArt1.getName()).isEqualTo("inline");
        assertThat(rotuloArt1.getText()).isEqualTo("Art. 1º");
        assertThat(texto((Element) fo.selectSingleNode("//*[@id='art1_par1']"))).startsWith("§ 1º As regras valem");
        assertThat(texto((Element) fo.selectSingleNode("//*[@id='cap1']"))).contains("CAPÍTULO I");

        // Links internos no caput do Art. 2º; o art. 9º (inexistente) fica como texto
        List<String> internos = new ArrayList<>();
        for (Node link : fo.selectNodes("//*[local-name()='basic-link'][@internal-destination]")) {
            internos.add(((Element) link).attributeValue("internal-destination") + "=" + link.getText());
            assertThat(((Element) link).attributeValue("color")).isEqualTo("#808080");
        }
        assertThat(internos).containsExactly("art1=art. 1º", "art1_par1=§ 1º do art. 1º", "cap1=Capítulo I");
        assertThat(texto(fo.getRootElement())).contains("e o art. 9º.");
    }

    @Test
    void preambuloSemLinkEEmentaComLink() throws Exception {
        List<Element> blocos = transformar(xml(REMISSOES_INTERNAS)).getRootElement().elements();

        assertThat(unicoLink(blocos.get(1)).attributeValue("external-destination"))
                .isEqualTo("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:1990-09-11;8078')");
        Element preambulo = blocos.get(2);
        assertThat(preambulo.attributeValue("space-before")).isEqualTo("72pt");
        assertThat(preambulo.asXML()).doesNotContain("basic-link");
        assertThat(texto(preambulo)).contains("com base na Constituição Federal, decreta:");
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

    // Justificação (issue #75, parte A) -------------------------------------------------------------------------

    @Test
    void justificacaoDepoisDaArticulacaoComTituloECorpo() throws Exception {
        List<Element> blocos = transformar(xml(JUSTIFICACAO)).getRootElement().elements();
        Element titulo = blocos.get(blocos.size() - 2);
        Element corpo = blocos.get(blocos.size() - 1);

        // Antes do título, o bloco da articulação
        assertThat(blocos.get(blocos.size() - 3).attributeValue("text-align")).isEqualTo("justify");
        assertThat(texto(blocos.get(blocos.size() - 3))).startsWith("Art. 1º Fica instituída");
        // Título: valores do bloco "JUSTIFICAÇÃO" da emenda
        assertThat(titulo.getText()).isEqualTo("JUSTIFICAÇÃO");
        assertThat(titulo.attributeValue("text-align")).isEqualTo("center");
        assertThat(titulo.attributeValue("font-weight")).isEqualTo("bold");
        assertThat(titulo.attributeValue("font-size")).isEqualTo("16pt");
        assertThat(titulo.attributeValue("space-before")).isEqualTo("26pt");
        assertThat(titulo.attributeValue("keep-with-next.within-page")).isEqualTo("always");
        // Corpo: bloco role="Justificativa" da emenda, alinhado à esquerda
        assertThat(corpo.attributeValue("space-before")).isEqualTo("14pt");
        assertThat(corpo.attributeValue("line-height")).isEqualTo("150%");
        assertThat(corpo.attributeValue("text-indent")).isEqualTo("2.5cm");
        assertThat(corpo.attributeValue("text-align")).isNull();
        // Um bloco por parágrafo, com o espaço após o parágrafo dos parâmetros de impressão
        Element primeiro = (Element) corpo.elements().get(0);
        assertThat(texto(primeiro)).startsWith("A presente proposição");
        assertThat(primeiro.attributeValue("margin-bottom")).isEqualTo("0.6em");
        assertThat(primeiro.attributeValue("text-align")).isNull();
        assertThat(primeiro.attributeValue("text-indent")).isNull();
        assertThat(texto(corpo)).endsWith("contamos com o apoio dos nobres Pares para a aprovação desta proposição.");
    }

    @Test
    void justificacaoComFormatacaoInline() throws Exception {
        Element paragrafo = paragrafoDaJustificacao("A presente proposição");

        assertThat(texto(paragrafo)).endsWith("deficiência visual a livros digitais acessíveis.");
        assertThat(inlineComTexto(paragrafo, "livros").attributeValue("font-weight")).isEqualTo("bold");
        assertThat(inlineComTexto(paragrafo, "digitais").attributeValue("font-style")).isEqualTo("italic");
        assertThat(inlineComTexto(paragrafo, "acessíveis").attributeValue("text-decoration")).isEqualTo("underline");

        Element formulas = paragrafoDaJustificacao("Os padrões técnicos");
        assertThat(texto(formulas)).contains("como H2O, e de unidades, como m2, pelos leitores de tela.");
        List<Node> rebaixados = formulas.selectNodes("*[@baseline-shift='sub']");
        List<Node> elevados = formulas.selectNodes("*[@baseline-shift='super']");
        assertThat(rebaixados).hasSize(1);
        assertThat(elevados).hasSize(1);
        assertThat(((Element) rebaixados.get(0)).attributeValue("font-size")).isEqualTo("0.7em");
        assertThat(((Element) elevados.get(0)).getText()).isEqualTo("2");
    }

    @Test
    void estilosDeParagrafoDoEditor() throws Exception {
        Element centralizado = paragrafoDaJustificacao("Parágrafo centralizado");
        assertThat(centralizado.attributeValue("text-align")).isEqualTo("center");
        assertThat(centralizado.attributeValue("text-indent")).isEqualTo("0");

        Element direita = paragrafoDaJustificacao("Parágrafo alinhado à direita");
        assertThat(direita.attributeValue("text-align")).isEqualTo("right");
        assertThat(direita.attributeValue("text-indent")).isEqualTo("0");

        Element justificado = paragrafoDaJustificacao("Parágrafo justificado");
        assertThat(justificado.attributeValue("text-align")).isEqualTo("justify");
        assertThat(justificado.attributeValue("text-indent")).isNull();

        Element semRecuo = paragrafoDaJustificacao("Parágrafo sem recuo e sem espaço depois");
        assertThat(semRecuo.attributeValue("text-indent")).isEqualTo("0");
        assertThat(semRecuo.attributeValue("margin-bottom")).isEqualTo("0");

        // Como na emenda: só margem e recuo, sem forçar o alinhamento
        Element ementa = paragrafoDaJustificacao("Parágrafo com estilo de ementa");
        assertThat(ementa.attributeValue("margin-left")).isEqualTo("6.5cm");
        assertThat(ementa.attributeValue("text-indent")).isEqualTo("0");
        assertThat(ementa.attributeValue("text-align")).isNull();

        Element normaAlterada = paragrafoDaJustificacao("Parágrafo com estilo de norma alterada");
        assertThat(normaAlterada.attributeValue("margin-left")).isEqualTo("3cm");
        assertThat(normaAlterada.attributeValue("text-indent")).isEqualTo("1.5cm");
        assertThat(normaAlterada.attributeValue("text-align")).isNull();
    }

    @Test
    void classeDesconhecidaEhIgnorada() throws Exception {
        Element paragrafo = (Element) corpoDaJustificacao(documentoComJustificacao(
                "<p class=\"ql-outra ql-align-center\">Texto.</p>")).elements().get(0);

        assertThat(paragrafo.attributeValue("text-align")).isEqualTo("center");
        assertThat(paragrafo.attributes()).extracting(a -> a.getName())
                .containsExactlyInAnyOrder("margin-bottom", "text-align", "text-indent");
    }

    @Test
    void versaoRevisadaSemExclusoesEComInclusoesSemDestaque() throws Exception {
        Element paragrafo = paragrafoDaJustificacao("O Ministério da Educação");

        // <del>fiscalização</del><ins>coordenação</ins>; a exclusão com nota de rodapé também some
        assertThat(texto(paragrafo)).isEqualTo(
                "O Ministério da Educação será responsável pela coordenação da política, nos termos do art. 1º desta proposição.");
        assertThat(paragrafo.selectNodes(".//*[@text-decoration or @color != '#808080']")).isEmpty();
        assertThat(texto(transformar(xml(JUSTIFICACAO)).getRootElement())).doesNotContain("fiscalização")
                .doesNotContain("conforme regulamento").doesNotContain("Nota excluída na revisão");
    }

    @Test
    void trechoComentadoComoTextoNormal() throws Exception {
        Element paragrafo = paragrafoDaJustificacao("O impacto orçamentário é reduzido");

        assertThat(texto(paragrafo)).startsWith("O impacto orçamentário é reduzido, pois a política utiliza a estrutura existente");
        assertThat(paragrafo.selectNodes("*[contains(., 'impacto orçamentário')]")).isEmpty();
        assertThat(transformar(xml(JUSTIFICACAO)).selectNodes("//@id[starts-with(., '_tc')]")).isEmpty();
    }

    @Test
    void linksERemissoesNaJustificacao() throws Exception {
        Element revisado = paragrafoDaJustificacao("O Ministério da Educação");
        List<Node> links = revisado.selectNodes(".//*[local-name()='basic-link']");
        assertThat(links).hasSize(2);
        Element externo = (Element) links.get(0);
        assertThat(externo.attributeValue("external-destination")).isEqualTo("url('https://www.gov.br/mec')");
        assertThat(externo.attributeValue("color")).isEqualTo("#808080");
        assertThat(externo.getText()).isEqualTo("Ministério da Educação");
        Element interno = (Element) links.get(1);
        assertThat(interno.attributeValue("internal-destination")).isEqualTo("art1");
        // O alvo da remissão da justificação recebe o id na articulação
        assertThat(transformar(xml(JUSTIFICACAO)).selectNodes("//*[@id='art1']")).hasSize(1);

        Element comUrn = paragrafoDaJustificacao("Segundo dados oficiais");
        assertThat(unicoLink(comUrn).attributeValue("external-destination"))
                .isEqualTo("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:2015-07-06;13146')");

        // a sem endereço: só o texto
        Element semEndereco = paragrafoDaJustificacao("Mais informações");
        assertThat(semEndereco.selectNodes(".//*[local-name()='basic-link']")).isEmpty();
        assertThat(texto(semEndereco)).isEqualTo("Mais informações estão disponíveis em endereço não informado.");
    }

    @Test
    void paragrafoVazioComoLinhaEmBranco() throws Exception {
        List<Element> paragrafos = corpoDaJustificacao(xml(JUSTIFICACAO)).elements();
        List<Element> vazios = new ArrayList<>();
        for (Element paragrafo : paragrafos) {
            if (paragrafo.getText().equals(NBSP)) {
                vazios.add(paragrafo);
            }
        }

        assertThat(vazios).hasSize(1);
        assertThat(vazios.get(0).attributeValue("margin-bottom")).isEqualTo("0.6em");
    }

    @Test
    void listasTabelasEImagensAindaNaoImpressas() throws Exception {
        Document fo = transformar(xml(JUSTIFICACAO));

        assertThat(texto(fo.getRootElement())).doesNotContain("Item de lista").doesNotContain("Indicador")
                .contains("Imagem ainda não impressa:");
        assertThat(fo.selectNodes("//*[local-name()='external-graphic' or local-name()='table' or local-name()='list-block']"))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "<p/>", "<p> </p><p/>", "<p><del id=\"_rt1\">Texto excluído.</del></p>" })
    void justificacaoSemTextoNaoImprimeTitulo(String partePrincipal) throws Exception {
        Document fo = transformar(documentoComJustificacao(partePrincipal));

        assertThat(texto(fo.getRootElement())).doesNotContain("JUSTIFICAÇÃO");
        assertThat(fo.getRootElement().elements()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { ARTICULACAO_E_ALTERACAO, CAPITULO_E_SECAO, PENA_E_TITULO_DISPOSITIVO, REMISSOES_INTERNAS })
    void documentoSemJustificacaoNaoImprimeTitulo(String nome) throws Exception {
        assertThat(texto(transformar(xml(nome)).getRootElement())).doesNotContain("JUSTIFICAÇÃO");
    }

    @Test
    void justificacaoDoExemploNaVersaoRevisada() throws Exception {
        String texto = texto(corpoDaJustificacao(recurso(XML_EXEMPLO)));

        // Exemplo da especificação do lexml-eta: <del>reforma</del><ins>modernização</ins> e trecho comentado
        assertThat(texto).startsWith("Esta proposição promove a modernização da gestão pública, nos termos do art. 1º.");
        assertThat(texto).doesNotContain("reforma");
    }

    @Test
    void variasJustificacoesComUmUnicoTitulo() throws Exception {
        String justificacoes = "<Justificacao><PartePrincipal><p>Primeira.</p></PartePrincipal></Justificacao>"
                + "<Justificacao><PartePrincipal><p>Segunda.</p></PartePrincipal></Justificacao>";
        Document fo = transformar(documento("", "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\">"
                + "<p>Texto.</p></Caput></Artigo>").replace("</Norma></ProjetoNorma>", "</Norma>" + justificacoes + "</ProjetoNorma>"));

        assertThat(fo.selectNodes("//*[text()='JUSTIFICAÇÃO']")).hasSize(1);
        assertThat(corpoDaJustificacao(fo).elements()).extracting(p -> p.getText()).containsExactly("Primeira.", "Segunda.");
    }

    // Notas de rodapé (issue #75, parte A) ----------------------------------------------------------------------

    @Test
    void notasDeRodapeNumeradasNaOrdemSemAsExcluidas() throws Exception {
        Document fo = transformar(xml(JUSTIFICACAO));
        List<Node> notas = fo.selectNodes("//*[local-name()='footnote']");

        // Quatro NotaDeRodape no documento; a que está dentro de <del> não é impressa nem numerada
        assertThat(notas).hasSize(3);
        List<String> numeros = new ArrayList<>();
        List<String> corpos = new ArrayList<>();
        for (Node nota : notas) {
            Element referencia = (Element) ((Element) nota).elements().get(0);
            assertThat(referencia.getName()).isEqualTo("inline");
            assertThat(referencia.attributeValue("baseline-shift")).isEqualTo("super");
            assertThat(referencia.attributeValue("font-size")).isEqualTo("0.7em");
            numeros.add(referencia.getText());
            corpos.add(texto((Element) ((Element) nota).selectSingleNode("*[local-name()='footnote-body']")));
        }
        assertThat(numeros).containsExactly("1", "2", "3");
        assertThat(corpos).containsExactly("1 Fonte: Pesquisa Nacional de Saúde, IBGE, 2019.",
                "2 Ver a estimativa de impacto, elaborada nos termos da Lei de Responsabilidade Fiscal.", "3 Nota simples.");
        assertThat(texto(fo.getRootElement())).doesNotContain("Nota excluída na revisão");
    }

    @Test
    void notaDeRodapeNaPosicaoDaReferencia() throws Exception {
        Element paragrafo = paragrafoDaJustificacao("Segundo dados oficiais");

        // A referência fica entre "oficiais" e a vírgula que vinha depois da nota no texto
        List<Node> filhos = paragrafo.content();
        int indice = -1;
        for (int i = 0; i < filhos.size(); i++) {
            if ("footnote".equals(filhos.get(i).getName())) {
                indice = i;
            }
        }
        assertThat(filhos.get(indice - 1).getText()).endsWith("Segundo dados oficiais");
        assertThat(filhos.get(indice + 1).getText()).startsWith(", cerca de 3,4% da população");
    }

    @Test
    void corpoDaNotaComFonteMenorEPropriedadesDoParagrafoZeradas() throws Exception {
        Element nota = (Element) transformar(xml(JUSTIFICACAO)).selectSingleNode("//*[local-name()='footnote-body']");
        Element externo = (Element) nota.elements().get(0);
        Element interno = (Element) externo.elements().get(0);

        // Emenda: bloco no tamanho do texto e bloco interno de 0.7em com line-height 1.5em
        assertThat(externo.attributeValue("font-size")).isEqualTo("14pt");
        assertThat(interno.attributeValue("font-size")).isEqualTo("0.7em");
        assertThat(interno.attributeValue("line-height")).isEqualTo("1.5em");
        // Recuos, alinhamento, peso, estilo e decoração herdados do parágrafo são zerados
        assertThat(externo.attributeValue("text-indent")).isEqualTo("0");
        assertThat(externo.attributeValue("start-indent")).isEqualTo("0");
        assertThat(externo.attributeValue("end-indent")).isEqualTo("0");
        assertThat(externo.attributeValue("text-align")).isEqualTo("start");
        assertThat(externo.attributeValue("font-weight")).isEqualTo("normal");
        assertThat(externo.attributeValue("font-style")).isEqualTo("normal");
        assertThat(externo.attributeValue("text-decoration")).isEqualTo("none");
    }

    @Test
    void formatacaoERemissaoDentroDaNota() throws Exception {
        Element paragrafo = paragrafoDaJustificacao("O impacto orçamentário é reduzido");
        Element corpo = (Element) paragrafo.selectSingleNode(".//*[local-name()='footnote-body']");

        assertThat(corpo.selectNodes(".//*[@font-weight='bold' and text()='estimativa']")).hasSize(1);
        assertThat(unicoLink(corpo).attributeValue("external-destination"))
                .isEqualTo("url('https://normas.leg.br/?urn=urn:lex:br:federal:lei.complementar:2000-05-04;101')");
    }

    @Test
    void numeracaoContinuaEntreJustificacoes() throws Exception {
        String justificacoes = "<Justificacao><PartePrincipal><p>Primeira<NotaDeRodape>A.</NotaDeRodape>.</p></PartePrincipal></Justificacao>"
                + "<Justificacao><PartePrincipal><p>Segunda<NotaDeRodape>B.</NotaDeRodape>.</p></PartePrincipal></Justificacao>";
        Document fo = transformar(documento("", "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\">"
                + "<p>Texto.</p></Caput></Artigo>").replace("</Norma></ProjetoNorma>", "</Norma>" + justificacoes + "</ProjetoNorma>"));

        List<String> corpos = new ArrayList<>();
        for (Node corpo : fo.selectNodes("//*[local-name()='footnote-body']")) {
            corpos.add(texto((Element) corpo));
        }
        assertThat(corpos).containsExactly("1 A.", "2 B.");
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

    /** O único fo:basic-link dentro do elemento. */
    private static Element unicoLink(Element elemento) {
        List<Node> links = elemento.selectNodes(".//*[local-name()='basic-link']");
        assertThat(links).hasSize(1);
        return (Element) links.get(0);
    }

    /** Índice do bloco de alteração de norma (margem esquerda de 3cm) entre os blocos da articulação. */
    private static int indiceDaAlteracao(List<Element> blocos) {
        for (int i = 0; i < blocos.size(); i++) {
            if ("3cm".equals(blocos.get(i).attributeValue("margin-left"))) {
                return i;
            }
        }
        throw new AssertionError("Bloco de alteração não encontrado");
    }

    /** Se o bloco tem uma linha pontilhada (fo:leader de pontos). */
    private static boolean temLinhaPontilhada(Element bloco) {
        for (Element filho : bloco.elements()) {
            if ("leader".equals(filho.getName()) && "dots".equals(filho.attributeValue("leader-pattern"))) {
                return true;
            }
        }
        return false;
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

    /** Bloco do corpo da justificação: o último bloco da raiz, depois do título "JUSTIFICAÇÃO". */
    private Element corpoDaJustificacao(String xml) throws DocumentException {
        return corpoDaJustificacao(transformar(xml));
    }

    private static Element corpoDaJustificacao(Document fo) {
        List<Element> blocos = fo.getRootElement().elements();
        assertThat(blocos.get(blocos.size() - 2).getText()).isEqualTo("JUSTIFICAÇÃO");
        return blocos.get(blocos.size() - 1);
    }

    /** Bloco do parágrafo da justificação do documento-com-justificacao que começa com o texto informado. */
    private Element paragrafoDaJustificacao(String inicio) throws DocumentException {
        for (Element paragrafo : corpoDaJustificacao(xml(JUSTIFICACAO)).elements()) {
            if (texto(paragrafo).startsWith(inicio)) {
                return paragrafo;
            }
        }
        throw new AssertionError("Parágrafo da justificação não encontrado: " + inicio);
    }

    /** O fo:inline filho do bloco cujo texto é o informado. */
    private static Element inlineComTexto(Element bloco, String texto) {
        for (Element filho : bloco.elements()) {
            if ("inline".equals(filho.getName()) && texto.equals(filho.getText())) {
                return filho;
            }
        }
        throw new AssertionError("fo:inline não encontrado: " + texto);
    }

    /** Documento LexML mínimo com um artigo e a justificação com o conteúdo de PartePrincipal informado. */
    private static String documentoComJustificacao(String partePrincipal) {
        return documento("", "<Artigo id=\"art1\"><Rotulo>Art. 1º</Rotulo><Caput id=\"art1_cpt\"><p>Texto.</p></Caput></Artigo>")
                .replace("</Norma></ProjetoNorma>", "</Norma><Justificacao><PartePrincipal>" + partePrincipal
                        + "</PartePrincipal></Justificacao></ProjetoNorma>");
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
