package br.gov.lexml.eta.etaservices.emenda;

import java.util.Objects;

import static br.gov.lexml.eta.etaservices.emenda.Sexo.M;
import static br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa.CD;

public final class ParlamentarRecord implements Parlamentar {
    private final String identificacao;
    private final String nome;
    private final Sexo sexo;
    private final String siglaPartido;
    private final String siglaUf;
    private final SiglaCasaLegislativa siglaCasaLegislativa;
    private final String cargo;

    public ParlamentarRecord(
            String identificacao,
            String nome,
            Sexo sexo,
            String siglaPartido,
            String siglaUf,
            SiglaCasaLegislativa siglaCasaLegislativa,
            String cargo) {
        this.identificacao = identificacao;
        this.nome = nome;
        this.sexo = sexo;
        this.siglaPartido = siglaPartido;
        this.siglaUf = siglaUf;
        this.siglaCasaLegislativa = siglaCasaLegislativa;
        this.cargo = cargo;
    }

    @Override
    public String getTratamento() {
        return siglaCasaLegislativa == CD ? tratamentoCamara() : tratamentoSenado();
    }

    private String tratamentoCamara() {
        return sexo == M ? "Deputado" : "Deputada";
    }

    private String tratamentoSenado() {
        return sexo == M ? "Senador" : "Senadora";
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

    public String getSiglaUF() {
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
        ParlamentarRecord that = (ParlamentarRecord) obj;
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
