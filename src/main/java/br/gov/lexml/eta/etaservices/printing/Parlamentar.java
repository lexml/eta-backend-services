package br.gov.lexml.eta.etaservices.printing;

public record Parlamentar(
        String identificacao,
        String nome,
        Sexo sexo,
        String siglaPartido,
        String siglaUf,
        SiglaCasaLegislativa siglaCasaLegislativa,
        String cargo
) {
}
