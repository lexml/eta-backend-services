package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class Epigrafe {
    private String texto;
    private String complemento;

    public Epigrafe(
            final String texto,
            final String complemento
    ) {
        this.texto = texto;
        this.complemento = complemento;
    }

    public Epigrafe() {

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
        var that = (Epigrafe) obj;
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
