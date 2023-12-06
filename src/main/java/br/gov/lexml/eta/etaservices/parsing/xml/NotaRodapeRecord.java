package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.NotaRodape;

public final class NotaRodapeRecord implements NotaRodape {

	private final String id;
	private final Integer numero;
	private final String texto;
	
	public NotaRodapeRecord(String id, Integer numero, String texto) {
		this.id = id;
		this.numero = numero;
		this.texto = texto;
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(id, numero, texto);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotaRodapeRecord other = (NotaRodapeRecord) obj;
		return Objects.equals(id, other.id) && Objects.equals(numero, other.numero)
				&& Objects.equals(texto, other.texto);
	}

	@Override
	public String toString() {
		return "NotaRodapeRecord [id=" + id + ", numero=" + numero + ", texto=" + texto + "]";
	}
	
}
