package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.ComandoEmendaTextoLivre;

public class ComandoEmendaTextoLivrePojo implements ComandoEmendaTextoLivre {

	private String motivo;
	private String texto;
	private String textoAntesRevisao;
	
	@Override
	public String getMotivo() {
		return this.motivo;
	}

	@Override
	public String getTexto() {
		return this.texto;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	@Override
	public String getTextoAntesRevisao() {
		return this.textoAntesRevisao;
	}
	
	public void setTextoAntesRevisao(String textoAntesRevisao) {
		this.textoAntesRevisao = textoAntesRevisao;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((motivo == null) ? 0 : motivo.hashCode());
		result = prime * result + ((texto == null) ? 0 : texto.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComandoEmendaTextoLivrePojo other = (ComandoEmendaTextoLivrePojo) obj;
		if (motivo == null) {
			if (other.motivo != null)
				return false;
		} else if (!motivo.equals(other.motivo))
			return false;
		if (texto == null) {
			if (other.texto != null)
				return false;
		} else if (!texto.equals(other.texto))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ComandoEmendaTextoLivrePojo [motivo=" + motivo + ", texto=" + texto + "]";
	}

}
