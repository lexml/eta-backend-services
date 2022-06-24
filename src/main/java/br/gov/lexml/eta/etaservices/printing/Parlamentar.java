package br.gov.lexml.eta.etaservices.printing;

public interface Parlamentar {
    String getTratamento();

    String getIdentificacao();

    String getNome();

    Sexo getSexo();

    String getSiglaPartido();

    String getSiglaUf();

    SiglaCasaLegislativa getSiglaCasaLegislativa();

    String getCargo();
}
