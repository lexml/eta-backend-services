package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;

public interface SequenciaComentario {
    String getId();

    String getLocal();

    List<? extends Comentario> getComentarios();
}
