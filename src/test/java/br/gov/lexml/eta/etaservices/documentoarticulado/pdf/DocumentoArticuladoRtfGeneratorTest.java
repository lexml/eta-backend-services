package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ANEXO_PARECER;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.comDocumentosDeTeste;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.json;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoRtfGeneratorTest {

    private final DocumentoArticuladoRtfGenerator gerador = new DocumentoArticuladoRtfGenerator(comDocumentosDeTeste());

    @Test
    void documentoValidoGeraRtfComChavesBalanceadas() throws IOException {
        String rtf = rtf(recurso(JSON_EXEMPLO));

        assertThat(rtf).startsWith("{\\rtf");
        assertThat(chavesAbertasSemFechar(rtf)).isZero();
    }

    @Test
    void acentuacaoPreservada() throws IOException {
        String texto = texto(rtf(recurso(JSON_EXEMPLO)));

        assertThat(texto).contains("Fica instituído o Programa de Modernização").contains("Sala das Sessões");
    }

    @Test
    void parteInicialEArticulacaoNaOrdemDoDocumento() throws IOException {
        String texto = texto(rtf(recurso(JSON_EXEMPLO)));

        assertThat(emOrdem(texto, "PROJETO DE LEI", "Institui o Programa de Modernização", "O CONGRESSO NACIONAL decreta:",
                "Art. 1º", "Fica instituído o Programa", "§ 1º", "I –", "II –", "a) publicação de dados abertos", "Art. 2º"))
                        .isTrue();
    }

    @Test
    void justificacaoLocalDataEAssinaturasDepoisDaArticulacao() throws IOException {
        String texto = texto(rtf(recurso(JSON_EXEMPLO)));

        assertThat(emOrdem(texto, "Art. 6º", "JUSTIFICAÇÃO", "Sala das Sessões, 24 de abril de 2026", "Senador Davi Alcolumbre",
                "Senadora Soraya Thronicke")).isTrue();
    }

    @Test
    void pendenciasAntesDaEpigrafe() throws IOException {
        String texto = texto(rtf(recurso(JSON_EXEMPLO)));

        assertThat(emOrdem(texto, "Este documento contém pendências", "Remover comentários.", "PROJETO DE LEI")).isTrue();
    }

    @Test
    void textoExcluidoEmRevisaoNaJustificacaoNaoEImpresso() throws IOException {
        String texto = texto(rtf(recurso(JSON_EXEMPLO)));

        assertThat(texto).contains("Esta proposição promove a modernização da gestão pública").doesNotContain("promove a reforma");
    }

    @Test
    void anexoDeParecerSemJustificacaoEComFecho() throws IOException {
        String texto = texto(rtf(json(ANEXO_PARECER)));

        assertThat(texto).doesNotContain("JUSTIFICAÇÃO").contains("Sala d");
    }

    @Test
    void rtfNaoEmbuteOModeloNemAnexos() throws IOException {
        String rtf = rtf(recurso(JSON_EXEMPLO));

        assertThat(rtf).doesNotContain("documento-articulado.xml").doesNotContain("lexedit:Metadado").doesNotContain("<LexML");
    }

    @Test
    void textoQueNaoEJsonFalhaSemEscreverNoDestino() {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();

        assertThatThrownBy(() -> gerador.generate("isto não é JSON", destino)).isInstanceOf(EtaBackendException.class);
        assertThat(destino.size()).isZero();
    }

    @Test
    void jsonQueNaoEDocumentoLexmlFalhaSemEscreverNoDestino() {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();

        assertThatThrownBy(() -> gerador.generate("{\"invalido\":1}", destino)).isInstanceOf(EtaBackendException.class);
        assertThat(destino.size()).isZero();
    }

    @Test
    void falhaNaConversaoParaXmlTrazAMensagemDoConversorESemEscreverNoDestino() {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();
        ConversorDocumentoArticuladoFake falho = comDocumentosDeTeste().falharCom("erro do jsonix-lexml");

        assertThatThrownBy(() -> new DocumentoArticuladoRtfGenerator(falho).generate(recurso(JSON_EXEMPLO), destino))
                .isInstanceOf(EtaBackendException.class).hasMessageContaining("erro do jsonix-lexml");
        assertThat(destino.size()).isZero();
    }

    @Test
    void falhaDepoisDaConversaoNaoEscreveNoDestino() {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();
        ConversorDocumentoArticulado xmlQuebrado = new ConversorDocumentoArticulado() {
            @Override
            public String jsonParaXml(String json) {
                return "<LexML";
            }

            @Override
            public String xmlParaJson(String xml) {
                throw new UnsupportedOperationException();
            }
        };

        assertThatThrownBy(() -> new DocumentoArticuladoRtfGenerator(xmlQuebrado).generate(recurso(JSON_EXEMPLO), destino))
                .isInstanceOf(EtaBackendException.class);
        assertThat(destino.size()).isZero();
    }

    @Test
    void gerarRtfNaoAlteraOPdfDoMesmoDocumento() throws IOException {
        DocumentoArticuladoPdfGenerator pdfGerador = new DocumentoArticuladoPdfGenerator(comDocumentosDeTeste());
        String json = recurso(JSON_EXEMPLO);

        byte[] antes = pdf(pdfGerador, json);
        rtf(json);
        byte[] depois = pdf(pdfGerador, json);

        assertThat(textoDoPdf(depois)).isEqualTo(textoDoPdf(antes));
        assertThat(DocumentoArticuladoPdfGeneratorTest.anexos(depois)).containsOnlyKeys("documento-articulado.xml");
        assertThat(DocumentoArticuladoPdfGeneratorTest.anexos(depois).get("documento-articulado.xml").conteudo)
                .isEqualTo(DocumentoArticuladoPdfGeneratorTest.anexos(antes).get("documento-articulado.xml").conteudo);
    }

    private static byte[] pdf(DocumentoArticuladoPdfGenerator pdfGerador, String json) throws IOException {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();
        pdfGerador.generate(json, destino);
        return destino.toByteArray();
    }

    private static String textoDoPdf(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    private String rtf(String json) throws IOException {
        ByteArrayOutputStream destino = new ByteArrayOutputStream();
        gerador.generate(json, destino);
        return new String(destino.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    /** Texto aproximado do RTF: acentos decodificados, sem comandos, chaves nem imagens, com espaços colapsados. */
    static String texto(String rtf) {
        String s = rtf.replaceAll("[0-9a-f]{60,}", "");
        s = substituir(s, "\\\\u(-?\\d+)\\\\'[0-9a-fA-F]{2}", m -> String.valueOf((char) (Integer.parseInt(m.group(1)) & 0xFFFF)));
        s = substituir(s, "\\\\'([0-9a-fA-F]{2})", m -> new String(new byte[] { (byte) Integer.parseInt(m.group(1), 16) },
                java.nio.charset.Charset.forName("windows-1252")));
        s = s.replaceAll("\\\\[a-zA-Z]+-?\\d* ?", "").replaceAll("[{}]", "");
        // a epígrafe usa espaço não separável (U+00A0)
        return s.replace('\u00a0', ' ').replaceAll("\\s+", " ");
    }

    private static String substituir(String s, String regex, java.util.function.Function<Matcher, String> f) {
        Matcher m = Pattern.compile(regex).matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(f.apply(m)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean emOrdem(String texto, String... trechos) {
        int posicao = -1;
        for (String trecho : trechos) {
            int i = texto.indexOf(trecho, posicao + 1);
            if (i < 0) {
                return false;
            }
            posicao = i;
        }
        return true;
    }

    private static int chavesAbertasSemFechar(String rtf) {
        int abertas = 0;
        for (int i = 0; i < rtf.length(); i++) {
            char c = rtf.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '{') {
                abertas++;
            } else if (c == '}') {
                abertas--;
            }
        }
        return abertas;
    }

}
