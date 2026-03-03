package br.gov.lexml.eta.etaservices.parecer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comissao {
    public String sigla;
    public String nome;
}
