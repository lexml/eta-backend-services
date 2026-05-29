package br.gov.lexml.eta.etaservices.printing.json;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.gov.lexml.eta.etaservices.emenda.SequenciaComentario;

@XmlRootElement(name = "SequenciaComentario")
@XmlAccessorType(XmlAccessType.FIELD)
public class SequenciaComentarioPojo implements SequenciaComentario {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String local;

    @XmlElement(name = "Comentario")
    private List<ComentarioPojo> comentarios;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    @Override
    public List<ComentarioPojo> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ComentarioPojo> comentarios) {
        this.comentarios = comentarios;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, local, comentarios);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SequenciaComentarioPojo other = (SequenciaComentarioPojo) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(local, other.local)
                && Objects.equals(comentarios, other.comentarios);
    }
}
