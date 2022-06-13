package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Objects;

public final class ComandoEmenda {
    private String cabecalhoComum;
    private List<ItemComandoEmenda> comandos;

    public ComandoEmenda(
            final String cabecalhoComum,
            final List<ItemComandoEmenda> comandos
    ) {
        this.cabecalhoComum = cabecalhoComum;
        this.comandos = comandos;
    }

    public String getCabecalhoComum() {
        return cabecalhoComum;
    }

    public List<ItemComandoEmenda> getComandos() {
        return comandos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComandoEmenda) obj;
        return Objects.equals(this.cabecalhoComum, that.cabecalhoComum) &&
                Objects.equals(this.comandos, that.comandos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cabecalhoComum, comandos);
    }

    @Override
    public String toString() {
        return "ComandoEmenda[" +
                "cabecalhoComum=" + cabecalhoComum + ", " +
                "comandos=" + comandos + ']';
    }

}
