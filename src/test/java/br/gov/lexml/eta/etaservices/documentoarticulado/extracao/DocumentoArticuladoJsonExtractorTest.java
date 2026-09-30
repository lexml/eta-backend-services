package br.gov.lexml.eta.etaservices.documentoarticulado.extracao;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class DocumentoArticuladoJsonExtractorTest {

    private final ConversorDocumentoArticuladoFake conversor = ConversorDocumentoArticuladoFake.comExemplo();
    private final DocumentoArticuladoJsonExtractor extractor = new DocumentoArticuladoJsonExtractor(conversor);

    @Test
    void idaEVoltaPreservaODocumento() throws Exception {
        String original = recurso(JSON_EXEMPLO);
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(conversor).generate(original, pdf);

        StringWriter recuperado = new StringWriter();
        extractor.extractJsonFromPdf(new ByteArrayInputStream(pdf.toByteArray()), recuperado);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode esperado = mapper.readTree(original);
        JsonNode obtido = mapper.readTree(recuperado.toString());
        assertThat(obtido).isEqualTo(esperado);
        // Os metadados do LexEdit (lexedit:Metadado) fazem parte do documento recuperado
        assertThat(obtido.at("/value/metadado/metadadoProprietario/0/any/0/value/local").asText()).isEqualTo("Sala das Sessões");
    }

    @Test
    void pdfDeOutroEditorSemOAnexo() throws Exception {
        try (InputStream pdfEmenda = getClass().getResourceAsStream("/test1.pdf")) {
            assertThat(pdfEmenda).isNotNull();
            StringWriter json = new StringWriter();

            assertThatThrownBy(() -> extractor.extractJsonFromPdf(pdfEmenda, json))
                    .isInstanceOf(EtaBackendException.class)
                    .hasMessage("Não se trata de um arquivo gerado pelo editor de proposições.");
            assertThat(json.toString()).isEmpty();
        }
    }

    @Test
    void arquivoQueNaoEhPdf() {
        StringWriter json = new StringWriter();
        ByteArrayInputStream naoPdf = new ByteArrayInputStream("isto não é um PDF".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extractJsonFromPdf(naoPdf, json))
                .isInstanceOf(EtaBackendException.class)
                .hasMessage("Não se trata de um arquivo gerado pelo editor de proposições.");
        assertThat(json.toString()).isEmpty();
    }

    @Test
    void falhaNaConversaoParaJson() throws Exception {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(conversor).generate(recurso(JSON_EXEMPLO), pdf);
        conversor.falharCom("erro na conversão para JSON");
        StringWriter json = new StringWriter();

        assertThatThrownBy(() -> extractor.extractJsonFromPdf(new ByteArrayInputStream(pdf.toByteArray()), json))
                .isInstanceOf(EtaBackendException.class)
                .hasMessage("erro na conversão para JSON");
        assertThat(json.toString()).isEmpty();
    }

}
