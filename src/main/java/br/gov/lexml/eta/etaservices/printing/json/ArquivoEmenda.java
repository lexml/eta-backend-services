package br.gov.lexml.eta.etaservices.printing.json;

import java.util.Objects;

public class ArquivoEmenda {
    private EmendaPojo emenda;

    public EmendaPojo getEmenda() {
        return emenda;
    }

    public void setEmenda(EmendaPojo emenda) {
        this.emenda = emenda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArquivoEmenda that = (ArquivoEmenda) o;
        return Objects.equals(emenda, that.emenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emenda);
    }

    @Override
    public String toString() {
        return "ArquivoEmenda{" +
                "emenda=" + emenda +
                '}';
    }
}
