package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class ColegiadoAutor {
    private String identificacao;
    private String nome;
    private String sigla;

    public ColegiadoAutor(
            final String identificacao,
            final String nome,
            final String sigla
    ) {
        this.identificacao = identificacao;
        this.nome = nome;
        this.sigla = sigla;
    }

    public ColegiadoAutor() {

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
        var that = (ColegiadoAutor) obj;
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
