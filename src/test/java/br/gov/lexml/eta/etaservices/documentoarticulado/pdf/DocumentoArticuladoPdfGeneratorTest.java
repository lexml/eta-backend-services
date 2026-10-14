package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO_LONGA;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.REMISSOES_INTERNAS;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoPdfGeneratorTest {

    private static byte[] pdfExemplo;

    @BeforeAll
    static void gerarPdfDoExemplo() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(ConversorDocumentoArticuladoFake.comExemplo()).generate(recurso(JSON_EXEMPLO), out);
        pdfExemplo = out.toByteArray();
    }

    @Test
    void embuteODocumentoArticuladoXmlConvertido() throws Exception {
        Map<String, Anexo> anexos = anexos(pdfExemplo);

        assertThat(anexos).containsOnlyKeys("documento-articulado.xml");
        Anexo anexo = anexos.get("documento-articulado.xml");
        assertThat(anexo.tipo).isEqualTo("text/xml");
        String xml = new String(anexo.conteudo, StandardCharsets.UTF_8);
        assertThat(xml).isEqualTo(recurso(XML_EXEMPLO));
        assertThat(xml).contains("Sala das Sessões").contains("Fica instituído o Programa de Modernização");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            ARTICULACAO_E_ALTERACAO + "|PROJETO DE LEI Nº 999, DE 2026|Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece"
                    + "|O CONGRESSO NACIONAL decreta:|Art. 1º Esta Lei altera a Lei nº 9.394|“Art. 12-A.",
            CAPITULO_E_SECAO + "|MEDIDA PROVISÓRIA Nº 999, DE 2026|Institui o Programa Extraordinário de Reequilíbrio Financeiro"
                    + "|O PRESIDENTE DA REPÚBLICA, no uso da atribuição|Art. 1º Fica instituído o Programa|k) pessoas físicas beneficiárias",
            PENA_E_TITULO_DISPOSITIVO + "|PROJETO DE LEI Nº 999, DE 2026|Tipifica os crimes de desvio de recursos da saúde pública"
                    + "|O CONGRESSO NACIONAL decreta:|Art. 1º Esta Lei tipifica os crimes|A pena será aumentada da metade" })
    void imprimeParteInicialEArticulacaoNaOrdem(String nome, String epigrafe, String ementa, String preambulo,
            String primeiroArtigo, String trechoDaAlteracao) throws Exception {
        String texto = textoNormalizado(gerarPdf(nome));

        // O bloco de alteração de norma vigente vem depois do primeiro artigo
        assertThat(texto).containsSubsequence(epigrafe, ementa, preambulo, primeiroArtigo, trechoDaAlteracao);
    }

    @Test
    void linksExternosParaOPortalNormas() throws Exception {
        List<String> links = links(gerarPdf(PENA_E_TITULO_DISPOSITIVO));

        assertThat(links).contains("uri:https://normas.leg.br/?urn=urn:lex:br:federal:decreto.lei:1940-12-07;2848",
                "uri:https://normas.leg.br/?urn=urn:lex:br:federal:decreto.lei:1940-12-07;2848!art327");
        assertThat(links).allMatch(l -> l.startsWith("uri:https://normas.leg.br/?urn=urn:lex:"));
    }

    @Test
    void linksInternosSoParaAlvosExistentes() throws Exception {
        List<String> links = links(gerarPdf(REMISSOES_INTERNAS));

        // Ementa: um link externo; preâmbulo: nenhum; Art. 2º: art. 1º, § 1º e Capítulo I (o art. 9º não existe)
        assertThat(links).filteredOn(l -> l.startsWith("uri:"))
                .containsExactly("uri:https://normas.leg.br/?urn=urn:lex:br:federal:lei:1990-09-11;8078");
        assertThat(links).filteredOn(l -> l.startsWith("interno:")).hasSize(3).allMatch(l -> l.equals("interno:pagina 1"));
    }

    @Test
    void exemploComLinksExternosEInternos() throws Exception {
        List<String> links = links(pdfExemplo);

        // Ementa, § 2º do Art. 1º, Art. 5º e justificação: links para o portal; Art. 2º e justificação: remissões
        // internas válidas (a do art. 7º, excluído, é coberta no DocumentoArticuladoConteudoTransformerTest); e o
        // link do editor para o portal do Ministério. Um link quebrado em duas linhas gera duas anotações.
        assertThat(links).filteredOn(l -> l.startsWith("uri:")).isNotEmpty()
                .allMatch(l -> l.startsWith("uri:https://normas.leg.br/?urn=urn:lex:") || l.equals("uri:https://www.gov.br/gestao"))
                .contains("uri:https://normas.leg.br/?urn=urn:lex:br:federal:lei:2021-04-01;14133!art5",
                        "uri:https://normas.leg.br/?urn=urn:lex:br:federal:decreto.lei:1940-12-07;2848!art327",
                        "uri:https://www.gov.br/gestao");
        assertThat(links).filteredOn(l -> l.startsWith("interno:")).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void justificacaoImpressaNaVersaoRevisadaDepoisDaArticulacao() throws Exception {
        String texto = textoNormalizado(pdfExemplo);

        // Exemplo da especificação do lexml-eta: <del>reforma</del><ins>modernização</ins>
        assertThat(texto).containsSubsequence("Art. 6º Esta Lei entra em vigor", "JUSTIFICAÇÃO",
                "Esta proposição promove a modernização da gestão pública");
        assertThat(texto).doesNotContain("promove a reforma");
        // Depois da justificação, o local e data e as assinaturas
        assertThat(texto).containsSubsequence("JUSTIFICAÇÃO", "Pelas razões expostas", "Sala das Sessões, 24 de abril de 2026.",
                "Senadora Soraya Thronicke");
    }

    @Test
    void blocoDeAlteracaoComAspasENota() throws Exception {
        String texto = textoNormalizado(gerarPdf(PENA_E_TITULO_DISPOSITIVO));

        assertThat(texto).containsSubsequence("passa a vigorar acrescido do seguinte parágrafo:", "“Art. 327.",
                "§ 3º A pena será aumentada da metade", "praticado contra a administração da saúde pública.” (NR)",
                "Art. 7º");
    }

    @Test
    void tituloDeDispositivoEPenaImpressos() throws Exception {
        byte[] pdf = gerarPdf(PENA_E_TITULO_DISPOSITIVO);

        assertThat(textoNormalizado(pdf)).containsSubsequence(
                "Desvio ou apropriação de recursos e insumos da saúde Art. 2º Desviar",
                "Pena – reclusão, de 4 (quatro) a 12 (doze) anos, e multa. § 1º");
        assertThat(fonteDaLinha(pdf, "Desvio ou apropriação de recursos e insumos da saúde")).containsIgnoringCase("bold");
        assertThat(fonteDaLinha(pdf, "Pena – reclusão, de 4 (quatro) a 12 (doze) anos, e multa.")).doesNotContainIgnoringCase("bold");
    }

    @Test
    void agrupadoresImpressosAntesDosArtigos() throws Exception {
        byte[] pdf = gerarPdf(CAPITULO_E_SECAO);
        String texto = textoNormalizado(pdf);

        assertThat(texto).containsSubsequence("CAPÍTULO I DISPOSIÇÕES PRELIMINARES Art. 1º Fica instituído",
                "CAPÍTULO III DOS PARTICIPANTES DO DESENROLA ADIMPLENTES Seção I Dos beneficiários Art. 4º");
        Map<String, String> fontes = fontesPorPalavra(pdf);
        assertThat(fontes.get("CAPÍTULO")).containsIgnoringCase("bold");
        assertThat(fontes.get("DISPOSIÇÕES")).doesNotContainIgnoringCase("bold");
        assertThat(fontes.get("Seção")).containsIgnoringCase("bold");
        // "beneficiários" aparece antes em texto regular: verifica a linha do nome da seção
        assertThat(fonteDaLinha(pdf, "Dos beneficiários")).containsIgnoringCase("bold");
    }

    @Test
    void rotuloDoArtigoEmNegritoETextoDoCaputRegular() throws Exception {
        Map<String, String> fontes = fontesPorPalavra(gerarPdf(PENA_E_TITULO_DISPOSITIVO));

        assertThat(fontes.get("Art")).containsIgnoringCase("bold");
        assertThat(fontes.get("tipifica")).doesNotContainIgnoringCase("bold");
    }

    @Test
    void preambuloSemNegritoEEpigrafeEmNegrito() throws Exception {
        Map<String, String> fontes = fontesPorPalavra(gerarPdf(CAPITULO_E_SECAO));

        // No documento o preâmbulo tem "<b>O PRESIDENTE DA REPÚBLICA</b>": o negrito é descartado
        assertThat(fontes.get("PRESIDENTE")).doesNotContainIgnoringCase("bold");
        assertThat(fontes.get("PROVISÓRIA")).containsIgnoringCase("bold");
    }

    @Test
    void metadadosXmpComTituloEHash() throws Exception {
        String xmp = xmp(pdfExemplo);

        assertThat(xmp)
                .contains("PROJETO DE LEI Nº , DE")
                .contains("<pdfaid:part>3</pdfaid:part>")
                .contains("<pdfaid:conformance>B</pdfaid:conformance>")
                .contains("<check:hash>")
                .doesNotContain("00000000000000000000000000000000");
        assertThat(xmp).containsPattern("<check:hash>[0-9A-F]{32}</check:hash>");
    }

    @Test
    void conteudoQueNaoEhJson() {
        assertNadaEscritoAoFalhar(ConversorDocumentoArticuladoFake.comExemplo(), "isto não é json", "JSON válido");
    }

    @Test
    void jsonQueNaoEhDocumentoLexml() {
        assertNadaEscritoAoFalhar(ConversorDocumentoArticuladoFake.comExemplo(), "{\"name\":{\"localPart\":\"Outro\"}}", "documento LexML");
        String semUrn = recurso(JSON_EXEMPLO).replace("urn:lex:br:senado.federal:projeto.lei:999999;9999", "");
        assertNadaEscritoAoFalhar(ConversorDocumentoArticuladoFake.comExemplo(), semUrn, "documento LexML");
    }

    // Justificação (issue #75, parte A) -------------------------------------------------------------------------

    @Test
    void justificacaoImpressaDepoisDaArticulacaoNaVersaoRevisada() throws Exception {
        String texto = textoNormalizado(gerarPdf(JUSTIFICACAO));

        assertThat(texto).containsSubsequence("PROJETO DE LEI Nº 123, DE 2026", "O CONGRESSO NACIONAL decreta:",
                "Art. 2º Esta Lei entra em vigor", "JUSTIFICAÇÃO", "A presente proposição institui",
                "a livros digitais acessíveis.", "pela coordenação da política, nos termos do art. 1º desta proposição.",
                "Pelas razões expostas");
        // Exclusões não impressas; no fim, o local e data e a assinatura (uma só: célula nas duas colunas)
        assertThat(texto).doesNotContain("fiscalização").doesNotContain("conforme regulamento")
                .doesNotContain("Nota excluída");
        assertThat(texto).containsSubsequence("Pelas razões expostas", "Sala das Sessões, 7 de outubro de 2026.",
                "Senadora Soraya Thronicke");
        Linha assinatura = linhaQueContem(linhas(gerarPdf(JUSTIFICACAO)), "Senadora Soraya Thronicke");
        assertThat(assinatura.centroDe("Senadora Soraya Thronicke")).isCloseTo(MARGEM_ESQUERDA + LARGURA_DO_TEXTO / 2, offset(1f));
    }

    @Test
    void formatacaoInlineDaJustificacaoNoPdf() throws Exception {
        List<Linha> linhas = linhas(gerarPdf(JUSTIFICACAO));
        Linha linha = linhaQueContem(linhas, "livros digitais acessíveis");

        assertThat(linha.fonteDe("livros")).containsIgnoringCase("bold");
        assertThat(linha.fonteDe("digitais")).containsIgnoringCase("italic");
        assertThat(linha.fonteDe("acessíveis")).doesNotContainIgnoringCase("bold").doesNotContainIgnoringCase("italic");
        // Título em negrito, centralizado e no tamanho de destaque
        Linha titulo = linhaQueContem(linhas, "JUSTIFICAÇÃO");
        assertThat(titulo.fonteDe("JUSTIFICAÇÃO")).containsIgnoringCase("bold");
        assertThat(titulo.tamanho).isEqualTo(16f);
    }

    @Test
    void linksDaJustificacaoENotasNoPdf() throws Exception {
        List<String> links = links(gerarPdf(JUSTIFICACAO));

        assertThat(links).contains("uri:https://www.gov.br/mec",
                "uri:https://normas.leg.br/?urn=urn:lex:br:federal:lei:2015-07-06;13146",
                "uri:https://normas.leg.br/?urn=urn:lex:br:federal:lei.complementar:2000-05-04;101", "interno:pagina 1");
    }

    @Test
    void notasDeRodapeNoRodapeDaPaginaDaReferencia() throws Exception {
        List<Linha> linhas = linhas(gerarPdf(JUSTIFICACAO));

        assertNotaNaPaginaDaReferencia(linhas, 1, "oficiais1", "Fonte: Pesquisa Nacional de Saúde");
        assertNotaNaPaginaDaReferencia(linhas, 2, "existente2", "Ver a estimativa de impacto");
        assertNotaNaPaginaDaReferencia(linhas, 3, "superior3", "Nota simples.");
        assertNotaNaPaginaDaReferencia(linhas, 4, "especial4", "Nota de rodapé em item de lista.");
        assertNotaNaPaginaDaReferencia(linhas, 5, "meses5", "Nota de rodapé em célula de tabela.");
        assertThat(linhas).filteredOn(l -> l.tamanho < 11 && l.texto.startsWith("6 ")).isEmpty();
    }

    @Test
    void notasDeRodapeComNumeracaoContinuaEmVariasPaginas() throws Exception {
        byte[] pdf = gerarPdf(JUSTIFICACAO_LONGA);
        List<Linha> linhas = linhas(pdf);

        String[] referencias = { "desastres1", "desastres2", "canalização3", "fiscal4", "resposta5", "brasileira6" };
        List<Integer> paginas = new ArrayList<>();
        for (int numero = 1; numero <= referencias.length; numero++) {
            paginas.add(assertNotaNaPaginaDaReferencia(linhas, numero, referencias[numero - 1], ""));
        }
        assertThat(paginas).isSorted();
        assertThat(paginas.stream().distinct().count()).as("páginas com notas").isGreaterThanOrEqualTo(2);
        try (PDDocument documento = PDDocument.load(pdf)) {
            assertThat(documento.getNumberOfPages()).isGreaterThanOrEqualTo(3);
        }
    }

    // Listas, tabelas e imagens (issue #75, parte B) ------------------------------------------------------------

    /** Largura da área do texto: A4 (595,275pt) menos as margens de 2,5cm e 2,1cm. */
    private static final float LARGURA_DO_TEXTO = 595.275f - (2.5f + 2.1f) * 72 / 2.54f;
    private static final float MARGEM_ESQUERDA = 2.5f * 72 / 2.54f;

    @Test
    void listasNoPdfComRotulosERecuos() throws Exception {
        byte[] pdf = gerarPdf(JUSTIFICACAO);
        String texto = textoNormalizado(pdf);
        List<Linha> linhas = linhas(pdf);

        assertThat(texto).containsSubsequence("1. Diagnóstico das bibliotecas públicas.", "2. Produção de obras com audiodescrição",
                "1. obras didáticas;", "2. obras literárias.", "3. Distribuição às bibliotecas participantes.",
                "4. Item com recuo do editor", "• Bibliotecas públicas estaduais.", "• Bibliotecas escolares");
        // Lista interna mais recuada que a externa; o item com indent-1 no mesmo nível dos demais
        float itemExterno = linhaQueContem(linhas, "Diagnóstico das bibliotecas públicas.").xDoTexto("Diagnóstico");
        assertThat(linhaQueContem(linhas, "obras didáticas;").xDoTexto("obras")).isGreaterThan(itemExterno + 50);
        assertThat(linhaQueContem(linhas, "Item com recuo do editor").xDoTexto("Item")).isEqualTo(itemExterno);
    }

    @Test
    void tabelasNoPdfComTextoMenorECabecalhoEmNegrito() throws Exception {
        List<Linha> linhas = linhas(gerarPdf(JUSTIFICACAO));

        Linha celula = linhaQueContem(linhas, "Livros convertidos");
        assertThat(celula.tamanho).isLessThan(12f);
        assertThat(linhaQueContem(linhas, "Etapa").fonteDe("Etapa")).containsIgnoringCase("bold");
        assertThat(linhaQueContem(linhas, "Diagnóstico e planejamento").fonteDe("Diagnóstico"))
                .doesNotContainIgnoringCase("bold");
        // A tabela de 60% fica centralizada: começa depois da tabela de largura total
        assertThat(linhaQueContem(linhas, "Etapa").xDoTexto("Etapa")).isGreaterThan(celula.xDoTexto("Livros") + 60);
    }

    @Test
    void sobrescritoNaoMudaAAlturaDaLinha() throws Exception {
        List<Linha> linhas = linhas(gerarPdf(JUSTIFICACAO));

        // Células da mesma linha da tabela alinhadas, mesmo com o número da nota de rodapé em uma delas
        Linha celulaProducao = linhas.stream().filter(l -> l.texto.trim().equals("Produção")).findFirst()
                .orElseThrow(AssertionError::new);
        assertThat(linhaQueContem(linhas, "12 meses").y).isEqualTo(celulaProducao.y);
        // Linha com m² e H₂O à mesma distância da anterior que as demais (entrelinha de 150% de 14pt = 21pt)
        Linha anterior = linhaQueContem(linhas, "Os padrões técnicos de acessibilidade");
        assertThat(linhaQueContem(linhas, "fórmulas, como H").y - anterior.y).isCloseTo(21f, offset(0.5f));
    }

    @Test
    void imagensNoPdfComLarguraEmPorcentagemDaLarguraDoTexto() throws Exception {
        List<float[]> desenhadas = imagensDesenhadas(gerarPdf(JUSTIFICACAO));
        // A primeira imagem é o brasão do cabeçalho (conferido em cabecalhoComBrasaoCasaETexto)
        List<float[]> imagens = desenhadas.subList(1, desenhadas.size());

        // 40% (parágrafo só com a imagem: centralizada), 20% (depois de texto) e sem width (largura total);
        // a imagem com endereço externo não é desenhada
        assertThat(imagens).hasSize(3);
        assertThat(imagens.get(0)[1]).isCloseTo(LARGURA_DO_TEXTO * 0.4f, offset(1f));
        assertThat(imagens.get(0)[0]).isCloseTo(MARGEM_ESQUERDA + LARGURA_DO_TEXTO * 0.3f, offset(1f));
        assertThat(imagens.get(1)[1]).isCloseTo(LARGURA_DO_TEXTO * 0.2f, offset(1f));
        assertThat(imagens.get(1)[0]).isCloseTo(MARGEM_ESQUERDA, offset(1f));
        assertThat(imagens.get(2)[1]).isCloseTo(LARGURA_DO_TEXTO, offset(1f));
    }

    // Cabeçalho (issue #74) -------------------------------------------------------------------------------------

    /** Largura do brasão no cabeçalho da emenda (149 px do brasao.jpg), medida num PDF de emenda: 53,6pt (1,89cm). */
    private static final float LARGURA_DO_BRASAO = 53.64f;

    @Test
    void cabecalhoComBrasaoCasaETexto() throws Exception {
        byte[] pdf = pdfExemplo;
        List<Linha> linhas = linhas(pdf);
        float[] brasao = imagensDesenhadas(pdf).get(0);

        // Brasão na primeira página, centralizado e com o tamanho da emenda
        assertThat(brasao[2]).isEqualTo(1f);
        assertThat(brasao[1]).isCloseTo(LARGURA_DO_BRASAO, offset(0.5f));
        assertThat(brasao[0]).isCloseTo(MARGEM_ESQUERDA + (LARGURA_DO_TEXTO - LARGURA_DO_BRASAO) / 2, offset(0.5f));
        // Casa legislativa em ITC Stone Sans 11pt e texto do cabeçalho, acima da epígrafe
        Linha casa = linhaQueContem(linhas, "SENADO FEDERAL");
        Linha texto = linhaQueContem(linhas, "Gabinete do Senador Davi Alcolumbre");
        Linha epigrafe = linhaQueContem(linhas, "PROJETO DE LEI");
        assertThat(casa.pagina).isEqualTo(1);
        assertThat(casa.fonteDe("SENADO")).containsIgnoringCase("StoneSans");
        assertThat(casa.tamanho).isEqualTo(11f);
        assertThat(casa.y).isLessThan(texto.y);
        assertThat(texto.y).isLessThan(epigrafe.y);
        // Corpo a 4cm do topo da área (1cm sem cabeçalho): epígrafe 3cm mais abaixo que no exemplo sem cabeçalho
        Linha epigrafeSemCabecalho = linhaQueContem(linhas(pdfDoExemploCom("false", "", "true", "14")), "PROJETO DE LEI");
        assertThat(epigrafe.y - epigrafeSemCabecalho.y).isCloseTo(3 * 72 / 2.54f, offset(0.5f));
    }

    @Test
    void cabecalhoSoNaPrimeiraPagina() throws Exception {
        byte[] pdf = pdfExemplo;

        assertThat(imagensDesenhadas(pdf)).filteredOn(i -> Math.abs(i[1] - LARGURA_DO_BRASAO) < 0.5f)
                .extracting(i -> i[2]).containsExactly(1f);
        assertThat(linhas(pdf)).filteredOn(l -> l.texto.contains("SENADO FEDERAL")).extracting(l -> l.pagina).containsExactly(1);
    }

    @Test
    void cabecalhoSoComTextoSemBrasao() throws Exception {
        byte[] pdf = gerarPdf(JUSTIFICACAO_LONGA);
        List<Linha> linhas = linhas(pdf);

        assertThat(imagensDesenhadas(pdf)).isEmpty();
        Linha texto = linhaQueContem(linhas, "Comissão de Desenvolvimento Regional e Turismo");
        assertThat(texto.pagina).isEqualTo(1);
        assertThat(texto.y).isLessThan(linhaQueContem(linhas, "PROJETO DE LEI").y);
        assertThat(linhas).noneMatch(l -> l.texto.contains("SENADO FEDERAL"));
    }

    @Test
    void cabecalhoDaMedidaProvisoriaSoComBrasao() throws Exception {
        byte[] pdf = gerarPdf(CAPITULO_E_SECAO);

        // Documento sem OpcoesImpressao (padrão: com brasão) e autoridade federal (sem casa legislativa)
        assertThat(imagensDesenhadas(pdf)).hasSize(1).allMatch(i -> i[2] == 1f);
        assertThat(textoNormalizado(pdf)).startsWith("MEDIDA PROVISÓRIA").doesNotContain("SENADO FEDERAL")
                .doesNotContain("CONGRESSO NACIONAL").doesNotContain("CÂMARA DOS DEPUTADOS");
    }

    // Tamanho da fonte e "reduzir espaço entre linhas" (issue #74, parte B) ------------------------------------

    @Test
    void fonte18ComEspacoEntreLinhasReduzido() throws Exception {
        List<Linha> linhas = linhas(pdfDoExemploCom("true", "Gabinete do Senador Davi Alcolumbre", "true", "18"));

        assertTamanhosEEntrelinha(linhas, 18f, 18f, 1.2f);
        // Notas de rodapé (0.7em) e células de tabela (80%) proporcionais à fonte do documento
        assertThat(linhas.stream().filter(l -> l.texto.startsWith("1 Anexo I")).findFirst().orElseThrow(AssertionError::new)
                .tamanho).isCloseTo(18f * 0.7f, offset(0.05f));
        assertThat(linhaQueContem(linhas, "Processos digitalizados").tamanho).isCloseTo(18f * 0.8f, offset(0.05f));
        // O cabeçalho não muda com a fonte do documento
        assertThat(linhaQueContem(linhas, "SENADO FEDERAL").tamanho).isEqualTo(11f);
    }

    /** Valores da emenda: destaque de 16pt (18pt com fonte 18) e entrelinha de 150% (120% com "reduzir"). */
    @ParameterizedTest
    @CsvSource({ "16,false,16,1.5", "14,true,16,1.2", "14,false,16,1.5" })
    void tamanhoDaFonteEEspacoEntreLinhasDasOpcoesDeImpressao(String fonte, String reduzir, float destaque, float entrelinha)
            throws Exception {
        assertTamanhosEEntrelinha(linhas(pdfDoExemploCom("true", "Gabinete do Senador Davi Alcolumbre", reduzir, fonte)),
                Float.parseFloat(fonte), destaque, entrelinha);
    }

    @Test
    void espacosFixosDaParteInicialNaoMudamComReduzir() throws Exception {
        // Mesmo tamanho de fonte (o exemplo tem 14 + reduzir): só a entrelinha muda; o espaço de 48pt entre
        // epígrafe e ementa, não
        List<Linha> normal = linhas(pdfDoExemploCom("true", "Gabinete do Senador Davi Alcolumbre", "false", "14"));
        List<Linha> reduzido = linhas(pdfExemplo);
        float distanciaNormal = linhaQueContem(normal, "Institui o Programa").y - linhaQueContem(normal, "PROJETO DE LEI").y;
        float distanciaReduzida = linhaQueContem(reduzido, "Institui o Programa").y - linhaQueContem(reduzido, "PROJETO DE LEI").y;

        // A primeira linha da ementa desce metade da diferença de entrelinha (meio espaçamento acima do texto)
        assertThat(distanciaNormal - distanciaReduzida).isCloseTo(14f * (1.5f - 1.2f) / 2, offset(0.5f));
    }

    /**
     * Epígrafe e título da justificação no tamanho de destaque; caput e parágrafos da justificação no tamanho da
     * fonte, com a distância entre linhas de um mesmo parágrafo igual a fonte x entrelinha.
     */
    private static void assertTamanhosEEntrelinha(List<Linha> linhas, float fonte, float destaque, float entrelinha) {
        assertThat(linhaQueContem(linhas, "PROJETO DE LEI").tamanho).isEqualTo(destaque);
        assertThat(linhas.stream().filter(l -> l.texto.trim().equals("JUSTIFICAÇÃO")).findFirst()
                .orElseThrow(AssertionError::new).tamanho).isEqualTo(destaque);
        assertThat(linhaQueContem(linhas, "Fica instituído o Programa").tamanho).isEqualTo(fonte);
        int indice = linhas.indexOf(linhaQueContem(linhas, "As metas serão revistas anualmente"));
        Linha primeira = linhas.get(indice);
        Linha segunda = linhas.get(indice + 1);
        assertThat(primeira.tamanho).isEqualTo(fonte);
        assertThat(segunda.pagina).isEqualTo(primeira.pagina);
        assertThat(segunda.y - primeira.y).isCloseTo(fonte * entrelinha, offset(0.1f));
    }

    /** PDF do exemplo com outras opções de impressão (no JSON e no XML, registrados no conversor falso). */
    private static byte[] pdfDoExemploCom(String imprimirBrasao, String textoCabecalho, String reduzir, String tamanhoFonte)
            throws IOException {
        String json = recurso(JSON_EXEMPLO).replaceFirst(
                "\"imprimirBrasao\": (true|false),(\\s*)\"textoCabecalho\": \"[^\"]*\",(\\s*)\"reduzirEspacoEntreLinhas\": (true|false),(\\s*)\"tamanhoFonte\": \\d+",
                "\"imprimirBrasao\": " + imprimirBrasao + ",$2\"textoCabecalho\": \"" + textoCabecalho
                        + "\",$3\"reduzirEspacoEntreLinhas\": " + reduzir + ",$5\"tamanhoFonte\": " + tamanhoFonte);
        String xml = recurso(XML_EXEMPLO).replaceFirst(
                "imprimirBrasao=\"(true|false)\" textoCabecalho=\"[^\"]*\" reduzirEspacoEntreLinhas=\"(true|false)\" tamanhoFonte=\"\\d+\"",
                "imprimirBrasao=\"" + imprimirBrasao + "\" textoCabecalho=\"" + textoCabecalho + "\" reduzirEspacoEntreLinhas=\""
                        + reduzir + "\" tamanhoFonte=\"" + tamanhoFonte + "\"");
        assertThat(json).as("opções no JSON do exemplo").contains("\"tamanhoFonte\": " + tamanhoFonte)
                .contains("\"reduzirEspacoEntreLinhas\": " + reduzir);
        assertThat(xml).as("opções no XML do exemplo").contains("tamanhoFonte=\"" + tamanhoFonte + "\"");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(new ConversorDocumentoArticuladoFake().registrar(json, xml)).generate(json, out);
        return out.toByteArray();
    }

    // Alertas e pendências (issue #79) ----------------------------------------------------------------------

    private static final String AVISO_REVISAO = "Este documento contém marcas de revisão e não deve ser protocolado até que estas sejam removidas.";
    private static final String AVISO_COMENTARIOS = "Este documento contém comentários e não deve ser protocolado até que sejam removidos.";
    private static final String AVISO_PENDENCIAS =
            "Este documento contém pendências de preenchimento e não deve ser protocolado até que estas sejam resolvidas:";

    @Test
    void alertasEPendenciasDoExemploAntesDaEpigrafe() throws Exception {
        String texto = textoNormalizado(pdfExemplo);
        List<Linha> linhas = linhas(pdfExemplo);

        // Depois do cabeçalho, as três caixas e as cinco pendências da especificação 13, como registradas
        assertThat(texto).containsSubsequence("Gabinete do Senador Davi Alcolumbre", AVISO_REVISAO, AVISO_COMENTARIOS, AVISO_PENDENCIAS,
                "• Não foi informado um texto de justificação.", "• Corrigir remissões internas inválidas.", "• Remover comentários.",
                "• Resolver marcas de revisão de texto.", "• Resolver marcas de revisão na articulação.", "PROJETO DE LEI");
        // Caixas em negrito e pendências sem negrito, em 12pt, na primeira página, acima da epígrafe
        Linha epigrafe = linhaQueContem(linhas, "PROJETO DE LEI");
        Linha aviso = linhaQueContem(linhas, "Este documento contém marcas de revisão");
        Linha pendencia = linhaQueContem(linhas, "Resolver marcas de revisão na articulação.");
        assertThat(aviso.pagina).isEqualTo(1);
        assertThat(aviso.tamanho).isEqualTo(12f);
        assertThat(aviso.fonteDe("documento")).containsIgnoringCase("bold");
        assertThat(pendencia.tamanho).isEqualTo(12f);
        assertThat(pendencia.fonteDe("Resolver")).doesNotContainIgnoringCase("bold");
        assertThat(pendencia.y).isLessThan(epigrafe.y);
        assertThat(epigrafe.pagina).isEqualTo(1);
    }

    @Test
    void alertasDasFixturesDaJustificacao() throws Exception {
        String justificacao = textoNormalizado(gerarPdf(JUSTIFICACAO));
        String longa = textoNormalizado(gerarPdf(JUSTIFICACAO_LONGA));

        // Revisões e comentários; a lista repete os avisos como o editor registrou
        assertThat(justificacao).containsSubsequence(AVISO_REVISAO, AVISO_COMENTARIOS, AVISO_PENDENCIAS, "• Remover comentários.",
                "• Resolver marcas de revisão de texto.", "PROJETO DE LEI");
        // Só revisões (sem comentários)
        assertThat(longa).containsSubsequence(AVISO_REVISAO, AVISO_PENDENCIAS, "• Resolver marcas de revisão de texto.", "PROJETO DE LEI");
        assertThat(longa).doesNotContain(AVISO_COMENTARIOS);
    }

    @ParameterizedTest
    @ValueSource(strings = { ARTICULACAO_E_ALTERACAO, CAPITULO_E_SECAO, PENA_E_TITULO_DISPOSITIVO, REMISSOES_INTERNAS })
    void documentosSemAlertas(String nome) throws Exception {
        assertThat(textoNormalizado(gerarPdf(nome))).doesNotContain("não deve ser protocolado");
    }

    @Test
    void soPendencias() throws Exception {
        String texto = textoNormalizado(pdfDoExemploComMetadado(metadado -> metadado.remove(
                Arrays.asList("revisoesTextuais", "revisoesArticulacao", "comentarios"))));

        assertThat(texto).doesNotContain(AVISO_REVISAO).doesNotContain(AVISO_COMENTARIOS);
        assertThat(texto).containsSubsequence("Gabinete do Senador Davi Alcolumbre", AVISO_PENDENCIAS,
                "• Não foi informado um texto de justificação.", "PROJETO DE LEI");
    }

    @Test
    void alertasEm12ptComFonte18() throws Exception {
        List<Linha> linhas = linhas(pdfDoExemploCom("true", "Gabinete do Senador Davi Alcolumbre", "true", "18"));

        assertThat(linhaQueContem(linhas, "PROJETO DE LEI").tamanho).isEqualTo(18f);
        assertThat(linhaQueContem(linhas, "Este documento contém marcas de revisão").tamanho).isEqualTo(12f);
        assertThat(linhaQueContem(linhas, "Corrigir remissões internas inválidas.").tamanho).isEqualTo(12f);
    }

    // Local e data e assinaturas (issue #76) -----------------------------------------------------------------

    @Test
    void fechoEAssinaturasDepoisDaJustificacaoUmaUnicaVez() throws Exception {
        String texto = textoNormalizado(pdfExemplo);

        assertThat(texto).containsSubsequence("JUSTIFICAÇÃO", "Pelas razões expostas", "Sala das Sessões, 24 de abril de 2026.",
                "Senador Davi Alcolumbre", "(UNIÃO - AP)", "Presidente do Senado Federal", "Senadora Soraya Thronicke", "(PSB - MS)",
                "Deputado João da Silva", "(PT - SP)", "Líder do Partido dos Trabalhadores", "Deputada Maria Souza", "(PL - RJ)",
                "Senador Pedro Alves", "(MDB - RS)");
        // A representação textual do LexML (ParteFinal) não é impressa
        for (String trecho : new String[] { "Sala das Sessões", "Senadora Soraya Thronicke", "Deputado João da Silva", "Senador Pedro Alves" }) {
            assertThat(texto.split(Pattern.quote(trecho), -1)).as(trecho).hasSize(2);
        }
    }

    @Test
    void fechoComRecuoEAssinaturasEmDuasColunasCentralizadasEmNegrito() throws Exception {
        List<Linha> linhas = linhas(pdfExemplo);
        Linha fecho = linhaQueContem(linhas, "Sala das Sessões, 24 de abril de 2026.");
        float coluna1 = MARGEM_ESQUERDA + LARGURA_DO_TEXTO / 4;
        float coluna2 = MARGEM_ESQUERDA + LARGURA_DO_TEXTO * 3 / 4;
        float meio = MARGEM_ESQUERDA + LARGURA_DO_TEXTO / 2;

        // Fecho com recuo de 2,5cm, na mesma página da primeira assinatura
        assertThat(fecho.xDoTexto("Sala")).isCloseTo(MARGEM_ESQUERDA + 2.5f * 72 / 2.54f, offset(0.5f));
        Linha davi = linhaDepoisDe(linhas, fecho, "Senador Davi Alcolumbre");
        assertThat(davi.pagina).isEqualTo(fecho.pagina);
        assertThat(davi.y).isGreaterThan(fecho.y);
        // Pares lado a lado, cada um centralizado na sua coluna; o quinto, sozinho, no meio
        assertThat(davi.centroDe("Senador Davi Alcolumbre")).isCloseTo(coluna1, offset(1f));
        assertThat(davi.fonteDe("Davi")).containsIgnoringCase("bold");
        Linha soraya = linhaDepoisDe(linhas, fecho, "Senadora Soraya Thronicke");
        assertThat(soraya.y).isEqualTo(davi.y);
        assertThat(soraya.centroDe("Senadora Soraya Thronicke")).isCloseTo(coluna2, offset(1f));
        assertThat(linhaDepoisDe(linhas, fecho, "(UNIÃO - AP)").centroDe("(UNIÃO - AP)")).isCloseTo(coluna1, offset(1f));
        assertThat(linhaDepoisDe(linhas, fecho, "Presidente do Senado Federal").fonteDe("Presidente")).containsIgnoringCase("bold");
        Linha joao = linhaDepoisDe(linhas, fecho, "Deputado João da Silva");
        Linha maria = linhaDepoisDe(linhas, fecho, "Deputada Maria Souza");
        assertThat(joao.y).isEqualTo(maria.y);
        assertThat(maria.centroDe("Deputada Maria Souza")).isCloseTo(coluna2, offset(1f));
        Linha pedro = linhaDepoisDe(linhas, fecho, "Senador Pedro Alves");
        assertThat(pedro.pagina > joao.pagina || pedro.y > joao.y).isTrue();
        assertThat(pedro.centroDe("Senador Pedro Alves")).isCloseTo(meio, offset(1f));
    }

    @Test
    void semDataComEspacosParaPreencherESemPartido() throws Exception {
        String texto = textoNormalizado(gerarPdf(JUSTIFICACAO_LONGA));

        assertThat(texto).containsSubsequence("Sala das Sessões, de de .", "Senador Davi Alcolumbre");
        assertThat(texto).doesNotContain("(UNIÃO - AP)");
    }

    @Test
    void autoriaPorComissaoSoComOFecho() throws Exception {
        String texto = textoNormalizado(pdfDoExemploComMetadado(metadado -> metadado.putObject("autoria").put("tipo", "Comissão")
                .putObject("colegiadoAutor").put("nome", "Comissão de Assuntos Econômicos").put("sigla", "CAE")));

        assertThat(texto).endsWith("Sala das Sessões, 24 de abril de 2026.");
        assertThat(texto).doesNotContain("Senadora Soraya Thronicke").doesNotContain("Comissão de Assuntos Econômicos");
    }

    @Test
    void semLocalSoAsAssinaturas() throws Exception {
        String texto = textoNormalizado(pdfDoExemploComMetadado(metadado -> metadado.remove("local")));

        assertThat(texto).doesNotContain("Sala das Sessões").doesNotContain("24 de abril de 2026");
        assertThat(texto).containsSubsequence("Pelas razões expostas", "Senador Davi Alcolumbre", "Senador Pedro Alves");
    }

    @ParameterizedTest
    @ValueSource(strings = { ARTICULACAO_E_ALTERACAO, CAPITULO_E_SECAO, PENA_E_TITULO_DISPOSITIVO, REMISSOES_INTERNAS })
    void documentosSemLocalNemAutoriaTerminamNoConteudo(String nome) throws Exception {
        String texto = textoNormalizado(gerarPdf(nome));

        assertThat(texto).doesNotContain("Sala das Sessões").doesNotContain("Senador ").doesNotContain("Deputad");
    }

    /** Primeira linha depois de {@code referencia} que contém o trecho. */
    private static Linha linhaDepoisDe(List<Linha> linhas, Linha referencia, String trecho) {
        return linhas.subList(linhas.indexOf(referencia) + 1, linhas.size()).stream().filter(l -> l.texto.contains(trecho))
                .findFirst().orElseThrow(() -> new AssertionError("Linha com '" + trecho + "' não encontrada depois de " + referencia));
    }

    /** PDF do exemplo com o valor do lexedit:Metadado alterado no JSON (o XML é o da fixture: só o JSON é lido aqui). */
    private static byte[] pdfDoExemploComMetadado(Consumer<ObjectNode> alteracao) throws IOException {
        ObjectNode documento = (ObjectNode) new ObjectMapper().readTree(recurso(JSON_EXEMPLO));
        alteracao.accept((ObjectNode) documento.path("value").path("metadado").path("metadadoProprietario").path(0).path("any").path(0).path("value"));
        String json = documento.toString();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(new ConversorDocumentoArticuladoFake().registrar(json, recurso(XML_EXEMPLO))).generate(json, out);
        return out.toByteArray();
    }

    @Test
    void pdfGeradoComImagemExternaSemCarregaLa() throws Exception {
        String texto = textoNormalizado(gerarPdf(JUSTIFICACAO));

        assertThat(texto).contains("Imagem externa, não carregada:").contains("Pelas razões expostas");
    }

    @Test
    void falhaNaConversaoParaXml() {
        assertNadaEscritoAoFalhar(ConversorDocumentoArticuladoFake.comExemplo().falharCom("Element [invalido] is not known"),
                recurso(JSON_EXEMPLO), "Element [invalido] is not known");
    }

    private static byte[] gerarPdf(String nome) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(ConversorDocumentoArticuladoFake.comDocumentosDeTeste())
                .generate(ConversorDocumentoArticuladoFake.json(nome), out);
        return out.toByteArray();
    }

    /** Texto extraído do PDF com espaços (inclusive não quebráveis) e quebras de linha normalizados. */
    private static String textoNormalizado(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            assertThat(documento.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            return new PDFTextStripper().getText(documento).replace(' ', ' ').replaceAll("\\s+", " ").trim();
        }
    }

    /**
     * Anotações de link do PDF, na ordem das páginas: "uri:&lt;URL&gt;" para links externos e
     * "interno:pagina &lt;n&gt;" para links internos (destino resolvido para a página, 1-based).
     */
    private static List<String> links(byte[] pdf) throws IOException {
        List<String> links = new ArrayList<>();
        try (PDDocument documento = PDDocument.load(pdf)) {
            for (PDPage pagina : documento.getPages()) {
                for (PDAnnotation anotacao : pagina.getAnnotations()) {
                    if (!(anotacao instanceof PDAnnotationLink)) {
                        continue;
                    }
                    PDAnnotationLink link = (PDAnnotationLink) anotacao;
                    PDAction acao = link.getAction();
                    PDDestination destino = link.getDestination();
                    if (acao instanceof PDActionURI) {
                        links.add("uri:" + ((PDActionURI) acao).getURI());
                        continue;
                    }
                    if (acao instanceof PDActionGoTo) {
                        destino = ((PDActionGoTo) acao).getDestination();
                    }
                    if (destino instanceof PDNamedDestination) {
                        destino = documento.getDocumentCatalog().findNamedDestinationPage((PDNamedDestination) destino);
                    }
                    if (destino instanceof PDPageDestination) {
                        PDPageDestination pageDestination = (PDPageDestination) destino;
                        int indice = pageDestination.getPage() != null ? documento.getPages().indexOf(pageDestination.getPage())
                                : pageDestination.getPageNumber();
                        links.add("interno:pagina " + (indice + 1));
                    } else {
                        links.add("desconhecido:" + (acao == null ? destino : acao.getType()));
                    }
                }
            }
        }
        return links;
    }

    /** Nome da fonte do primeiro caractere da primeira linha do PDF que é exatamente o texto informado. */
    private static String fonteDaLinha(byte[] pdf, String linha) throws IOException {
        List<String> fontes = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String texto, List<TextPosition> posicoes) throws IOException {
                if (fontes.isEmpty() && texto.trim().equals(linha)) {
                    fontes.add(posicoes.get(0).getFont().getName());
                }
                super.writeString(texto, posicoes);
            }
        };
        try (PDDocument documento = PDDocument.load(pdf)) {
            stripper.getText(documento);
        }
        assertThat(fontes).as("linha '%s' no PDF", linha).isNotEmpty();
        return fontes.get(0);
    }

    /** Nome da fonte usada no primeiro caractere de cada palavra do PDF. */
    private static Map<String, String> fontesPorPalavra(byte[] pdf) throws IOException {
        Map<String, String> fontes = new LinkedHashMap<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String texto, List<TextPosition> posicoes) throws IOException {
                String[] palavras = texto.replace(' ', ' ').split(" ");
                int inicio = 0;
                for (String palavra : palavras) {
                    if (!palavra.isEmpty() && inicio < posicoes.size()) {
                        fontes.putIfAbsent(palavra.replaceAll("[,.:;]$", ""), posicoes.get(inicio).getFont().getName());
                    }
                    inicio += palavra.length() + 1;
                }
                super.writeString(texto, posicoes);
            }
        };
        try (PDDocument documento = PDDocument.load(pdf)) {
            stripper.getText(documento);
        }
        return fontes;
    }

    /** Linha de texto do PDF, com a página (1-based), a posição vertical (de cima para baixo), o tamanho e as fontes. */
    static class Linha {
        final int pagina;
        final float y;
        final float tamanho;
        final String texto;
        final List<String> fontes = new ArrayList<>();
        final List<Float> xs = new ArrayList<>();
        final List<Float> larguras = new ArrayList<>();

        Linha(int pagina, String texto, List<TextPosition> posicoes) {
            this.pagina = pagina;
            this.texto = texto.replace(' ', ' ');
            this.y = posicoes.get(0).getYDirAdj();
            // Pela matriz do texto: getFontSizeInPt() trunca para inteiro (12,6pt viraria 12pt)
            this.tamanho = Math.round(posicoes.get(0).getTextMatrix().getScalingFactorX() * 100) / 100f;
            for (TextPosition posicao : posicoes) {
                fontes.add(posicao.getFont().getName());
                xs.add(posicao.getXDirAdj());
                larguras.add(posicao.getWidthDirAdj());
            }
        }

        /** Centro horizontal (pt) do trecho na linha: meio entre o início do primeiro e o fim do último caractere. */
        float centroDe(String trecho) {
            int inicio = texto.indexOf(trecho);
            assertThat(inicio).as("'%s' na linha '%s'", trecho, texto).isNotNegative();
            int fim = Math.min(inicio + trecho.length() - 1, xs.size() - 1);
            return (xs.get(inicio) + xs.get(fim) + larguras.get(fim)) / 2;
        }

        /** Posição horizontal (pt) do primeiro caractere da palavra na linha. */
        float xDoTexto(String palavra) {
            int indice = texto.indexOf(palavra);
            assertThat(indice).as("'%s' na linha '%s'", palavra, texto).isNotNegative();
            return xs.get(Math.min(indice, xs.size() - 1));
        }

        /** Fonte do primeiro caractere da palavra na linha. */
        String fonteDe(String palavra) {
            int indice = texto.indexOf(palavra);
            assertThat(indice).as("'%s' na linha '%s'", palavra, texto).isNotNegative();
            return fontes.get(Math.min(indice, fontes.size() - 1));
        }

        @Override
        public String toString() {
            return "p" + pagina + " y=" + y + " " + tamanho + "pt: " + texto;
        }
    }

    private static List<Linha> linhas(byte[] pdf) throws IOException {
        List<Linha> linhas = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String texto, List<TextPosition> posicoes) throws IOException {
                linhas.add(new Linha(getCurrentPageNo(), texto, posicoes));
                super.writeString(texto, posicoes);
            }
        };
        try (PDDocument documento = PDDocument.load(pdf)) {
            stripper.getText(documento);
        }
        return linhas;
    }

    /**
     * Imagens desenhadas no PDF, na ordem das páginas: {x, largura, página (1-based), y} em pontos, a partir da
     * matriz de transformação vigente no operador "Do" de cada imagem (y a partir da base da página).
     */
    private static List<float[]> imagensDesenhadas(byte[] pdf) throws IOException {
        List<float[]> imagens = new ArrayList<>();
        int[] pagina = { 0 };
        PDFStreamEngine motor = new PDFStreamEngine() {
            {
                addOperator(new Concatenate());
                addOperator(new DrawObject());
                addOperator(new Save());
                addOperator(new Restore());
                addOperator(new SetMatrix());
            }

            @Override
            protected void processOperator(Operator operador, List<COSBase> operandos) throws IOException {
                if ("Do".equals(operador.getName())
                        && getResources().getXObject((COSName) operandos.get(0)) instanceof PDImageXObject) {
                    Matrix matriz = getGraphicsState().getCurrentTransformationMatrix();
                    imagens.add(new float[] { matriz.getTranslateX(), matriz.getScalingFactorX(), pagina[0], matriz.getTranslateY() });
                }
                super.processOperator(operador, operandos);
            }
        };
        try (PDDocument documento = PDDocument.load(pdf)) {
            for (PDPage pagina1 : documento.getPages()) {
                pagina[0]++;
                motor.processPage(pagina1);
            }
        }
        return imagens;
    }

    private static Linha linhaQueContem(List<Linha> linhas, String trecho) {
        return linhas.stream().filter(l -> l.texto.contains(trecho)).findFirst()
                .orElseThrow(() -> new AssertionError("Linha com '" + trecho + "' não encontrada em " + linhas));
    }

    /**
     * Verifica que o texto da nota (linha em fonte menor iniciada por "número espaço") está na mesma página da
     * referência (palavra seguida do número sobrescrito, ex.: "oficiais1") e abaixo de todo o texto da página.
     * Devolve a página.
     */
    private static int assertNotaNaPaginaDaReferencia(List<Linha> linhas, int numero, String referencia, String inicioDaNota) {
        Linha nota = linhas.stream().filter(l -> l.tamanho < 11 && l.texto.startsWith(numero + " " + inicioDaNota))
                .findFirst().orElseThrow(() -> new AssertionError("Nota " + numero + " não encontrada em " + linhas));
        Linha linhaDaReferencia = linhaQueContem(linhas, referencia);

        assertThat(nota.pagina).as("página da nota %d", numero).isEqualTo(linhaDaReferencia.pagina);
        float fimDoTexto = (float) linhas.stream().filter(l -> l.pagina == nota.pagina && l.tamanho >= 14)
                .mapToDouble(l -> l.y).max().orElse(0);
        assertThat(nota.y).as("nota %d abaixo do texto da página", numero).isGreaterThan(fimDoTexto);
        return nota.pagina;
    }

    private static void assertNadaEscritoAoFalhar(ConversorDocumentoArticuladoFake conversor, String json, String mensagem) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThatThrownBy(() -> new DocumentoArticuladoPdfGenerator(conversor).generate(json, out))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining(mensagem);
        assertThat(out.size()).isZero();
    }

    static String xmp(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            PDMetadata metadata = documento.getDocumentCatalog().getMetadata();
            return IOUtils.toString(metadata.exportXMPMetadata(), StandardCharsets.UTF_8);
        }
    }

    static class Anexo {
        final String tipo;
        final byte[] conteudo;

        Anexo(PDEmbeddedFile arquivo) throws IOException {
            this.tipo = arquivo.getSubtype();
            this.conteudo = arquivo.toByteArray();
        }
    }

    /** Anexos do PDF, lidos antes de fechar o documento. */
    static Map<String, Anexo> anexos(byte[] pdf) throws IOException {
        Map<String, Anexo> anexos = new LinkedHashMap<>();
        try (PDDocument documento = PDDocument.load(pdf)) {
            Map<String, PDComplexFileSpecification> especificacoes = new LinkedHashMap<>();
            coletar(new PDDocumentNameDictionary(documento.getDocumentCatalog()).getEmbeddedFiles(), especificacoes);
            for (Map.Entry<String, PDComplexFileSpecification> e : especificacoes.entrySet()) {
                anexos.put(e.getKey(), new Anexo(e.getValue().getEmbeddedFile()));
            }
        }
        return anexos;
    }

    private static void coletar(PDNameTreeNode<PDComplexFileSpecification> no, Map<String, PDComplexFileSpecification> anexos) throws IOException {
        if (no == null) {
            return;
        }
        Map<String, PDComplexFileSpecification> nomes = no.getNames();
        if (nomes != null) {
            anexos.putAll(nomes);
        }
        List<PDNameTreeNode<PDComplexFileSpecification>> filhos = no.getKids();
        if (filhos != null) {
            for (PDNameTreeNode<PDComplexFileSpecification> filho : filhos) {
                coletar(filho, anexos);
            }
        }
    }

}
