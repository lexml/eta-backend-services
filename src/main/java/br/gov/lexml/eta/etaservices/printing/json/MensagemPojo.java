package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import br.gov.lexml.eta.etaservices.emenda.Mensagem;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class MensagemPojo implements Mensagem {
	@XmlAttribute
	private String tipo;
	@XmlAttribute
	private String descricao;
	@XmlAttribute
	private Boolean fix;
}
