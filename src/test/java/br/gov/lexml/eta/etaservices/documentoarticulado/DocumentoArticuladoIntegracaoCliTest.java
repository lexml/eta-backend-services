package br.gov.lexml.eta.etaservices.documentoarticulado;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;
import br.gov.lexml.eta.etaservices.documentoarticulado.extracao.DocumentoArticuladoJsonExtractor;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Integração com o executável jsonix-lexml real (2.0.0+). Ignorado em {@code mvn test}; para rodar:
 * {@code mvn test -Dtest=DocumentoArticuladoIntegracaoCliTest -Djsonix-lexml.cli=<caminho do executável>}.
 */
@EnabledIfSystemProperty(named = "jsonix-lexml.cli", matches = ".+")
class DocumentoArticuladoIntegracaoCliTest {

    private ConversorDocumentoArticulado conversor;

    @BeforeEach
    void setUp() {
        conversor = new ConversorDocumentoArticuladoCli(Paths.get(System.getProperty("jsonix-lexml.cli")), Duration.ofSeconds(60));
    }

    @Test
    void idaEVoltaComOExecutavelReal() throws Exception {
        String original = recurso(JSON_EXEMPLO);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        new DocumentoArticuladoPdfGenerator(conversor).generate(original, pdf);
        StringWriter recuperado = new StringWriter();
        new DocumentoArticuladoJsonExtractor(conversor).extractJsonFromPdf(new ByteArrayInputStream(pdf.toByteArray()), recuperado);

        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readTree(recuperado.toString())).isEqualTo(mapper.readTree(original));
    }

    @Test
    void xmlConvertidoPreservaAcentosEMetadadosLexEdit() {
        String xml = conversor.jsonParaXml(recurso(JSON_EXEMPLO));

        assertThat(xml)
                .contains("xmlns:lexedit=\"http://www.lexml.gov.br/lexedit/1.0\"")
                .contains("local=\"Sala das Sessões\"")
                .contains("Fica instituído o Programa de Modernização");
    }

    @Test
    void documentoRejeitadoPeloExecutavel() {
        assertThatThrownBy(() -> conversor.jsonParaXml("{\"invalido\":1}"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("is not known in this context");
    }

}
