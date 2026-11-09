package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFopProcessor.AnexoLido;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGeneratorTest.Anexo;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoFopProcessorTest {

    private static final String PLACEHOLDER_HASH = "<check:hash>00000000000000000000000000000000";

    private static String xslFo;
    private static String xml;
    private static int paginasDoDocumento;

    private final DocumentoArticuladoFopProcessor processador = new DocumentoArticuladoFopProcessor();

    @BeforeAll
    static void montarXslFo() throws IOException {
        JsonNode documento = new ObjectMapper().readTree(recurso(JSON_EXEMPLO));
        xml = recurso(XML_EXEMPLO);
        ParametrosImpressaoDocumentoArticulado parametros =
                ParametrosImpressaoDocumentoArticulado.de(OpcoesImpressaoDocumentoArticulado.de(documento));
        String conteudoFo = new DocumentoArticuladoConteudoTransformer().transformar(xml, parametros, true);
        xslFo = new DocumentoArticuladoTemplateProcessor().processar(documento, parametros, conteudoFo);
        paginasDoDocumento = paginas(new DocumentoArticuladoFopProcessor().gerarPdf(xslFo, xml));
    }

    @Test
    void semAnexosGeraOMesmoPdfDeAntes() throws IOException {
        byte[] pdf = processador.gerarPdf(xslFo, xml, Collections.emptyList());

        assertThat(paginas(pdf)).isEqualTo(paginasDoDocumento);
        assertThat(DocumentoArticuladoPdfGeneratorTest.anexos(pdf)).containsOnlyKeys("documento-articulado.xml");
    }

    @Test
    void anexoImpressoEhEmbutidoEConcatenado() throws IOException {
        byte[] anexoA = arquivo("anexoA.pdf");

        byte[] pdf = processador.gerarPdf(xslFo, xml, Arrays.asList(new AnexoLido("anexoA.pdf", anexoA, true)));

        assertThat(paginas(pdf)).isEqualTo(paginasDoDocumento + paginas(anexoA));
        Map<String, Anexo> anexos = DocumentoArticuladoPdfGeneratorTest.anexos(pdf);
        assertThat(anexos).containsOnlyKeys("documento-articulado.xml", "anexoA.pdf");
        assertThat(anexos.get("anexoA.pdf").conteudo).isEqualTo(anexoA);
        assertThat(anexos.get("anexoA.pdf").tipo).isEqualTo("application/pdf");
    }

    @Test
    void anexosImpressosSaoConcatenadosNaOrdemRecebidaEOsNaoImpressosSoEmbutidos() throws IOException {
        byte[] anexoA = arquivo("anexoA.pdf");
        byte[] anexoB = arquivo("anexoB.pdf");
        byte[] anexoC = arquivo("anexoA.pdf");

        byte[] pdf = processador.gerarPdf(xslFo, xml, Arrays.asList(
                new AnexoLido("anexoA.pdf", anexoA, true),
                new AnexoLido("anexoB.pdf", anexoB, false),
                new AnexoLido("anexoC.pdf", anexoC, true)));

        assertThat(paginas(pdf)).isEqualTo(paginasDoDocumento + paginas(anexoA) + paginas(anexoC));
        assertThat(DocumentoArticuladoPdfGeneratorTest.anexos(pdf))
                .containsOnlyKeys("documento-articulado.xml", "anexoA.pdf", "anexoB.pdf", "anexoC.pdf");
    }

    @Test
    void paginasDosAnexosImpressosSeguemAOrdemRecebida() throws IOException {
        byte[] anexoA = arquivo("anexoA.pdf");
        byte[] anexoB = arquivo("anexoB.pdf");

        byte[] pdf = processador.gerarPdf(xslFo, xml, Arrays.asList(
                new AnexoLido("anexoB.pdf", anexoB, true), new AnexoLido("anexoA.pdf", anexoA, true)));

        int paginasB = paginas(anexoB);
        assertThat(texto(pdf, paginasDoDocumento + 1)).isEqualTo(texto(anexoB, 1));
        assertThat(texto(pdf, paginasDoDocumento + paginasB + 1)).isEqualTo(texto(anexoA, 1));
    }

    @Test
    void anexoNaoPdfSoEmbutidoNaoEhValidado() throws IOException {
        byte[] docx = arquivo("anexoB.docx");

        byte[] pdf = processador.gerarPdf(xslFo, xml, Arrays.asList(new AnexoLido("anexoB.docx", docx, false)));

        assertThat(paginas(pdf)).isEqualTo(paginasDoDocumento);
        Map<String, Anexo> anexos = DocumentoArticuladoPdfGeneratorTest.anexos(pdf);
        assertThat(anexos.get("anexoB.docx").conteudo).isEqualTo(docx);
        assertThat(anexos.get("anexoB.docx").tipo)
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void modeloEMetadadosPreservadosAposConcatenar() throws IOException {
        byte[] pdf = processador.gerarPdf(xslFo, xml, Arrays.asList(new AnexoLido("anexoA.pdf", arquivo("anexoA.pdf"), true)));

        Anexo modelo = DocumentoArticuladoPdfGeneratorTest.anexos(pdf).get("documento-articulado.xml");
        assertThat(new String(modelo.conteudo, StandardCharsets.UTF_8)).isEqualTo(xml);
        assertThat(DocumentoArticuladoPdfGeneratorTest.xmp(pdf)).contains(PLACEHOLDER_HASH);
    }

    @Test
    void anexoImpressoQueNaoEPdfFalhaComONomeDoAnexo() {
        byte[] naoPdf = "isto não é um PDF".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> processador.gerarPdf(xslFo, xml, Arrays.asList(new AnexoLido("falso.pdf", naoPdf, true))))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("falso.pdf");
    }

    @Test
    void gerarRtfDevolveUmDocumentoRtf() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).startsWith("{\\rtf");
        assertThat(rtf).doesNotContain(PLACEHOLDER_HASH);
    }

    @Test
    void gerarRtfComXslFoInvalidoFalha() {
        assertThatThrownBy(() -> processador.gerarRtf("isto não é XML"))
                .isInstanceOf(EtaBackendException.class);
    }

    @Test
    void gerarRtfOmiteImagemPngComAvisoEMantemJpeg() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).doesNotContain("does not support").doesNotContain("iVBORw0KGgo");
        assertThat(rtf).contains("Imagem omitida").contains("\\pict");
    }

    @Test
    void gerarRtfEscreveLinksExternosSemOWrapperUrl() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).contains("HYPERLINK \"https://normas.leg.br/?urn=").doesNotContain("url('");
    }

    @Test
    void gerarRtfDaTabelasComLarguraDeColunaEmVezDeZero() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).contains("\\trowd").contains("\\cellx");
        assertThat(rtf).doesNotContainPattern("\\\\cellx0(?!\\d)");
    }

    @Test
    void gerarRtfAplicaAEntrelinhaDoPdfAoTextoEAosParagrafosReiniciados() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        // 150% do corpo no PDF = 1,5 / 1,15 de linha simples no Word
        assertThat(rtf).contains("\\sectd \\sl313\\slmult1 ");
        assertThat(rtf.split("\\\\pard", -1).length - 1).isEqualTo(rtf.split("\\\\pard \\\\sl313\\\\slmult1 ", -1).length - 1);
    }

    @Test
    void gerarRtfUsaFonteRomanaComAlternativaSerifada() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).containsPattern("\\\\f\\d+\\\\froman\\\\fcharset0 Gentium Basic\\{\\\\\\*\\\\falt Georgia\\};");
    }

    @Test
    void gerarRtfEscreveNotasDeRodapeNoRodapeDaPaginaEComNumeroSobrescritoLegivel() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        assertThat(rtf).contains("\\footnote").doesNotContain("\\ftnalt");
        // sem o 0.7em do PDF: o Word j\u00e1 reduz o sobrescrito
        assertThat(rtf).doesNotContainPattern("\\{\\\\super[^}]*\\\\fs19");
    }

    @Test
    void gerarRtfInclueOBrasaoECabecalhoAntesDasPendencias() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        // o exemplo tem bras\u00e3o e a imagem JPEG da justifica\u00e7\u00e3o
        assertThat(rtf.split("\\\\jpegblip", -1).length - 1).isEqualTo(2);
        assertThat(rtf.indexOf("Gabinete do Senador Davi Alcolumbre")).isPositive()
                .isLessThan(rtf.indexOf("Este documento cont"));
        assertThat(rtf.indexOf("\\jpegblip")).isLessThan(rtf.indexOf("Este documento cont"));
    }

    @Test
    void gerarRtfSeparaOCabecalhoDoRestoComEspacoNoUltimoParagrafo() {
        String rtf = new String(processador.gerarRtf(xslFo), StandardCharsets.ISO_8859_1);

        int texto = rtf.indexOf("Gabinete do Senador Davi Alcolumbre");
        // 28pt = 560 twips, no par\u00e1grafo do texto do cabe\u00e7alho
        assertThat(rtf.substring(Math.max(0, texto - 120), texto)).contains("\\sa560");
    }

    @Test
    void gerarRtfSemBrasaoQuandoImprimirBrasaoEhFalso() {
        DocumentoArticuladoFoGerador gerador = new DocumentoArticuladoFoGerador(
                br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.comDocumentosDeTeste());
        String json = br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake
                .json(br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO_LONGA);
        DocumentoArticuladoFoGerador.FoGerado fo = gerador.gerar(json, gerador.validar(json));

        String rtf = new String(processador.gerarRtf(fo.xslFo, fo.parametros), StandardCharsets.ISO_8859_1);

        assertThat(rtf).doesNotContain("\\jpegblip");
    }

    @Test
    void adaptacaoParaRtfNaoAlteraOPdf() throws IOException {
        processador.gerarRtf(xslFo);

        assertThat(paginas(processador.gerarPdf(xslFo, xml))).isEqualTo(paginasDoDocumento);
    }

    private static int paginas(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            return documento.getNumberOfPages();
        }
    }

    private static String texto(byte[] pdf, int pagina) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            PDFTextStripper extrator = new PDFTextStripper();
            extrator.setStartPage(pagina);
            extrator.setEndPage(pagina);
            return extrator.getText(documento);
        }
    }

    private static byte[] arquivo(String nome) throws IOException {
        try (InputStream in = DocumentoArticuladoFopProcessorTest.class.getResourceAsStream("/" + nome)) {
            return IOUtils.toByteArray(in);
        }
    }

}
