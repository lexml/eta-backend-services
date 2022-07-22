package br.gov.lexml.eta.etaservices.printing;

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
