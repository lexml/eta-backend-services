package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.List;
import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.Comentario;
import br.gov.lexml.eta.etaservices.emenda.SequenciaComentario;

public final class SequenciaComentarioRecord implements SequenciaComentario {

    private final String id;
    private final String idDispositivo;
    private final String local;
    private final List<? extends Comentario> comentarios;

    public SequenciaComentarioRecord(String id, String local, List<? extends Comentario> comentarios) {
        this(id, null, local, comentarios);
    }

    public SequenciaComentarioRecord(String id, String idDispositivo, String local, List<? extends Comentario> comentarios) {
        this.id = id;
        this.idDispositivo = idDispositivo;
        this.local = local;
        this.comentarios = comentarios;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getIdDispositivo() {
        return idDispositivo;
    }

    @Override
    public String getLocal() {
        return local;
    }

    @Override
    public List<? extends Comentario> getComentarios() {
        return comentarios;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idDispositivo, local, comentarios);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SequenciaComentarioRecord other = (SequenciaComentarioRecord) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(idDispositivo, other.idDispositivo)
                && Objects.equals(local, other.local)
                && Objects.equals(comentarios, other.comentarios);
    }

    @Override
    public String toString() {
        return "SequenciaComentarioRecord [id=" + id + ", idDispositivo=" + idDispositivo + ", local=" + local + ", comentarios=" + comentarios + "]";
    }
}
