package br.gov.lexml.eta.etaservices.printing.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class EmendaPojoComentariosTest {

    @Test
    void desserializaSequenciasComentarioDoJsonDaEmenda() throws Exception {
        String json = "{"
                + "\"sequenciasComentario\":[{"
                + "\"id\":\"sc123\","
                + "\"local\":\"texto\","
                + "\"comentarios\":[{"
                + "\"usuario\":{\"nome\":\"fragomeni\",\"id\":\"fragomeni\",\"sigla\":\"F\"},"
                + "\"dataHora\":\"2026-05-18 15:48:26\","
                + "\"texto\":\"Comentario salvo no JSON\""
                + "}]"
                + "}]"
                + "}";

        EmendaPojo emenda = new ObjectMapper().readValue(json, EmendaPojo.class);

        assertThat(emenda.getSequenciasComentario()).hasSize(1);
        SequenciaComentarioPojo sequencia = emenda.getSequenciasComentario().get(0);
        assertThat(sequencia.getId()).isEqualTo("sc123");
        assertThat(sequencia.getLocal()).isEqualTo("texto");
        assertThat(sequencia.getComentarios()).hasSize(1);

        ComentarioPojo comentario = sequencia.getComentarios().get(0);
        assertThat(comentario.getUsuario().getNome()).isEqualTo("fragomeni");
        assertThat(comentario.getDataHora()).isEqualTo("2026-05-18 15:48:26");
        assertThat(comentario.getTexto()).isEqualTo("Comentario salvo no JSON");
    }

    @Test
    void desserializaRevisaoSemTypeComoRevisaoJustificativa() throws Exception {
        String json = "{"
                + "\"revisoes\":[{"
                + "\"id\":\"r1\","
                + "\"usuario\":{\"nome\":\"ruan.oliveira\",\"id\":\"60339139307\",\"sigla\":\"R\"},"
                + "\"dataHora\":\"2026-05-28 16:05:00\","
                + "\"descricao\":\"Justificação Alterada\""
                + "}]"
                + "}";

        EmendaPojo emenda = new ObjectMapper().readValue(json, EmendaPojo.class);

        assertThat(emenda.getRevisoes()).hasSize(1);
        assertThat(emenda.getRevisoes().get(0)).isInstanceOf(RevisaoJustificativaPojo.class);
    }

    @Test
    void desserializaRevisaoTextoLivreSemTypePelaDescricao() throws Exception {
        String json = "{"
                + "\"revisoes\":[{"
                + "\"id\":\"r1\","
                + "\"usuario\":{\"nome\":\"ruan.oliveira\",\"id\":\"60339139307\",\"sigla\":\"R\"},"
                + "\"dataHora\":\"2026-05-28 16:05:00\","
                + "\"descricao\":\"Texto Livre Alterado\""
                + "}]"
                + "}";

        EmendaPojo emenda = new ObjectMapper().readValue(json, EmendaPojo.class);

        assertThat(emenda.getRevisoes()).hasSize(1);
        assertThat(emenda.getRevisoes().get(0)).isInstanceOf(RevisaoTextoLivrePojo.class);
    }

    @Test
    void desserializaRevisaoElementoSemTypePelosCamposProprios() throws Exception {
        String json = "{"
                + "\"revisoes\":[{"
                + "\"id\":\"r1\","
                + "\"usuario\":{\"nome\":\"ruan.oliveira\",\"id\":\"60339139307\",\"sigla\":\"R\"},"
                + "\"dataHora\":\"2026-05-28 16:05:00\","
                + "\"descricao\":\"Dispositivo incluído\","
                + "\"actionType\":\"ADICIONAR_ELEMENTO\","
                + "\"stateType\":\"ElementoIncluido\","
                + "\"elementoAposRevisao\":{}"
                + "}]"
                + "}";

        EmendaPojo emenda = new ObjectMapper().readValue(json, EmendaPojo.class);

        assertThat(emenda.getRevisoes()).hasSize(1);
        assertThat(emenda.getRevisoes().get(0)).isInstanceOf(RevisaoElementoPojo.class);
    }
}
