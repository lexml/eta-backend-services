package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.ComponenteEmendado;

import java.util.Objects;

public final class ComponenteEmendadoRecord implements ComponenteEmendado {
    private final String urn;
    private final boolean articulado;
    private final String rotuloAnexo;
    private final String tituloAnexo;
    private final DispositivosEmendaRecord dispositivos;

    public ComponenteEmendadoRecord(
            String urn,
            boolean articulado,
            String rotuloAnexo,
            String tituloAnexo,
            DispositivosEmendaRecord dispositivos) {
        this.urn = urn;
        this.articulado = articulado;
        this.rotuloAnexo = rotuloAnexo;
        this.tituloAnexo = tituloAnexo;
        this.dispositivos = dispositivos;
    }

    @Override
    public String getUrn() {
        return urn;
    }

    @Override
    public boolean isArticulado() {
        return articulado;
    }

    @Override
    public String getRotuloAnexo() {
        return rotuloAnexo;
    }

    @Override
    public String getTituloAnexo() {
        return tituloAnexo;
    }

    @Override
    public DispositivosEmendaRecord getDispositivos() {
        return dispositivos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponenteEmendadoRecord) obj;
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
