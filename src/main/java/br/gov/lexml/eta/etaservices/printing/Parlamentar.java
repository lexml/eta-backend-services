package br.gov.lexml.eta.etaservices.printing;

import static br.gov.lexml.eta.etaservices.printing.Sexo.M;
import static br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa.CD;

public record Parlamentar(
    String identificacao,
    String nome,
    Sexo sexo,
    String siglaPartido,
    String siglaUf,
    SiglaCasaLegislativa siglaCasaLegislativa,
    String cargo) {

    public String tratamento() {
        return siglaCasaLegislativa == CD ? tratamentoCamara() : tratamentoSenado();
    }

    private String tratamentoCamara() {
        return sexo == M ? "Deputado" : "Deputada";
    }
    private String tratamentoSenado() {
        return sexo == M ? "Senador" : "Senadora";
    }

}
