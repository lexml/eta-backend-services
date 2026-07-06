package br.gov.lexml.eta.etaservices.printing.json;

import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RevisaoJustificativa")
@NoArgsConstructor
public class RevisaoJustificativaPojo extends RevisaoPojo {

    public RevisaoJustificativaPojo(String type, String id, UsuarioPojo usuario, String dataHora, String descricao) {
        super(type, id, usuario, dataHora, descricao);
    }

}
