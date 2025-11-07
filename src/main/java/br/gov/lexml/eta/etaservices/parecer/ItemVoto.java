package br.gov.lexml.eta.etaservices.parecer;

import java.util.List;

import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemVoto {
    private String texto;
    private List<? extends NotaRodapePojo> notasRodape;
    private DocumentoVoto documento;
    private Integer posicao;
}
