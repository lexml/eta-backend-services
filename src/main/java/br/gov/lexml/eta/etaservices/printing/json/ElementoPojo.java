package br.gov.lexml.eta.etaservices.printing.json;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import br.gov.lexml.eta.etaservices.emenda.Elemento;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class ElementoPojo extends ReferenciaPojo implements Elemento {
	@XmlAttribute
    private int nivel;
	@XmlAttribute
    private String numero;
	@XmlAttribute
    private String rotulo;
	@XmlAttribute
    private boolean agrupador;
	@XmlAttribute
    private boolean editavel;
	@XmlElement(name = "Mensagens")
    private List<MensagemPojo> mensagens;
	@XmlElement(name = "Hierarquia")
    private HierarquiaPojo hierarquia;
	@XmlAttribute
    private boolean sendoEditado;
	@XmlAttribute
    private int index;
	@XmlAttribute
    private String norma;
	@XmlAttribute
    private boolean abreAspas;
	@XmlAttribute
    private boolean fechaAspas;
	@XmlAttribute
    private String notaAlteracao;
	@XmlAttribute
    private boolean dispositivoAlteracao;
	@XmlAttribute
    private String tipoOmissis;
	@XmlAttribute
    private boolean podeEditarNotaAlteracao;
	@XmlAttribute
    private boolean manterNoMesmoGrupoDeAspas;
	@XmlAttribute
    private String artigoDefinido;
	@XmlElement(name = "ElementoAnteriorNaSequenciaDeLeitura")
    private ReferenciaPojo elementoAnteriorNaSequenciaDeLeitura;
	@XmlElement(name = "Revisao")
    private RevisaoPojo revisao;
}
