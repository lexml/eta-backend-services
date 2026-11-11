package br.gov.lexml.eta.etaservices.printing.pdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.printing.json.ComentarioPojo;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.SequenciaComentarioPojo;
import br.gov.lexml.eta.etaservices.printing.json.UsuarioPojo;

class VelocityTemplateProcessorComentariosTest {

    private static final String ALERTA_COMENTARIOS =
            "Este documento cont\u00e9m coment\u00e1rios e n\u00e3o deve ser protocolado at\u00e9 que sejam removidos.";

    @Test
    void deveApresentarAlertaDeComentariosQuandoExistiremComentarios() throws IOException {
        EmendaPojo emenda = readEmendaFile();
        emenda.setSequenciasComentario(List.of(criaSequenciaComentario()));

        String templateResult = new VelocityTemplateProcessor(new TemplateLoaderBean()).getTemplateResult(emenda);

        assertThat(templateResult).contains(ALERTA_COMENTARIOS);
    }

    @Test
    void naoDeveApresentarAlertaDeComentariosQuandoNaoExistiremComentarios() throws IOException {
        EmendaPojo emenda = readEmendaFile();
        emenda.setSequenciasComentario(Collections.emptyList());

        String templateResult = new VelocityTemplateProcessor(new TemplateLoaderBean()).getTemplateResult(emenda);

        assertThat(templateResult).doesNotContain(ALERTA_COMENTARIOS);
    }

    private EmendaPojo readEmendaFile() {
        try {
            URL sourceUrl = getClass().getClassLoader().getResource("test1.json");
            assert sourceUrl != null;

            File sourceFile = new File(sourceUrl.getFile());
            String json = FileUtils.readFileToString(sourceFile, UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            EmendaPojo emenda = objectMapper.readValue(json, EmendaPojo.class);
            emenda.setRevisoes(Collections.emptyList());
            emenda.setPendenciasPreenchimento(Collections.emptyList());
            return emenda;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SequenciaComentarioPojo criaSequenciaComentario() {
        UsuarioPojo usuario = new UsuarioPojo();
        usuario.setNome("fragomeni");
        usuario.setId("fragomeni");
        usuario.setSigla("F");

        ComentarioPojo comentario = new ComentarioPojo();
        comentario.setUsuario(usuario);
        comentario.setDataHora("2026-05-18 15:48:26");
        comentario.setTexto("Comentario para alerta no PDF.");

        SequenciaComentarioPojo sequencia = new SequenciaComentarioPojo();
        sequencia.setId("sc999999999");
        sequencia.setLocal("justifica\u00e7\u00e3o");
        sequencia.setComentarios(List.of(comentario));
        return sequencia;
    }
}
