package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface SequenciaComentario {
    String getId();

    default String getIdDispositivo() {
        return null;
    }

    String getLocal();

    List<? extends Comentario> getComentarios();
}
