package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.gov.lexml.eta.etaservices.emenda.Revisao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = RevisaoElementoPojo.class, name = "RevisaoElemento"),
	@JsonSubTypes.Type(value = RevisaoJustificativaPojo.class, name = "RevisaoJustificativa"),
	@JsonSubTypes.Type(value = RevisaoTextoLivrePojo.class, name = "RevisaoTextoLivre")
})
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
@NoArgsConstructor
public abstract class RevisaoPojo implements Revisao {
	@XmlAttribute
    String type = getClass().getSimpleName().replace("Pojo", "");
	@XmlAttribute
    String id;
    @XmlElement(name = "Usuario")
    UsuarioPojo usuario;
	@XmlAttribute
    String dataHora;
	@XmlAttribute
    String descricao;
}
