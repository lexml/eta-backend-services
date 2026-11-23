package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class DocumentoArticuladoFoDocxTest {

    @Test
    void pacoteMinimoContemPartesObrigatorias() throws IOException {
        Map<String, byte[]> zip = gerarZip("<fo:block>Texto simples</fo:block>", false);

        assertThat(zip).containsKeys("[Content_Types].xml", "_rels/.rels", "word/document.xml",
                "word/styles.xml", "word/settings.xml", "word/_rels/document.xml.rels");
        assertThat(texto(zip, "word/document.xml")).contains("Texto simples");
    }

    @Test
    void cabecalhoVaiParaHeader1ComTitlePg() throws IOException {
        Map<String, byte[]> zip = gerarZip("<fo:block>Corpo</fo:block>", true);

        assertThat(zip).containsKey("word/header1.xml");
        assertThat(texto(zip, "word/header1.xml")).contains("SENADO FEDERAL").contains("Gabinete");
        assertThat(texto(zip, "word/document.xml")).contains("w:titlePg").contains("w:type=\"first\"");
        assertThat(texto(zip, "[Content_Types].xml")).contains("/word/header1.xml");
    }

    @Test
    void caixaDePendenciasUsaFundoAmareloETextoVermelho() throws IOException {
        String fo = "<fo:block background-color=\"yellow\" color=\"red\" font-size=\"12pt\" font-weight=\"bold\" "
                + "padding=\".1cm\" space-after=\".3cm\">Este documento contém pendências:"
                + "<fo:list-block font-weight=\"normal\" margin-left=\".5cm\" provisional-distance-between-starts=\"1em\">"
                + "<fo:list-item><fo:list-item-label><fo:block>&#8226;</fo:block></fo:list-item-label>"
                + "<fo:list-item-body><fo:block>Resolver comentário.</fo:block></fo:list-item-body></fo:list-item>"
                + "</fo:list-block></fo:block>";

        Map<String, byte[]> zip = gerarZip(fo, false);
        String doc = texto(zip, "word/document.xml");

        assertThat(doc).contains("w:fill=\"FFFF00\"").contains("w:val=\"FF0000\"")
                .contains("Este documento contém pendências:").contains("Resolver comentário.");
    }

    @Test
    void notaDeRodapeNativaSemNumeroDuplicado() throws IOException {
        String fo = "<fo:block>Texto com nota<fo:footnote>"
                + "<fo:inline baseline-shift=\"super\" font-size=\"0.7em\">1</fo:inline>"
                + "<fo:footnote-body><fo:block font-size=\"14pt\"><fo:block font-size=\"0.7em\">1 Conteúdo da nota.</fo:block></fo:block></fo:footnote-body>"
                + "</fo:footnote>.</fo:block>";

        Map<String, byte[]> zip = gerarZip(fo, false);

        assertThat(zip).containsKey("word/footnotes.xml");
        assertThat(texto(zip, "word/document.xml")).contains("w:footnoteReference");
        String footnotes = texto(zip, "word/footnotes.xml");
        assertThat(footnotes).contains("w:footnoteRef").contains("Conteúdo da nota.").doesNotContain("1 Conteúdo da nota.");
    }

    @Test
    void linksExternosERemissoesInternas() throws IOException {
        String fo = "<fo:block id=\"art1\">Art. 1º Veja <fo:basic-link external-destination=\"url('https://normas.leg.br/?urn=urn:lex:br:federal:lei:2020;1')\" color=\"#808080\">Lei 1</fo:basic-link>"
                + " e <fo:basic-link internal-destination=\"art1\" color=\"#808080\">este artigo</fo:basic-link>.</fo:block>";

        Map<String, byte[]> zip = gerarZip(fo, false);
        String doc = texto(zip, "word/document.xml");
        String rels = texto(zip, "word/_rels/document.xml.rels");

        assertThat(rels).contains("Target=\"https://normas.leg.br/?urn=urn:lex:br:federal:lei:2020;1\"")
                .contains("TargetMode=\"External\"").doesNotContain("url(");
        assertThat(doc).contains("w:bookmarkStart").contains("w:name=\"art1\"").contains("w:anchor=\"art1\"");
    }

    @Test
    void omissisUsaTabStopPontilhado() throws IOException {
        String fo = "<fo:block>“<fo:leader leader-pattern=\"dots\" leader-length.optimum=\"100%\"/>”</fo:block>";

        Map<String, byte[]> zip = gerarZip(fo, false);
        String doc = texto(zip, "word/document.xml");

        assertThat(doc).contains("w:leader=\"dot\"").contains("<w:tab/>");
    }

    @Test
    void tabelaCentralizadaComColspanERowspan() throws IOException {
        String fo = "<fo:table table-layout=\"fixed\" width=\"100%\">"
                + "<fo:table-column column-width=\"proportional-column-width(1)\"/>"
                + "<fo:table-column column-width=\"80%\"/>"
                + "<fo:table-column column-width=\"proportional-column-width(1)\"/>"
                + "<fo:table-body><fo:table-row><fo:table-cell column-number=\"2\"><fo:block>"
                + "<fo:table width=\"100%\" border=\"1pt solid\">"
                + "<fo:table-column column-width=\"proportional-column-width(1)\"/>"
                + "<fo:table-column column-width=\"proportional-column-width(1)\"/>"
                + "<fo:table-body>"
                + "<fo:table-row><fo:table-cell number-columns-spanned=\"2\" border=\"1pt solid\"><fo:block>Título</fo:block></fo:table-cell></fo:table-row>"
                + "<fo:table-row><fo:table-cell number-rows-spanned=\"2\" border=\"1pt solid\"><fo:block>A</fo:block></fo:table-cell>"
                + "<fo:table-cell border=\"1pt solid\"><fo:block>B1</fo:block></fo:table-cell></fo:table-row>"
                + "<fo:table-row><fo:table-cell border=\"1pt solid\"><fo:block>B2</fo:block></fo:table-cell></fo:table-row>"
                + "</fo:table-body></fo:table>"
                + "</fo:block></fo:table-cell></fo:table-row></fo:table-body></fo:table>";

        Map<String, byte[]> zip = gerarZip(fo, false);
        String doc = texto(zip, "word/document.xml");

        assertThat(doc).contains("w:jc w:val=\"center\"")
                .contains("w:gridSpan w:val=\"2\"")
                .contains("w:vMerge w:val=\"restart\"")
                .contains("<w:vMerge/>")
                .contains("Título").contains("B1").contains("B2");
    }

    @Test
    void imagensPngEJpegSaoEmbutidasNativamenteEExternaViraAviso() throws IOException {
        String png = dataUri("png", Color.BLUE, "png");
        String jpg = dataUri("jpeg", Color.GREEN, "jpg");
        String fo = "<fo:block><fo:external-graphic src=\"url('" + png + "')\" width=\"50%\"/>"
                + "<fo:external-graphic src=\"url('" + jpg + "')\" width=\"50%\"/>"
                + "<fo:external-graphic src=\"url('https://exemplo.org/fora.png')\"/></fo:block>";

        Map<String, byte[]> zip = gerarZip(fo, false);

        assertThat(zip).containsKeys("word/media/image1.png", "word/media/image2.jpg");
        String doc = texto(zip, "word/document.xml");
        assertThat(doc).contains("w:drawing").contains(DocumentoArticuladoFoDocx.AVISO_IMAGEM).doesNotContain("exemplo.org");
    }

    private static Map<String, byte[]> gerarZip(String corpoFo, boolean comCabecalho) throws IOException {
        String cabecalho = comCabecalho
                ? "<fo:static-content flow-name=\"header-principal\">"
                        + "<fo:block text-align=\"center\">SENADO FEDERAL</fo:block>"
                        + "<fo:block text-align=\"center\">Gabinete</fo:block>"
                        + "</fo:static-content>"
                : "";
        String fo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
                + "<fo:page-sequence master-reference=\"document\">"
                + cabecalho
                + "<fo:flow flow-name=\"xsl-region-body\" font-size=\"14pt\">"
                + corpoFo
                + "</fo:flow></fo:page-sequence></fo:root>";

        byte[] docx = DocumentoArticuladoFoDocx.gerar(fo, ParametrosImpressaoDocumentoArticulado.padrao());
        return descompactar(docx);
    }

    static Map<String, byte[]> descompactar(byte[] docx) throws IOException {
        Map<String, byte[]> entradas = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docx))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int lidos;
                while ((lidos = zis.read(buf)) >= 0) {
                    baos.write(buf, 0, lidos);
                }
                entradas.put(entry.getName(), baos.toByteArray());
            }
        }
        return entradas;
    }

    static String texto(Map<String, byte[]> zip, String entrada) {
        return new String(zip.get(entrada), StandardCharsets.UTF_8);
    }

    private static String dataUri(String subtipo, Color cor, String formato) throws IOException {
        BufferedImage img = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(cor);
        g.fillRect(0, 0, 20, 10);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, formato, out);
        return "data:image/" + subtipo + ";base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

}
