package br.gov.lexml.eta.etaservices.emenda;

public interface Parlamentar {
    String getTratamento();

    String getIdentificacao();

    String getNome();

    Sexo getSexo();

    String getSiglaPartido();

    String getSiglaUF();

    SiglaCasaLegislativa getSiglaCasaLegislativa();

    String getCargo();
}
