package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import br.gov.lexml.eta.etaservices.emenda.Revisao;
import br.gov.lexml.eta.etaservices.parecer.RevisaoAnalise;
import br.gov.lexml.eta.etaservices.parecer.RevisaoEmenta;
import br.gov.lexml.eta.etaservices.parecer.RevisaoRelatorio;
import br.gov.lexml.eta.etaservices.parecer.RevisaoTextoItemVoto;
import br.gov.lexml.eta.etaservices.parecer.RevisaoVoto;
import lombok.Data;

@Data
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RevisaoElementoPojo.class, name = "RevisaoElemento"), @JsonSubTypes.Type(value = RevisaoJustificativaPojo.class, name = "RevisaoJustificativa"),
        @JsonSubTypes.Type(value = RevisaoTextoLivrePojo.class, name = "RevisaoTextoLivre"), @JsonSubTypes.Type(value = RevisaoEmenta.class, name = "RevisaoEmenta"),
        @JsonSubTypes.Type(value = RevisaoRelatorio.class, name = "RevisaoRelatorio"),
        @JsonSubTypes.Type(value = RevisaoAnalise.class, name = "RevisaoAnalise"),
        @JsonSubTypes.Type(value = RevisaoVoto.class, name = "RevisaoVoto"),
        @JsonSubTypes.Type(value = RevisaoTextoItemVoto.class, name = "RevisaoTextoItemVoto")
})
@XmlAccessorType(XmlAccessType.FIELD)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(value = "type")
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
