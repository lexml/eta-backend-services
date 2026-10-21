package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake;

class AlertasDocumentoArticuladoTest {

    private static final String REVISAO_TEXTUAL = "\"revisoesTextuais\":{\"revisaoTextual\":[{\"refIdRevisao\":\"_rt1\",\"refIdUsuario\":\"sf:fulano\"}]}";
    private static final String REVISAO_ARTICULACAO = "\"revisoesArticulacao\":{\"revisaoArticulacao\":[{\"revisao\":\"excluido\",\"refIdUsuario\":\"sf:fulano\"}]}";
    private static final String COMENTARIO = "\"comentarios\":{\"sequenciaComentario\":[{\"refIdElementoComentado\":\"art1\",\"comentario\":"
            + "[{\"refIdUsuario\":\"sf:fulano\",\"p\":[{\"content\":[\"Texto.\"]}]}]}]}";

    @Test
    void semDados() throws Exception {
        assertThat(AlertasDocumentoArticulado.de(documento("\"local\":\"Sala das Sessões\"")).getPendencias()).isEmpty();
        assertThat(AlertasDocumentoArticulado.de(new ObjectMapper().readTree("{\"value\":{\"metadado\":{}}}")).getPendencias()).isEmpty();
    }

    /** Revisões e comentários não geram alerta próprio: só as pendências registradas contam. */
    @Test
    void revisoesEComentariosSemPendenciasNaoGeramAlerta() throws Exception {
        assertThat(AlertasDocumentoArticulado.de(documento(REVISAO_TEXTUAL + "," + REVISAO_ARTICULACAO + "," + COMENTARIO))
                .getPendencias()).isEmpty();
    }

    @Test
    void pendenciasNaOrdemESemAlteracao() throws Exception {
        AlertasDocumentoArticulado alertas = AlertasDocumentoArticulado.de(documento(REVISAO_TEXTUAL + "," + COMENTARIO + ","
                + pendencias("Não foi informado um texto de justificação.", "Remover comentários.", "Resolver marcas de revisão de texto.")));

        assertThat(alertas.getPendencias()).containsExactly("Não foi informado um texto de justificação.", "Remover comentários.",
                "Resolver marcas de revisão de texto.");
    }

    @Test
    void pendenciaEmBrancoDescartadaEAsDemaisComoChegaram() throws Exception {
        AlertasDocumentoArticulado alertas = AlertasDocumentoArticulado.de(documento(pendencias("  Corrigir remissões internas inválidas. ", "  ", "")));

        assertThat(alertas.getPendencias()).containsExactly("  Corrigir remissões internas inválidas. ");
    }

    /** Lista ausente, vazia ou de outro tipo: nenhuma pendência. */
    @ParameterizedTest
    @ValueSource(strings = { "\"pendencias\":{\"pendencia\":[]}", "\"pendencias\":{}", "\"pendencias\":\"x\"", "\"pendencias\":{\"pendencia\":[1]}" })
    void semPendencias(String pendencias) throws Exception {
        assertThat(AlertasDocumentoArticulado.de(documento(pendencias)).getPendencias()).isEmpty();
    }

    @Test
    void exemplo() throws Exception {
        AlertasDocumentoArticulado alertas = AlertasDocumentoArticulado.de(
                new ObjectMapper().readTree(ConversorDocumentoArticuladoFake.recurso(ConversorDocumentoArticuladoFake.JSON_EXEMPLO)));

        assertThat(alertas.getPendencias()).hasSize(5);
    }

    private static String pendencias(String... textos) {
        StringBuilder lista = new StringBuilder();
        for (String texto : textos) {
            lista.append(lista.length() == 0 ? "" : ",").append('"').append(texto).append('"');
        }
        return "\"pendencias\":{\"pendencia\":[" + lista + "]}";
    }

    /** documento-articulado.json mínimo com os atributos informados no valor do {@code lexedit:Metadado}. */
    private static JsonNode documento(String atributos) throws Exception {
        return new ObjectMapper().readTree("{\"value\":{\"metadado\":{\"metadadoProprietario\":[{\"any\":[{\"name\":{"
                + "\"namespaceURI\":\"http://www.lexml.gov.br/lexedit/1.0\",\"localPart\":\"Metadado\"},\"value\":{" + atributos + "}}]}]}}}");
    }

}
