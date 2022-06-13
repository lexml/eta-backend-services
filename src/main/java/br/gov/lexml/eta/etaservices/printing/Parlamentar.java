package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class Parlamentar {
    private String identificacao;
    private String nome;
    private Sexo sexo;
    private String siglaPartido;
    private String siglaUf;
    private SiglaCasaLegislativa siglaCasaLegislativa;
    private String cargo;

    public Parlamentar(
            final String identificacao,
            final String nome,
            final Sexo sexo,
            final String siglaPartido,
            final String siglaUf,
            final SiglaCasaLegislativa siglaCasaLegislativa,
            final String cargo
    ) {
        this.identificacao = identificacao;
        this.nome = nome;
        this.sexo = sexo;
        this.siglaPartido = siglaPartido;
        this.siglaUf = siglaUf;
        this.siglaCasaLegislativa = siglaCasaLegislativa;
        this.cargo = cargo;
    }

    public Parlamentar() {
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public String getNome() {
        return nome;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public String getSiglaPartido() {
        return siglaPartido;
    }

    public String getSiglaUf() {
        return siglaUf;
    }

    public SiglaCasaLegislativa getSiglaCasaLegislativa() {
        return siglaCasaLegislativa;
    }

    public String getCargo() {
        return cargo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Parlamentar) obj;
        return Objects.equals(this.identificacao, that.identificacao) &&
                Objects.equals(this.nome, that.nome) &&
                Objects.equals(this.sexo, that.sexo) &&
                Objects.equals(this.siglaPartido, that.siglaPartido) &&
                Objects.equals(this.siglaUf, that.siglaUf) &&
                Objects.equals(this.siglaCasaLegislativa, that.siglaCasaLegislativa) &&
                Objects.equals(this.cargo, that.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacao, nome, sexo, siglaPartido, siglaUf, siglaCasaLegislativa, cargo);
    }

    @Override
    public String toString() {
        return "Parlamentar[" +
                "identificacao=" + identificacao + ", " +
                "nome=" + nome + ", " +
                "sexo=" + sexo + ", " +
                "siglaPartido=" + siglaPartido + ", " +
                "siglaUf=" + siglaUf + ", " +
                "siglaCasaLegislativa=" + siglaCasaLegislativa + ", " +
                "cargo=" + cargo + ']';
    }

}
