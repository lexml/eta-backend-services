package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.Epigrafe;

import java.util.Objects;

public final class EpigrafeRecord implements Epigrafe {
    private final String texto;
    private final String complemento;

    public EpigrafeRecord(String texto, String complemento) {
        this.texto = texto;
        this.complemento = complemento;
    }

    public String getTexto() {
        return texto;
    }

    public String getComplemento() {
        return complemento;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EpigrafeRecord) obj;
        return Objects.equals(this.texto, that.texto) &&
                Objects.equals(this.complemento, that.complemento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, complemento);
    }

    @Override
    public String toString() {
        return "Epigrafe[" +
                "texto=" + texto + ", " +
                "complemento=" + complemento + ']';
    }


}
