package br.gov.lexml.eta.etaservices.printing.json;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.gov.lexml.eta.etaservices.emenda.Comentario;
import br.gov.lexml.eta.etaservices.emenda.Usuario;

@XmlRootElement(name = "Comentario")
@XmlAccessorType(XmlAccessType.FIELD)
public class ComentarioPojo implements Comentario {

    @XmlElement(name = "Usuario")
    private UsuarioPojo usuario;

    @XmlAttribute
    private String dataHora;

    @XmlElement(name = "Texto")
    private String texto;

    @Override
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioPojo usuario) {
        this.usuario = usuario;
    }

    @Override
    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, dataHora, texto);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ComentarioPojo other = (ComentarioPojo) obj;
        return Objects.equals(usuario, other.usuario)
                && Objects.equals(dataHora, other.dataHora)
                && Objects.equals(texto, other.texto);
    }
}
