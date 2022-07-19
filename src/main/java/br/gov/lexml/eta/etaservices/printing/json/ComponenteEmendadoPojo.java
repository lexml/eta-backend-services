package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.printing.DispositivosEmendaRecord;

import java.util.Objects;

public class ComponenteEmendadoPojo implements ComponenteEmendado {
    private String urn;
    private boolean articulado;
    private String rotuloAnexo;
    private String tituloAnexo;
    private DispositivosEmendaRecord dispositivos;

    @Override
    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    @Override
    public boolean isArticulado() {
        return articulado;
    }

    public void setArticulado(boolean articulado) {
        this.articulado = articulado;
    }

    @Override
    public String getRotuloAnexo() {
        return rotuloAnexo;
    }

    public void setRotuloAnexo(String rotuloAnexo) {
        this.rotuloAnexo = rotuloAnexo;
    }

    @Override
    public String getTituloAnexo() {
        return tituloAnexo;
    }

    public void setTituloAnexo(String tituloAnexo) {
        this.tituloAnexo = tituloAnexo;
    }

    @Override
    public DispositivosEmendaRecord getDispositivos() {
        return dispositivos;
    }

    public void setDispositivos(DispositivosEmendaRecord dispositivos) {
        this.dispositivos = dispositivos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponenteEmendadoPojo that = (ComponenteEmendadoPojo) o;
        return articulado == that.articulado && urn.equals(that.urn) && rotuloAnexo.equals(that.rotuloAnexo) && tituloAnexo.equals(that.tituloAnexo) && dispositivos.equals(that.dispositivos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, articulado, rotuloAnexo, tituloAnexo, dispositivos);
    }

    @Override
    public String toString() {
        return "ComponenteEmendadoPojo{" +
                "urn='" + urn + '\'' +
                ", articulado=" + articulado +
                ", rotuloAnexo='" + rotuloAnexo + '\'' +
                ", tituloAnexo='" + tituloAnexo + '\'' +
                ", dispositivos=" + dispositivos +
                '}';
    }
}
