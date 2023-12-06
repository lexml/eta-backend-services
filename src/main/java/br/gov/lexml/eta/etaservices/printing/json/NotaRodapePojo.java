package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import br.gov.lexml.eta.etaservices.emenda.NotaRodape;

@XmlRootElement(name = "NotaRodape")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotaRodapePojo implements NotaRodape {

	@XmlAttribute
	private String id;
	@XmlAttribute
	private Integer numero;
	@XmlAttribute
	private String texto;
	
	@Override
	public String getId() {
		return id;
	}
	@Override
	public Integer getNumero() {
		return numero;
	}
	@Override
	public String getTexto() {
		return texto;
	}
		
}
