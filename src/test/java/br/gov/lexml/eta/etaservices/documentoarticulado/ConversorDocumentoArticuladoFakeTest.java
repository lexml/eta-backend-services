package br.gov.lexml.eta.etaservices.documentoarticulado;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.XML_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = { ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO, ConversorDocumentoArticuladoFake.CAPITULO_E_SECAO,
            ConversorDocumentoArticuladoFake.PENA_E_TITULO_DISPOSITIVO, ConversorDocumentoArticuladoFake.REMISSOES_INTERNAS })
    void converteOsDocumentosDaIssue72(String nome) throws Exception {
        ConversorDocumentoArticuladoFake fake = ConversorDocumentoArticuladoFake.comDocumentosDeTeste();
        String json = ConversorDocumentoArticuladoFake.json(nome);
        String xml = ConversorDocumentoArticuladoFake.xml(nome);

        assertThat(fake.jsonParaXml(json)).isEqualTo(xml);
        assertThat(fake.xmlParaJson(xml)).isEqualTo(json);
        assertThat(new ObjectMapper().readTree(json).at("/value/projetoNorma/norma/parteInicial/epigrafe").isMissingNode()).isFalse();
        // O par de exemplo da #71 continua registrado
        assertThat(fake.jsonParaXml(recurso(JSON_EXEMPLO))).isEqualTo(recurso(XML_EXEMPLO));
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
