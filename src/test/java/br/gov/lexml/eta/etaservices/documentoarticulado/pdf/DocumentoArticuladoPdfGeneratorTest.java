package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

    @Test
    void pdfTemPaginaSemOTextoDaProposicao() throws Exception {
        try (PDDocument pdf = PDDocument.load(pdfExemplo)) {
            assertThat(pdf.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            String texto = new PDFTextStripper().getText(pdf);
            assertThat(texto).doesNotContain("Fica instituído").doesNotContain("Art. 1");
        }
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
