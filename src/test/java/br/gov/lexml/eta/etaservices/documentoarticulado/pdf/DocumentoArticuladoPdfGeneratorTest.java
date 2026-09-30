package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
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
    void justificacaoAindaNaoImpressa() throws Exception {
        String texto = textoNormalizado(pdfExemplo);

        assertThat(texto).contains("Art. 1º Fica instituído o Programa de Modernização");
        assertThat(texto).doesNotContain("Esta proposição promove a");
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
