package br.gov.lexml.eta.etaservices.parecer;

import br.gov.lexml.eta.etaservices.emenda.Anexo;
import br.gov.lexml.eta.etaservices.printing.pdf.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentoVoto implements Anexo {

    private TipoDocumento tipo;
    private String nomeArquivo;
    private String base64;

}
