package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.Epigrafe;

import java.util.Objects;

public class EpigrafePojo implements Epigrafe {
    private String texto;
    private String complemento;

    @Override
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpigrafePojo that = (EpigrafePojo) o;
        return texto.equals(that.texto) && complemento.equals(that.complemento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, complemento);
    }

    @Override
    public String toString() {
        return "EpigrafePojo{" +
                "texto='" + texto + '\'' +
                ", complemento='" + complemento + '\'' +
                '}';
    }
}

