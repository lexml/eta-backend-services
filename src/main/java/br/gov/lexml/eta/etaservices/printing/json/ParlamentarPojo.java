package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.Parlamentar;
import br.gov.lexml.eta.etaservices.emenda.Sexo;
import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;

import java.util.Objects;

import static br.gov.lexml.eta.etaservices.emenda.Sexo.M;
import static br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa.CD;

public class ParlamentarPojo implements Parlamentar {
    private String identificacao;
    private String nome;
    private Sexo sexo;
    private String siglaPartido;
    private String siglaUF;
    private SiglaCasaLegislativa siglaCasaLegislativa;
    private String cargo;

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

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public Sexo getSexo() {
        return sexo;
    }

    @SuppressWarnings("unused")
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    @Override
    public String getSiglaPartido() {
        return siglaPartido;
    }

    @SuppressWarnings("unused")
    public void setSiglaPartido(String siglaPartido) {
        this.siglaPartido = siglaPartido;
    }

    @Override
    public String getSiglaUF() {
        return siglaUF;
    }

    @SuppressWarnings("unused")
    public void setSiglaUF(String siglaUf) {
        this.siglaUF = siglaUf;
    }

    @Override
    public SiglaCasaLegislativa getSiglaCasaLegislativa() {
        return siglaCasaLegislativa;
    }

    @SuppressWarnings("unused")
    public void setSiglaCasaLegislativa(SiglaCasaLegislativa siglaCasaLegislativa) {
        this.siglaCasaLegislativa = siglaCasaLegislativa;
    }

    @Override
    public String getCargo() {
        return cargo;
    }

    @SuppressWarnings("unused")
    public void setCargo(String cargo) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParlamentarPojo that = (ParlamentarPojo) o;
        return Objects.equals(identificacao, that.identificacao) && Objects.equals(nome, that.nome) && sexo == that.sexo && Objects.equals(siglaPartido, that.siglaPartido) && Objects.equals(siglaUF, that.siglaUF) && siglaCasaLegislativa == that.siglaCasaLegislativa && Objects.equals(cargo, that.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacao, nome, sexo, siglaPartido, siglaUF, siglaCasaLegislativa, cargo);
    }

    @Override
    public String toString() {
        return "ParlamentarPojo{" +
                "identificacao='" + identificacao + '\'' +
                ", nome='" + nome + '\'' +
                ", sexo=" + sexo +
                ", siglaPartido='" + siglaPartido + '\'' +
                ", siglaUf='" + siglaUF + '\'' +
                ", siglaCasaLegislativa=" + siglaCasaLegislativa +
                ", cargo='" + cargo + '\'' +
                '}';
    }
}
