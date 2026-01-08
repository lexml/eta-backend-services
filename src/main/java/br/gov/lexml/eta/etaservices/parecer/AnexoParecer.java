package br.gov.lexml.eta.etaservices.parecer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnexoParecer {
    private String idArquivo;
    private String nomeArquivo;
    private String nomeDocumento;
    private String tipo;
    private String mimeType;
}
