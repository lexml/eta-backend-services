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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
        // Local e data e assinaturas ainda não impressos
        assertThat(texto).doesNotContain("Sala das Sessões").doesNotContain("Senadora Soraya Thronicke");
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
        // Exclusões não impressas; listas, tabelas, local e data e assinaturas ainda não
        assertThat(texto).doesNotContain("fiscalização").doesNotContain("conforme regulamento")
                .doesNotContain("Nota excluída").doesNotContain("Item de lista").doesNotContain("Indicador")
                .doesNotContain("Sala das Sessões").doesNotContain("Senadora Soraya Thronicke");
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
        assertThat(linhas).filteredOn(l -> l.texto.startsWith("4 ")).isEmpty();
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

        Linha(int pagina, String texto, List<TextPosition> posicoes) {
            this.pagina = pagina;
            this.texto = texto.replace(' ', ' ');
            this.y = posicoes.get(0).getYDirAdj();
            this.tamanho = posicoes.get(0).getFontSizeInPt();
            for (TextPosition posicao : posicoes) {
                fontes.add(posicao.getFont().getName());
            }
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
