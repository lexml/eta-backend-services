package br.gov.lexml.eta.etaservices.documentoarticulado;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

class ConversorDocumentoArticuladoFakeTest {

    @Test
    void converteOParDeExemploNosDoisSentidos() throws Exception {
        ConversorDocumentoArticuladoFake fake = ConversorDocumentoArticuladoFake.comExemplo();
        String json = recurso(JSON_EXEMPLO);
        String xml = recurso(XML_EXEMPLO);

        // JSON reformatado continua sendo reconhecido
        String jsonCompacto = new ObjectMapper().readTree(json).toString();

        assertThat(fake.jsonParaXml(jsonCompacto)).isEqualTo(xml);
        assertThat(fake.xmlParaJson(xml)).isEqualTo(json);
        assertThat(xml).contains("Sala das Sessões").contains("urn:lex:br:senado.federal:projeto.lei:999999;9999");
    }

    @Test
    void documentoNaoRegistradoEhErro() {
        ConversorDocumentoArticuladoFake fake = ConversorDocumentoArticuladoFake.comExemplo();

        assertThatThrownBy(() -> fake.jsonParaXml("{\"outro\":1}")).isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> fake.xmlParaJson("<LexML/>")).isInstanceOf(EtaBackendException.class);
    }

    @Test
    void modoFalha() {
        ConversorDocumentoArticuladoFake fake = ConversorDocumentoArticuladoFake.comExemplo().falharCom("erro simulado");

        assertThatThrownBy(() -> fake.jsonParaXml(recurso(JSON_EXEMPLO)))
                .isInstanceOf(EtaBackendException.class)
                .hasMessage("erro simulado");
    }

}
