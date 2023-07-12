package br.gov.lexml.eta.etaservices.printing.json;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import br.gov.lexml.eta.etaservices.emenda.Usuario;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class UsuarioPojo implements Usuario {
	@XmlAttribute
    private String nome;
	@XmlAttribute
    private String id;
	@XmlAttribute
    private String sigla;
}
