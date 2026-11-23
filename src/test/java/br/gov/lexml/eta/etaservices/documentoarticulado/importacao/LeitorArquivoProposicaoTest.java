package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.parser.pl.block.Block;
import br.gov.lexml.parser.pl.block.Paragraph;
import scala.jdk.javaapi.CollectionConverters;

class LeitorArquivoProposicaoTest {

    private final LeitorArquivoProposicao leitor = new LeitorArquivoProposicao();

    @Test
    void deveLerDocxPreservandoParagrafosEFormatacaoInline() throws IOException {
        byte[] docx = DocxTesteBuilder.comCorpoXml(
                "<w:p><w:r><w:t>Dispõe sobre </w:t></w:r>"
                        + "<w:r><w:rPr><w:b/></w:rPr><w:t>negrito</w:t></w:r>"
                        + "<w:r><w:t>, </w:t></w:r>"
                        + "<w:r><w:rPr><w:i/></w:rPr><w:t>itálico</w:t></w:r>"
                        + "<w:r><w:t> e m</w:t></w:r>"
                        + "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:t>2</w:t></w:r>"
                        + "<w:r><w:t>.</w:t></w:r></w:p>"
                        + "<w:p><w:r><w:t>Art. 1º Fica instituído o programa.</w:t></w:r></w:p>");

        List<Block> blocos = CollectionConverters.asJava(leitor.lerDocx(docx));

        assertThat(blocos).hasSize(2);
        assertThat(blocos.get(0)).isInstanceOf(Paragraph.class);
        Paragraph p0 = (Paragraph) blocos.get(0);
        assertThat(p0.unormalizedText()).isEqualTo("Dispõe sobre negrito, itálico e m2.");
        String xmlP0 = p0.toNodeSeq().toString();
        assertThat(xmlP0).contains("<b>negrito</b>");
        assertThat(xmlP0).contains("<i>itálico</i>");
        assertThat(xmlP0).contains("<sup>2</sup>");
        assertThat(((Paragraph) blocos.get(1)).unormalizedText()).isEqualTo("Art. 1º Fica instituído o programa.");
    }

    @Test
    void deveRejeitarDocxVazioOuInvalido() throws IOException {
        byte[] zipSemDocumentXml = DocxTesteBuilder.zipComEntrada("outro.txt", "abc".getBytes(StandardCharsets.UTF_8));
        byte[] naoZip = "isto nao eh um zip".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> leitor.lerDocx(null)).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerDocx(new byte[0])).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerDocx(zipSemDocumentXml)).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerDocx(naoZip)).isInstanceOf(EtaBackendException.class);
    }

    @Test
    void devePreservarOrdemExataDeLinhasConsecutivasNoTxt() {
        String txt = "Primeira linha\nSegunda linha\r\nTerceira linha\rQuarta linha";

        List<String> textos = extrairTextos(leitor.lerTxt(txt));

        assertThat(textos).containsExactly("Primeira linha", "Segunda linha", "Terceira linha", "Quarta linha");
    }

    @Test
    void deveRemoverBomDecodificarWindows1252EEscaparCaracteresEspeciaisXml() {
        String comBom = "\uFEFFEmenta: trata de <bens> & serviços > 10.";
        assertThat(extrairTextos(leitor.lerTxt(comBom.getBytes(StandardCharsets.UTF_8))))
                .containsExactly("Ementa: trata de <bens> & serviços > 10.");

        String textoCp1252 = "Art. 1º Dispõe sobre a organização (§ 1º).";
        byte[] bytesCp1252 = textoCp1252.getBytes(Charset.forName("windows-1252"));
        assertThat(extrairTextos(leitor.lerTxt(bytesCp1252)))
                .containsExactly("Art. 1º Dispõe sobre a organização (§ 1º).");
    }

    @Test
    void deveRejeitarTxtVazioOuNulo() {
        assertThatThrownBy(() -> leitor.lerTxt((String) null)).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerTxt("\uFEFF   \n\t  ")).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerTxt((byte[]) null)).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> leitor.lerTxt(new byte[0])).isInstanceOf(EtaBackendException.class);
    }

    private static List<String> extrairTextos(scala.collection.immutable.List<Block> blocosScala) {
        List<String> textos = new ArrayList<>();
        for (Block b : CollectionConverters.asJava(blocosScala)) {
            if (b instanceof Paragraph) {
                textos.add(((Paragraph) b).unormalizedText());
            }
        }
        return textos;
    }

}
