package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.Comentario;
import br.gov.lexml.eta.etaservices.emenda.Usuario;

public final class ComentarioRecord implements Comentario {

    private final Usuario usuario;
    private final String dataHora;
    private final String texto;

    public ComentarioRecord(Usuario usuario, String dataHora, String texto) {
        this.usuario = usuario;
        this.dataHora = dataHora;
        this.texto = texto;
    }

    @Override
    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public String getDataHora() {
        return dataHora;
    }

    @Override
    public String getTexto() {
        return texto;
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
        ComentarioRecord other = (ComentarioRecord) obj;
        return Objects.equals(usuario, other.usuario)
                && Objects.equals(dataHora, other.dataHora)
                && Objects.equals(texto, other.texto);
    }

    @Override
    public String toString() {
        return "ComentarioRecord [usuario=" + usuario + ", dataHora=" + dataHora + ", texto=" + texto + "]";
    }
}
