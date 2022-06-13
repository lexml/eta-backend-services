package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class ComponenteEmendado {
    private String urn;
    private boolean articulado;
    private String rotuloAnexo;
    private String tituloAnexo;
    private DispositivosEmenda dispositivos;

    public ComponenteEmendado(
            final String urn,
            final boolean articulado,
            final String rotuloAnexo,
            final String tituloAnexo,
            final DispositivosEmenda dispositivos
    ) {
        this.urn = urn;
        this.articulado = articulado;
        this.rotuloAnexo = rotuloAnexo;
        this.tituloAnexo = tituloAnexo;
        this.dispositivos = dispositivos;
    }

    public ComponenteEmendado() {
    }

    public String getUrn() {
        return urn;
    }

    public boolean getArticulado() {
        return articulado;
    }

    public String getRotuloAnexo() {
        return rotuloAnexo;
    }

    public String getTituloAnexo() {
        return tituloAnexo;
    }

    public DispositivosEmenda getDispositivos() {
        return dispositivos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponenteEmendado) obj;
        return Objects.equals(this.urn, that.urn) &&
                this.articulado == that.articulado &&
                Objects.equals(this.rotuloAnexo, that.rotuloAnexo) &&
                Objects.equals(this.tituloAnexo, that.tituloAnexo) &&
                Objects.equals(this.dispositivos, that.dispositivos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, articulado, rotuloAnexo, tituloAnexo, dispositivos);
    }

    @Override
    public String toString() {
        return "ComponenteEmendado[" +
                "urn=" + urn + ", " +
                "articulado=" + articulado + ", " +
                "rotuloAnexo=" + rotuloAnexo + ", " +
                "tituloAnexo=" + tituloAnexo + ", " +
                "dispositivos=" + dispositivos + ']';
    }

}
