package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.pdf.ParametrosImpressaoDocumentoArticuladoTest.documento;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.ObjectMapper;

class OpcoesImpressaoDocumentoArticuladoTest {

    @Test
    void opcoesDoDocumento() throws Exception {
        OpcoesImpressaoDocumentoArticulado opcoes = OpcoesImpressaoDocumentoArticulado.de(documento(
                "\"imprimirBrasao\":false,\"textoCabecalho\":\"  Gabinete  \",\"reduzirEspacoEntreLinhas\":true,\"tamanhoFonte\":16"));

        assertThat(opcoes.isImprimirBrasao()).isFalse();
        assertThat(opcoes.getTextoCabecalho()).isEqualTo("Gabinete");
        assertThat(opcoes.isReduzirEspacoEntreLinhas()).isTrue();
        assertThat(opcoes.getTamanhoFonte()).isEqualTo(16);
    }

    @Test
    void semOpcoesValemOsPadroesDoEditor() throws Exception {
        assertPadrao(OpcoesImpressaoDocumentoArticulado.de(new ObjectMapper().readTree("{\"value\":{\"metadado\":{}}}")));
        assertPadrao(OpcoesImpressaoDocumentoArticulado.de(documento("")));
        assertPadrao(OpcoesImpressaoDocumentoArticulado.padrao());
    }

    @Test
    void tiposInesperadosValemOsPadroes() throws Exception {
        assertPadrao(OpcoesImpressaoDocumentoArticulado.de(documento(
                "\"imprimirBrasao\":\"false\",\"textoCabecalho\":1,\"reduzirEspacoEntreLinhas\":\"true\",\"tamanhoFonte\":\"18\"")));
    }

    /** O editor só oferece 14, 16 e 18; outro tamanho vale 14. */
    @ParameterizedTest
    @ValueSource(strings = { "11", "15", "20", "0", "-14", "16.5" })
    void tamanhoForaDoEditorVale14(String tamanho) throws Exception {
        assertThat(OpcoesImpressaoDocumentoArticulado.de(documento("\"tamanhoFonte\":" + tamanho)).getTamanhoFonte()).isEqualTo(14);
    }

    private static void assertPadrao(OpcoesImpressaoDocumentoArticulado opcoes) {
        assertThat(opcoes.isImprimirBrasao()).isTrue();
        assertThat(opcoes.getTextoCabecalho()).isEmpty();
        assertThat(opcoes.isReduzirEspacoEntreLinhas()).isFalse();
        assertThat(opcoes.getTamanhoFonte()).isEqualTo(14);
    }

}
