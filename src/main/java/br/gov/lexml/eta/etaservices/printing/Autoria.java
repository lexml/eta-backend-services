package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Objects;

public final class Autoria {
    private TipoAutoria tipo;
    private boolean imprimirPartidoUF;
    private int quantidadeAssinaturasAdicionaisSenadores;
    private int quantidadeAssinaturasAdicionaisDeputados;
    private List<Parlamentar> parlamentares;
    private ColegiadoAutor colegiado;

    public Autoria(
            final TipoAutoria tipo,
            final boolean imprimirPartidoUF,
            final int quantidadeAssinaturasAdicionaisSenadores,
            final int quantidadeAssinaturasAdicionaisDeputados,
            final List<Parlamentar> parlamentares,
            final ColegiadoAutor colegiado

    ) {
        this.tipo = tipo;
        this.imprimirPartidoUF = imprimirPartidoUF;
        this.quantidadeAssinaturasAdicionaisSenadores = quantidadeAssinaturasAdicionaisSenadores;
        this.quantidadeAssinaturasAdicionaisDeputados = quantidadeAssinaturasAdicionaisDeputados;
        this.parlamentares = parlamentares;
        this.colegiado = colegiado;
    }

    public Autoria() {

    }

    public TipoAutoria Tipo() {
        return tipo;
    }

    public boolean getImprimirPartidoUF() {
        return imprimirPartidoUF;
    }

    public int getQuantidadeAssinaturasAdicionaisSenadores() {
        return quantidadeAssinaturasAdicionaisSenadores;
    }

    public int getQuantidadeAssinaturasAdicionaisDeputados() {
        return quantidadeAssinaturasAdicionaisDeputados;
    }

    public List<Parlamentar> getParlamentares() {
        return parlamentares;
    }

    public ColegiadoAutor getColegiado() {
        return colegiado;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Autoria) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                this.imprimirPartidoUF == that.imprimirPartidoUF &&
                this.quantidadeAssinaturasAdicionaisSenadores == that.quantidadeAssinaturasAdicionaisSenadores &&
                this.quantidadeAssinaturasAdicionaisDeputados == that.quantidadeAssinaturasAdicionaisDeputados &&
                Objects.equals(this.parlamentares, that.parlamentares) &&
                Objects.equals(this.colegiado, that.colegiado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, imprimirPartidoUF, quantidadeAssinaturasAdicionaisSenadores, quantidadeAssinaturasAdicionaisDeputados, parlamentares, colegiado);
    }

    @Override
    public String toString() {
        return "Autoria[" +
                "tipo=" + tipo + ", " +
                "imprimirPartidoUF=" + imprimirPartidoUF + ", " +
                "quantidadeAssinaturasAdicionaisSenadores=" + quantidadeAssinaturasAdicionaisSenadores + ", " +
                "quantidadeAssinaturasAdicionaisDeputados=" + quantidadeAssinaturasAdicionaisDeputados + ", " +
                "parlamentares=" + parlamentares + ", " +
                "colegiado=" + colegiado + ']';
    }

}
