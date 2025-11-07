package br.gov.lexml.eta.etaservices.parecer;

import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comissao {
    public SiglaCasaLegislativa siglaCasaLegislativa;
    public String sigla;
    public String nome;
}
