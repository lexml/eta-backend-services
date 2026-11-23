package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ANEXO_PARECER;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.EPIGRAFE_VARIAS_LINHAS;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO_LONGA;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.REMISSOES_INTERNAS;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.comDocumentosDeTeste;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.json;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFoDocxTest.descompactar;
import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFoDocxTest.texto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.parser.pl.docx.DOCXReader$;
import scala.Option;
import scala.xml.Elem;

class DocumentoArticuladoDocxGeneratorTest {

    private final DocumentoArticuladoDocxGenerator gerador = new DocumentoArticuladoDocxGenerator(comDocumentosDeTeste());

    @Test
    void documentoValidoGeraZipOoxmlCompletoELegivelPeloDocxReader() throws Exception {
        byte[] bytes = docx(recurso(JSON_EXEMPLO));
        Map<String, byte[]> zip = descompactar(bytes);

        assertThat(zip).containsKeys("[Content_Types].xml", "_rels/.rels", "word/document.xml",
                "word/styles.xml", "word/settings.xml", "word/_rels/document.xml.rels", "word/header1.xml");
        for (Map.Entry<String, byte[]> entry : zip.entrySet()) {
            if (entry.getKey().endsWith(".xml") || entry.getKey().endsWith(".rels")) {
                assertXmlValido(entry.getValue());
            }
        }

        Option<Elem> xhtml = DOCXReader$.MODULE$.readDOCX(new ByteArrayInputStream(bytes));
        assertThat(xhtml.isDefined()).isTrue();
        assertThat(xhtml.get().toString()).contains("Fica instituído o Programa de Modernização");
    }

    @Test
    void acentuacaoPreservadaEOrdemDasPartes() throws Exception {
        String textoPlano = textoPlano(docx(recurso(JSON_EXEMPLO)));

        assertThat(textoPlano).contains("Fica instituído o Programa de Modernização").contains("Sala das Sessões");
        assertThat(emOrdem(textoPlano, "Este documento contém pendências", "Remover comentários.",
                "PROJETO DE LEI", "Institui o Programa de Modernização", "O CONGRESSO NACIONAL decreta:",
                "Art. 1º", "Fica instituído o Programa", "§ 1º", "I –", "II –", "a) publicação de dados abertos",
                "Art. 6º", "JUSTIFICAÇÃO", "Sala das Sessões, 24 de abril de 2026",
                "Senador Davi Alcolumbre", "Senadora Soraya Thronicke")).isTrue();
    }

    @Test
    void cabecalhoComBrasaoFicaApenasNaPrimeiraPagina() throws Exception {
        Map<String, byte[]> zip = descompactar(docx(recurso(JSON_EXEMPLO)));

        assertThat(zip).containsKey("word/header1.xml").containsKey("word/_rels/header1.xml.rels");
        assertThat(zip.keySet().stream().anyMatch(k -> k.startsWith("word/media/image") && k.endsWith(".jpg"))).isTrue();
        assertThat(texto(zip, "word/header1.xml")).contains("SENADO FEDERAL").contains("w:drawing");
        assertThat(texto(zip, "word/document.xml")).contains("w:titlePg").contains("w:type=\"first\"");
    }

    @Test
    void semBrasaoNaoIncluiImagemNemNomeDaCasa() throws Exception {
        Map<String, byte[]> zip = descompactar(docx(json(JUSTIFICACAO_LONGA)));

        assertThat(zip).containsKey("word/header1.xml");
        String header = texto(zip, "word/header1.xml");
        assertThat(header).doesNotContain("w:drawing").doesNotContain("SENADO FEDERAL")
                .doesNotContain("CÂMARA DOS DEPUTADOS").doesNotContain("CONGRESSO NACIONAL");
    }

    @Test
    void textoExcluidoEmRevisaoNaJustificacaoNaoEImpresso() throws Exception {
        String textoPlano = textoPlano(docx(recurso(JSON_EXEMPLO)));

        assertThat(textoPlano).contains("Esta proposição promove a modernização da gestão pública")
                .doesNotContain("promove a reforma");
    }

    @Test
    void anexoDeParecerOmiteJustificacaoEMantemFecho() throws Exception {
        Map<String, byte[]> zip = descompactar(docx(json(ANEXO_PARECER)));
        String textoPlano = textoPlano(zip);

        assertThat(textoPlano).doesNotContain("JUSTIFICAÇÃO").contains("Sala d");
        assertThat(zip).doesNotContainKey("word/footnotes.xml");
    }

    @Test
    void justificacaoCompletaGeraNotasTabelasListasEImagensNativas() throws Exception {
        Map<String, byte[]> zip = descompactar(docx(json(JUSTIFICACAO)));

        assertThat(zip).containsKey("word/footnotes.xml");
        assertThat(texto(zip, "word/document.xml")).contains("w:footnoteReference")
                .contains("w:jc w:val=\"center\"").contains("w:gridSpan").contains("w:vMerge");
        assertThat(zip.keySet().stream().anyMatch(k -> k.startsWith("word/media/") && k.endsWith(".png"))).isTrue();
        assertThat(zip.keySet().stream().anyMatch(k -> k.startsWith("word/media/") && k.endsWith(".jpg"))).isTrue();
    }

    @Test
    void remissoesInternasEExternasEAlteracaoDeNorma() throws Exception {
        Map<String, byte[]> zipRemissoes = descompactar(docx(json(REMISSOES_INTERNAS)));
        String docRemissoes = texto(zipRemissoes, "word/document.xml");
        assertThat(docRemissoes).contains("w:bookmarkStart").contains("w:anchor=");

        Map<String, byte[]> zipAlteracao = descompactar(docx(json(ARTICULACAO_E_ALTERACAO)));
        String docAlteracao = texto(zipAlteracao, "word/document.xml");
        assertThat(docAlteracao).contains("Art. 12-A.").contains("(NR)");
        assertThat(texto(zipAlteracao, "word/_rels/document.xml.rels")).contains("https://normas.leg.br/?urn=");
    }

    @ParameterizedTest
    @ValueSource(strings = { ARTICULACAO_E_ALTERACAO, CAPITULO_E_SECAO, PENA_E_TITULO_DISPOSITIVO, REMISSOES_INTERNAS,
            JUSTIFICACAO, JUSTIFICACAO_LONGA, ANEXO_PARECER, EPIGRAFE_VARIAS_LINHAS })
    void todasAsFixturesGeramDocxValidoComXmlBemFormado(String fixture) throws Exception {
        byte[] bytes = docx(json(fixture));
        Map<String, byte[]> zip = descompactar(bytes);

        assertThat(zip).containsKeys("[Content_Types].xml", "_rels/.rels", "word/document.xml", "word/styles.xml");
        for (Map.Entry<String, byte[]> entry : zip.entrySet()) {
            if (entry.getKey().endsWith(".xml") || entry.getKey().endsWith(".rels")) {
                assertXmlValido(entry.getValue());
            }
        }
    }

    @Test
    void docxNaoEmbuteModeloNemAnexos() throws Exception {
        Map<String, byte[]> zip = descompactar(docx(recurso(JSON_EXEMPLO)));

        assertThat(zip.keySet()).noneMatch(nome -> nome.contains("documento-articulado.xml"));
        String doc = texto(zip, "word/document.xml");
        assertThat(doc).doesNotContain("documento-articulado.xml").doesNotContain("lexedit:Metadado").doesNotContain("<LexML");
    }

    @Test
    void entradasInvalidasEFalhasNaoEscrevemNoDestino() {
        ByteArrayOutputStream destino1 = new ByteArrayOutputStream();
        assertThatThrownBy(() -> gerador.generate("isto não é JSON", destino1)).isInstanceOf(EtaBackendException.class);
        assertThat(destino1.size()).isZero();

        ByteArrayOutputStream destino2 = new ByteArrayOutputStream();
        assertThatThrownBy(() -> gerador.generate("{\"invalido\":1}", destino2)).isInstanceOf(EtaBackendException.class);
        assertThat(destino2.size()).isZero();

        ByteArrayOutputStream destino3 = new ByteArrayOutputStream();
        ConversorDocumentoArticuladoFake falho = comDocumentosDeTeste().falharCom("erro do jsonix-lexml");
        assertThatThrownBy(() -> new DocumentoArticuladoDocxGenerator(falho).generate(recurso(JSON_EXEMPLO), destino3))
                .isInstanceOf(EtaBackendException.class).hasMessageContaining("erro do jsonix-lexml");
        assertThat(destino3.size()).isZero();

        ByteArrayOutputStream destino4 = new ByteArrayOutputStream();
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
        assertThatThrownBy(() -> new DocumentoArticuladoDocxGenerator(xmlQuebrado).generate(recurso(JSON_EXEMPLO), destino4))
                .isInstanceOf(EtaBackendException.class);
        assertThat(destino4.size()).isZero();
    }

    @Test
    void gerarDocxNaoAlteraOPdfNemORtfDoMesmoDocumento() throws IOException {
        DocumentoArticuladoPdfGenerator pdfGerador = new DocumentoArticuladoPdfGenerator(comDocumentosDeTeste());
        DocumentoArticuladoRtfGenerator rtfGerador = new DocumentoArticuladoRtfGenerator(comDocumentosDeTeste());
        String json = recurso(JSON_EXEMPLO);

        byte[] pdfAntes = gerarBytes(out -> pdfGerador.generate(json, out));
        byte[] rtfAntes = gerarBytes(out -> rtfGerador.generate(json, out));

        docx(json);

        byte[] pdfDepois = gerarBytes(out -> pdfGerador.generate(json, out));
        byte[] rtfDepois = gerarBytes(out -> rtfGerador.generate(json, out));

        assertThat(textoDoPdf(pdfDepois)).isEqualTo(textoDoPdf(pdfAntes));
        assertThat(DocumentoArticuladoRtfGeneratorTest.texto(new String(rtfDepois, StandardCharsets.ISO_8859_1)))
                .isEqualTo(DocumentoArticuladoRtfGeneratorTest.texto(new String(rtfAntes, StandardCharsets.ISO_8859_1)));
    }

    private byte[] docx(String json) throws IOException {
        return gerarBytes(out -> gerador.generate(json, out));
    }

    @FunctionalInterface
    private interface Geracao {
        void executar(ByteArrayOutputStream out) throws IOException;
    }

    private static byte[] gerarBytes(Geracao geracao) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        geracao.executar(out);
        return out.toByteArray();
    }

    private static String textoDoPdf(byte[] pdf) throws IOException {
        try (PDDocument documento = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    private static Document assertXmlValido(byte[] xml) throws DocumentException {
        return new SAXReader().read(new ByteArrayInputStream(xml));
    }

    private static String textoPlano(byte[] docx) throws Exception {
        return textoPlano(descompactar(docx));
    }

    private static String textoPlano(Map<String, byte[]> zip) throws Exception {
        Document doc = assertXmlValido(zip.get("word/document.xml"));
        return doc.getRootElement().getStringValue().replace('\u00a0', ' ').replaceAll("\\s+", " ");
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

}
