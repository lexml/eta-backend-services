package br.gov.lexml.eta.etaservices.parecer;

import br.gov.lexml.eta.etaservices.printing.json.RevisaoTextoLivrePojo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RevisaoTextoItemVoto extends RevisaoTextoLivrePojo {
    private Integer posicao;
}