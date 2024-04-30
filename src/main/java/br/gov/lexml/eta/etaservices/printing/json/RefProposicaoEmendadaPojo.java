package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;

import java.util.Objects;

public class RefProposicaoEmendadaPojo implements RefProposicaoEmendada {
    private String urn;
    private String sigla;
    private String numero;
    private String ano;
    private String ementa;
    private String identificacaoTexto;
    private String emendarTextoSubstitutivo;

    @Override
    public String getUrn() {
        return urn;
    }

    @SuppressWarnings("unused")
    public void setUrn(String urn) {
        this.urn = urn;
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
    public String getNumero() {
        return numero;
    }

    @SuppressWarnings("unused")
    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String getAno() {
        return ano;
    }

    @SuppressWarnings("unused")
    public void setAno(String ano) {
        this.ano = ano;
    }

    @Override
    public String getEmenta() {
        return ementa;
    }

    @SuppressWarnings("unused")
    public void setEmenta(String ementa) {
        this.ementa = ementa;
    }

    @Override
    public String getIdentificacaoTexto() {
        return identificacaoTexto;
    }

    @SuppressWarnings("unused")
    public void setIdentificacaoTexto(String identificacaoTexto) {
        this.identificacaoTexto = identificacaoTexto;
    }

    @Override
    public String getEmendarTextoSubstitutivo() {
        return this.emendarTextoSubstitutivo;
    }

    @SuppressWarnings("unused")
    public void setEmendarTextoSubstitutivo(String emendarTextoSubstitutivo) {
        this.emendarTextoSubstitutivo = emendarTextoSubstitutivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefProposicaoEmendadaPojo that = (RefProposicaoEmendadaPojo) o;
        return Objects.equals(urn, that.urn) && Objects.equals(sigla, that.sigla) && Objects.equals(numero, that.numero) && Objects.equals(ano, that.ano) && Objects.equals(ementa, that.ementa) && Objects.equals(identificacaoTexto, that.identificacaoTexto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, sigla, numero, ano, ementa, identificacaoTexto);
    }

    @Override
    public String toString() {
        return "RefProposicaoEmendadaPojo{" +
                "urn='" + urn + '\'' +
                ", sigla='" + sigla + '\'' +
                ", numero='" + numero + '\'' +
                ", ano='" + ano + '\'' +
                ", ementa='" + ementa + '\'' +
                ", identificacaoTexto='" + identificacaoTexto + '\'' +
                '}';
    }
}
