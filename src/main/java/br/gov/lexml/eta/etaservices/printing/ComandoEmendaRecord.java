package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Objects;

public final class ComandoEmendaRecord implements ComandoEmenda {
    private final String cabecalhoComum;
    private final List<? extends ItemComandoEmenda> comandos;

    public ComandoEmendaRecord(String cabecalhoComum, List<? extends ItemComandoEmenda> comandos) {
        this.cabecalhoComum = cabecalhoComum;
        this.comandos = comandos;
    }

    public String getCabecalhoComum() {
        return cabecalhoComum;
    }

    public List<? extends ItemComandoEmenda> getComandos() {
        return comandos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ComandoEmendaRecord that = (ComandoEmendaRecord) obj;
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
