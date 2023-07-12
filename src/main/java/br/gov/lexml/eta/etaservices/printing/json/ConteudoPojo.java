package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import br.gov.lexml.eta.etaservices.emenda.Conteudo;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ConteudoPojo implements Conteudo {
	@XmlElement(name = "Texto")
	private String texto;
}
