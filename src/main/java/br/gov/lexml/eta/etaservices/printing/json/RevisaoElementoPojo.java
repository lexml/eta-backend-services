package br.gov.lexml.eta.etaservices.printing.json;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import br.gov.lexml.eta.etaservices.emenda.RevisaoElemento;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "RevisaoElemento")
@XmlAccessorType(XmlAccessType.FIELD)
public class RevisaoElementoPojo extends RevisaoPojo implements RevisaoElemento {
	@XmlAttribute
    private String actionType;
	@XmlAttribute
    private String stateType;
	@XmlElement(name = "ElementoAntesRevisao")
    private ElementoPojo elementoAntesRevisao;
	@XmlElement(name = "ElementoAposRevisao")
    private ElementoPojo elementoAposRevisao;
    @XmlAttribute
    private String idRevisaoElementoPai;
    @XmlAttribute
    private String idRevisaoElementoPrincipal;
}
