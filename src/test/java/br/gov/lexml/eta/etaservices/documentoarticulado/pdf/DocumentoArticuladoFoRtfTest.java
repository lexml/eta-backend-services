package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

class DocumentoArticuladoFoRtfTest {

    private static final String PREFIXO_JPEG = "data:image/jpeg;base64,";

    @Test
    void pngOpacoViraJpeg() throws Exception {
        BufferedImage jpeg = adaptarEler(dataUri("png", imagem(BufferedImage.TYPE_INT_RGB, Color.RED), "png"));

        assertThat(jpeg.getRGB(5, 5) & 0xFFFFFF).satisfies(rgb -> assertThat(vermelho(rgb)).isTrue());
    }

    @Test
    void pngComTransparenciaFicaComFundoBranco() throws Exception {
        BufferedImage transparente = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        BufferedImage jpeg = adaptarEler(dataUri("png", transparente, "png"));

        int rgb = jpeg.getRGB(5, 5);
        assertThat((rgb >> 16) & 0xFF).isGreaterThan(240);
        assertThat((rgb >> 8) & 0xFF).isGreaterThan(240);
        assertThat(rgb & 0xFF).isGreaterThan(240);
    }

    @Test
    void gifEBmpTambemViramJpeg() throws Exception {
        assertThat(adaptarEler(dataUri("gif", imagem(BufferedImage.TYPE_INT_RGB, Color.BLUE), "gif"))).isNotNull();
        assertThat(adaptarEler(dataUri("bmp", imagem(BufferedImage.TYPE_INT_RGB, Color.BLUE), "bmp"))).isNotNull();
    }

    @Test
    void jpegOriginalNaoEhRecodificado() throws Exception {
        String original = dataUri("jpeg", imagem(BufferedImage.TYPE_INT_RGB, Color.GREEN), "jpg");

        assertThat(srcAposAdaptar(original)).isEqualTo(original);
    }

    @Test
    void imagemExternaViraAviso() throws Exception {
        Document fo = adaptar("https://exemplo.org/imagem.png");

        assertAviso(fo);
    }

    @Test
    void dadosIlegiveisViramAvisoSemOConteudo() throws Exception {
        String src = "data:image/png;base64," + Base64.getEncoder().encodeToString("isto não é uma imagem".getBytes());

        Document fo = adaptar(src);

        assertAviso(fo);
        assertThat(fo.asXML()).doesNotContain(Base64.getEncoder().encodeToString("isto".getBytes()));
    }

    @Test
    void base64InvalidoViraAviso() throws Exception {
        assertAviso(adaptar("data:image/png;base64,@@@"));
    }

    private static boolean vermelho(int rgb) {
        return ((rgb >> 16) & 0xFF) > 200 && ((rgb >> 8) & 0xFF) < 60 && (rgb & 0xFF) < 60;
    }

    private static void assertAviso(Document fo) {
        assertThat(fo.selectNodes("//*[local-name()='external-graphic']")).isEmpty();
        Element aviso = (Element) fo.selectSingleNode("//*[local-name()='inline']");
        assertThat(aviso.getText()).isEqualTo(DocumentoArticuladoFoRtf.AVISO_IMAGEM);
        assertThat(aviso.attributeValue("color")).isEqualTo("#FF0000");
    }

    private static BufferedImage imagem(int tipo, Color cor) {
        BufferedImage imagem = new BufferedImage(10, 10, tipo);
        java.awt.Graphics2D g = imagem.createGraphics();
        g.setColor(cor);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        return imagem;
    }

    private static String dataUri(String subtipo, BufferedImage imagem, String formato) throws IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, formato, saida);
        return "data:image/" + subtipo + ";base64," + Base64.getEncoder().encodeToString(saida.toByteArray());
    }

    private static Document adaptar(String src) throws DocumentException {
        Document fo = DocumentHelper.parseText("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"><fo:block>"
                + "<fo:external-graphic src=\"url('" + src + "')\"/></fo:block></fo:root>");
        DocumentoArticuladoFoRtf.adaptar(fo);
        return fo;
    }

    private static String srcAposAdaptar(String src) throws DocumentException {
        Element imagem = (Element) adaptar(src).selectSingleNode("//*[local-name()='external-graphic']");
        return imagem.attributeValue("src").replaceAll("^url\\('(.*)'\\)$", "$1");
    }

    private static BufferedImage adaptarEler(String src) throws Exception {
        String novo = srcAposAdaptar(src);
        assertThat(novo).startsWith(PREFIXO_JPEG);
        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(novo.substring(PREFIXO_JPEG.length()))));
    }

}
