package br.gov.lexml.eta.etaservices.printing.json;

import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RevisaoTextoLivre")
@NoArgsConstructor
public class RevisaoTextoLivrePojo extends RevisaoPojo {

    public RevisaoTextoLivrePojo(String type, String id, UsuarioPojo usuario, String dataHora, String descricao) {
        super(type, id, usuario, dataHora, descricao);
    }
}
