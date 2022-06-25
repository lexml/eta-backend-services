package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.ColegiadoAutor;

import java.util.Objects;

public final class ColegiadoAutorRecord implements ColegiadoAutor {
    private final String identificacao;
    private final String nome;
    private final String sigla;

    public ColegiadoAutorRecord(String identificacao, String nome, String sigla) {
        this.identificacao = identificacao;
        this.nome = nome;
        this.sigla = sigla;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ColegiadoAutorRecord that = (ColegiadoAutorRecord) obj;
        return Objects.equals(this.identificacao, that.identificacao) &&
                Objects.equals(this.nome, that.nome) &&
                Objects.equals(this.sigla, that.sigla);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacao, nome, sigla);
    }

    @Override
    public String toString() {
        return "ColegiadoAutor[" +
                "identificacao=" + identificacao + ", " +
                "nome=" + nome + ", " +
                "sigla=" + sigla + ']';
    }

}
