package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.Autoria;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoAutor;
import br.gov.lexml.eta.etaservices.emenda.Parlamentar;
import br.gov.lexml.eta.etaservices.emenda.TipoAutoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoriaPojo implements Autoria {

    private TipoAutoria tipo;
    private boolean imprimirPartidoUF;
    private int quantidadeAssinaturasAdicionaisDeputados;
    private int quantidadeAssinaturasAdicionaisSenadores;
    private List<? extends ParlamentarPojo> parlamentares = new ArrayList<>();
    private ColegiadoAutorPojo colegiado;

    @Override
    public TipoAutoria getTipo() {
        return tipo;
    }

    @SuppressWarnings("unused")
    public void setTipo(TipoAutoria tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean isImprimirPartidoUF() {
        return imprimirPartidoUF;
    }

    @SuppressWarnings("unused")
    public void setImprimirPartidoUF(boolean imprimirPartidoUF) {
        this.imprimirPartidoUF = imprimirPartidoUF;
    }

    @Override
    public int getQuantidadeAssinaturasAdicionaisDeputados() {
        return quantidadeAssinaturasAdicionaisDeputados;
    }

    @SuppressWarnings("unused")
    public void setQuantidadeAssinaturasAdicionaisDeputados(int quantidadeAssinaturasAdicionaisDeputados) {
        this.quantidadeAssinaturasAdicionaisDeputados = quantidadeAssinaturasAdicionaisDeputados;
    }

    @Override
    public int getQuantidadeAssinaturasAdicionaisSenadores() {
        return quantidadeAssinaturasAdicionaisSenadores;
    }

    @SuppressWarnings("unused")
    public void setQuantidadeAssinaturasAdicionaisSenadores(int quantidadeAssinaturasAdicionaisSenadores) {
        this.quantidadeAssinaturasAdicionaisSenadores = quantidadeAssinaturasAdicionaisSenadores;
    }

    @Override
    public List<? extends Parlamentar> getParlamentares() {
        return parlamentares;
    }

    @SuppressWarnings("unused")
    public void setParlamentares(List<? extends ParlamentarPojo> parlamentares) {
        this.parlamentares = parlamentares;
    }

    @Override
    public ColegiadoAutor getColegiado() {
        return colegiado;
    }

    @SuppressWarnings("unused")
    public void setColegiado(ColegiadoAutorPojo colegiado) {
        this.colegiado = colegiado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoriaPojo that = (AutoriaPojo) o;
        return imprimirPartidoUF == that.imprimirPartidoUF && quantidadeAssinaturasAdicionaisDeputados == that.quantidadeAssinaturasAdicionaisDeputados && quantidadeAssinaturasAdicionaisSenadores == that.quantidadeAssinaturasAdicionaisSenadores && tipo == that.tipo && parlamentares.equals(that.parlamentares) && colegiado.equals(that.colegiado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, imprimirPartidoUF, quantidadeAssinaturasAdicionaisDeputados, quantidadeAssinaturasAdicionaisSenadores, parlamentares, colegiado);
    }

    @Override
    public String toString() {
        return "AutoriaPojo{" +
                "tipo=" + tipo +
                ", imprimirPartidoUF=" + imprimirPartidoUF +
                ", quantidadeAssinaturasAdicionaisDeputados=" + quantidadeAssinaturasAdicionaisDeputados +
                ", quantidadeAssinaturasAdicionaisSenadores=" + quantidadeAssinaturasAdicionaisSenadores +
                ", parlamentares=" + parlamentares +
                ", colegiado=" + colegiado +
                '}';
    }
}
