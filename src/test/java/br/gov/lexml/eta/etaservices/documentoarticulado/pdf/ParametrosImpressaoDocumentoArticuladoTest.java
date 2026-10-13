package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ParametrosImpressaoDocumentoArticuladoTest {

    @Test
    void valoresPadrao() {
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.padrao();

        assertThat(parametros.getTamanhoFonte()).isEqualTo(14);
        assertThat(parametros.getMaxTamanhoFonte()).isEqualTo("16pt");
        assertThat(parametros.getLineHeight()).isEqualTo("150%");
        assertThat(parametros.getPMarginBottom()).isEqualTo("0.6em");
        assertThat(parametros.getEspacoAntesTituloJustificacao()).isEqualTo("26pt");
        assertThat(parametros.getEspacoAntesCorpoJustificacao()).isEqualTo("14pt");
    }

    /** Valores da emenda (template-velocity-emenda.xml) para cada combinação de fonte e "reduzir espaço". */
    @ParameterizedTest
    @CsvSource({ "14,false,14,16pt,150%,0.6em,26pt,14pt", "16,false,16,16pt,150%,0.6em,26pt,14pt",
            "18,false,18,18pt,150%,0.6em,26pt,14pt", "14,true,14,16pt,120%,0.45em,22pt,10pt",
            "18,true,18,18pt,120%,0.45em,22pt,10pt" })
    void valoresDasOpcoesDoDocumento(int fonte, boolean reduzir, int tamanho, String destaque, String entrelinha,
            String espacoParagrafo, String antesTitulo, String antesCorpo) throws Exception {
        ParametrosImpressaoDocumentoArticulado parametros = ParametrosImpressaoDocumentoArticulado.de(
                OpcoesImpressaoDocumentoArticulado.de(documento("\"tamanhoFonte\":" + fonte + ",\"reduzirEspacoEntreLinhas\":" + reduzir)));

        assertThat(parametros.getTamanhoFonte()).isEqualTo(tamanho);
        assertThat(parametros.getMaxTamanhoFonte()).isEqualTo(destaque);
        assertThat(parametros.getLineHeight()).isEqualTo(entrelinha);
        assertThat(parametros.getPMarginBottom()).isEqualTo(espacoParagrafo);
        assertThat(parametros.getEspacoAntesTituloJustificacao()).isEqualTo(antesTitulo);
        assertThat(parametros.getEspacoAntesCorpoJustificacao()).isEqualTo(antesCorpo);
    }

    /** Monta um documento-articulado.json mínimo com os atributos informados em lexedit:OpcoesImpressao. */
    static JsonNode documento(String atributosOpcoes) throws Exception {
        return new ObjectMapper().readTree("{\"value\":{\"metadado\":{\"metadadoProprietario\":[{\"any\":[{\"value\":"
                + "{\"opcoesImpressao\":{" + atributosOpcoes + "}}}]}]}}}");
    }

}
