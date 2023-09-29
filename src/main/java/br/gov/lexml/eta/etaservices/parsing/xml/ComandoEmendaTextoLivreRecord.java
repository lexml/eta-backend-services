package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.ComandoEmendaTextoLivre;

public final class ComandoEmendaTextoLivreRecord implements ComandoEmendaTextoLivre {
    private final String motivo;
    private final String texto;
    private final String textoAntesRevisao;

    public ComandoEmendaTextoLivreRecord(String motivo, String texto, String textoAntesRevisao) {
        this.motivo = motivo;
        this.texto = texto;
        this.textoAntesRevisao = textoAntesRevisao;
    }

	@Override
	public String getMotivo() {
		return motivo;
	}

	@Override
	public String getTexto() {
		return texto;
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
		ComandoEmendaTextoLivreRecord other = (ComandoEmendaTextoLivreRecord) obj;
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
		return "ComandoEmendaTextoLivreRecord [motivo=" + motivo + ", texto=" + texto + ", textoAntesRevisao="
				+ textoAntesRevisao + "]";
	}

	@Override
	public String getTextoAntesRevisao() {
		return this.textoAntesRevisao;
	}

    

}
