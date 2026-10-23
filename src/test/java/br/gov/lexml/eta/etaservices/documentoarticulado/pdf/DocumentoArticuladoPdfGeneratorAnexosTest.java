package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.extracao.DocumentoArticuladoJsonExtractor;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGeneratorTest.Anexo;
import br.gov.lexml.eta.etaservices.parecer.AnexoPDFA;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoPdfGeneratorAnexosTest {

    private final ConversorDocumentoArticuladoFake conversor = ConversorDocumentoArticuladoFake.comExemplo();
    private final DocumentoArticuladoPdfGenerator gerador = new DocumentoArticuladoPdfGenerator(conversor);
    private final String json = recurso(JSON_EXEMPLO);

    @Test
    void semAnexosOuComListaVaziaOuNulaGeraOMesmoPdf() throws IOException {
        byte[] base = gerar(null);

        for (List<AnexoPDFA> semAnexos : Arrays.<List<AnexoPDFA>>asList(
                Collections.emptyList(), Arrays.asList(null, AnexoPDFA.builder().nomeArquivo("x.pdf").build()))) {
            byte[] pdf = gerar(semAnexos);
            assertThat(paginas(pdf)).isEqualTo(paginas(base));
            assertThat(DocumentoArticuladoPdfGeneratorTest.anexos(pdf)).containsOnlyKeys("documento-articulado.xml");
        }
        ByteArrayOutputStream antigo = new ByteArrayOutputStream();
        gerador.generate(json, antigo);
        assertThat(paginas(antigo.toByteArray())).isEqualTo(paginas(base));
    }

    @Test
    void anexosSaoEmbutidosEOsImpressosConcatenados() throws IOException {
        byte[] anexoA = arquivo("anexoA.pdf");
        byte[] anexoB = arquivo("anexoB.pdf");
        int paginasBase = paginas(gerar(null));

        byte[] pdf = gerar(Arrays.asList(anexo("anexoA.pdf", anexoA, true), anexo("anexoB.pdf", anexoB, false)));

        assertThat(paginas(pdf)).isEqualTo(paginasBase + paginas(anexoA));
        Map<String, Anexo> anexos = DocumentoArticuladoPdfGeneratorTest.anexos(pdf);
        assertThat(anexos).containsOnlyKeys("documento-articulado.xml", "anexoA.pdf", "anexoB.pdf");
        assertThat(anexos.get("anexoA.pdf").conteudo).isEqualTo(anexoA);
        assertThat(anexos.get("anexoB.pdf").conteudo).isEqualTo(anexoB);
    }

    @Test
    void hashGravadoEhOMd5DoPdfFinalComAnexosImpressos() throws Exception {
        byte[] pdf = gerar(Arrays.asList(anexo("anexoA.pdf", arquivo("anexoA.pdf"), true)));

        String texto = new String(pdf, StandardCharsets.ISO_8859_1);
        int inicio = texto.lastIndexOf("<check:hash>") + "<check:hash>".length();
        String hash = texto.substring(inicio, inicio + 32);
        assertThat(hash).isNotEqualTo("00000000000000000000000000000000");

        byte[] comPlaceholder = pdf.clone();
        Arrays.fill(comPlaceholder, inicio, inicio + 32, (byte) '0');
        StringBuilder esperado = new StringBuilder();
        for (byte b : MessageDigest.getInstance("MD5").digest(comPlaceholder)) {
            esperado.append(String.format("%02X", b));
        }
        assertThat(hash).isEqualTo(esperado.toString());
    }

    @Test
    void nomeReservadoEhRejeitadoSemEscreverNoDestino() {
        for (String nome : Arrays.asList("documento-articulado.xml", "Documento-Articulado.XML")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertThatThrownBy(() -> gerador.generate(json,
                    Arrays.asList(anexo(nome, arquivo("anexoA.pdf"), false)), out))
                    .isInstanceOf(EtaBackendException.class)
                    .hasMessageContaining("documento-articulado.xml");
            assertThat(out.size()).isZero();
        }
    }

    @Test
    void anexoImpressoInvalidoFalhaSemEscreverNoDestino() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThatThrownBy(() -> gerador.generate(json,
                Arrays.asList(anexo("falso.pdf", "não é PDF".getBytes(StandardCharsets.UTF_8), true)), out))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("falso.pdf");
        assertThat(out.size()).isZero();
    }

    @Test
    void jsonInvalidoContinuaSendoRejeitadoComAnexos() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThatThrownBy(() -> gerador.generate("não é json", Arrays.asList(anexo("a.pdf", new byte[0], false)), out))
                .isInstanceOf(EtaBackendException.class);
        assertThat(out.size()).isZero();
    }

    @Test
    void idaEVoltaDoJsonFuncionaEmPdfComAnexos() throws IOException {
        byte[] pdf = gerar(Arrays.asList(
                anexo("anexoA.pdf", arquivo("anexoA.pdf"), true), anexo("anexoB.docx", arquivo("anexoB.docx"), false)));

        StringWriter recuperado = new StringWriter();
        new DocumentoArticuladoJsonExtractor(conversor).extractJsonFromPdf(new ByteArrayInputStream(pdf), recuperado);

        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readTree(recuperado.toString())).isEqualTo(mapper.readTree(json));
    }

    private byte[] gerar(List<AnexoPDFA> anexos) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        gerador.generate(json, anexos, out);
        return out.toByteArray();
    }

    private static AnexoPDFA anexo(String nome, byte[] conteudo, boolean imprimir) {
        return AnexoPDFA.builder().nomeArquivo(nome).file(new ByteArrayInputStream(conteudo)).imprimir(imprimir).build();
    }

    private static int paginas(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            return documento.getNumberOfPages();
        }
    }

    private static byte[] arquivo(String nome) {
        try (InputStream in = DocumentoArticuladoPdfGeneratorAnexosTest.class.getResourceAsStream("/" + nome)) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
