package br.gov.lexml.eta.etaservices.parecer;

import java.util.List;

import br.gov.lexml.eta.etaservices.printing.json.RevisaoPojo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RevisaoVoto extends RevisaoPojo {
    private List<RevisaoTextoItemVoto> itensTexto;
}