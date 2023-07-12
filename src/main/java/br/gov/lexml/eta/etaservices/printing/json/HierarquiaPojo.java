package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import br.gov.lexml.eta.etaservices.emenda.Hierarquia;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class HierarquiaPojo implements Hierarquia {
	@XmlElement(name = "Pai")
	private ReferenciaPojo pai;
	@XmlAttribute
	private Integer posicao;
	@XmlAttribute
	private String numero;
}
