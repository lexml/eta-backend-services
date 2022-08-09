package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.ColegiadoAutor;

import java.util.Objects;

public class ColegiadoAutorPojo implements ColegiadoAutor {
    private String identificacao;
    private String nome;
    private String sigla;

    @Override
    public String getIdentificacao() {
        return identificacao;
    }

    @SuppressWarnings("unused")
    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @SuppressWarnings("unused")
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String getSigla() {
        return sigla;
    }

    @SuppressWarnings("unused")
    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColegiadoAutorPojo that = (ColegiadoAutorPojo) o;
        return identificacao.equals(that.identificacao) && nome.equals(that.nome) && sigla.equals(that.sigla);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacao, nome, sigla);
    }
}
