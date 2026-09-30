package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParametrosImpressaoDocumentoArticuladoTest {

    @Test
    void valoresPadrao() {
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.padrao();

        assertThat(parametros.getTamanhoFonte()).isEqualTo(14);
        assertThat(parametros.getMaxTamanhoFonte()).isEqualTo("16pt");
        assertThat(parametros.getLineHeight()).isEqualTo("150%");
        assertThat(parametros.getPMarginBottom()).isEqualTo("0.6em");
    }

}
