package br.gov.lexml.eta.etaservices.emenda;

import java.util.List;
import java.util.Objects;

public final class AutoriaRecord implements Autoria {
    private final TipoAutoria tipo;
    private final boolean imprimirPartidoUF;
    private final int quantidadeAssinaturasAdicionaisSenadores;
    private final int quantidadeAssinaturasAdicionaisDeputados;
    private final List<? extends Parlamentar> parlamentares;
    private final ColegiadoAutor colegiado;

    public AutoriaRecord(
            TipoAutoria tipo,
            boolean imprimirPartidoUF,
            int quantidadeAssinaturasAdicionaisSenadores,
            int quantidadeAssinaturasAdicionaisDeputados,
            List<? extends Parlamentar> parlamentares,
            ColegiadoAutor colegiado) {
        this.tipo = tipo;
        this.imprimirPartidoUF = imprimirPartidoUF;
        this.quantidadeAssinaturasAdicionaisSenadores = quantidadeAssinaturasAdicionaisSenadores;
        this.quantidadeAssinaturasAdicionaisDeputados = quantidadeAssinaturasAdicionaisDeputados;
        this.parlamentares = parlamentares;
        this.colegiado = colegiado;
    }

    @Override
    public TipoAutoria getTipo() {
        return tipo;
    }

    @Override
    public boolean isImprimirPartidoUF() {
        return imprimirPartidoUF;
    }

    @Override
    public int getQuantidadeAssinaturasAdicionaisSenadores() {
        return quantidadeAssinaturasAdicionaisSenadores;
    }

    @Override
    public int getQuantidadeAssinaturasAdicionaisDeputados() {
        return quantidadeAssinaturasAdicionaisDeputados;
    }

    @Override
    public List<? extends Parlamentar> getParlamentares() {
        return parlamentares;
    }

    @Override
    public ColegiadoAutor getColegiado() {
        return colegiado;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        AutoriaRecord that = (AutoriaRecord) obj;
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
