package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.ComandoEmenda;
import br.gov.lexml.eta.etaservices.printing.ItemComandoEmenda;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComandoEmendaPojo implements ComandoEmenda {
    private String cabecalhoComum;
    private List<? extends ItemComandoEmendaPojo> comandos = new ArrayList<>();

    @Override
    public String getCabecalhoComum() {
        return cabecalhoComum;
    }

    @SuppressWarnings("unused")
    public void setCabecalhoComum(String cabecalhoComum) {
        this.cabecalhoComum = cabecalhoComum;
    }

    @Override
    public List<? extends ItemComandoEmenda> getComandos() {
        return comandos;
    }

    @SuppressWarnings("unused")
    public void setComandos(List<? extends ItemComandoEmendaPojo> comandos) {
        this.comandos = comandos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComandoEmendaPojo that = (ComandoEmendaPojo) o;
        return cabecalhoComum.equals(that.cabecalhoComum) && comandos.equals(that.comandos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cabecalhoComum, comandos);
    }

    @Override
    public String toString() {
        return "ComandoEmendaPojo{" +
                "cabecalhoComum='" + cabecalhoComum + '\'' +
                ", comandos=" + comandos +
                '}';
    }
}
