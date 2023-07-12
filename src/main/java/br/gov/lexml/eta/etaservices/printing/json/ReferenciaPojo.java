package br.gov.lexml.eta.etaservices.printing.json;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import br.gov.lexml.eta.etaservices.emenda.Referencia;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenciaPojo implements Referencia {
	@XmlAttribute
    private String tipo;
	@XmlAttribute
    private Integer uuid;
	@XmlAttribute
    private String uuid2;
	@XmlAttribute
    private String lexmlId;
	@XmlElement(name = "Conteudo")
    private ConteudoPojo conteudo;
	@XmlAttribute
    private String descricaoSituacao;
	@XmlAttribute
    private Integer uuidAlteracao;
	@XmlAttribute
    private String uuid2Alteracao;
	@XmlAttribute
    private Boolean existeNaNormaAlterada;
    
    @Override
    public Boolean isExisteNaNormaAlterada() {
    	return existeNaNormaAlterada;
    }
}