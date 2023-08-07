package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.Anexo;

public final class AnexoRecord implements Anexo {
    private final String nomeArquivo;
    private final String base64;
	
    
    public AnexoRecord(String nomeArquivo, String base64) {
		super();
		this.nomeArquivo = nomeArquivo;
		this.base64 = base64;
	}
    
	@Override
	public String getNomeArquivo() {
		return this.nomeArquivo;
	}
	@Override
	public String getBase64() {
		return this.base64;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base64 == null) ? 0 : base64.hashCode());
		result = prime * result + ((nomeArquivo == null) ? 0 : nomeArquivo.hashCode());
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
		AnexoRecord other = (AnexoRecord) obj;
		if (base64 == null) {
			if (other.base64 != null)
				return false;
		} else if (!base64.equals(other.base64))
			return false;
		if (nomeArquivo == null) {
			if (other.nomeArquivo != null)
				return false;
		} else if (!nomeArquivo.equals(other.nomeArquivo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnexoRecord [nomeArquivo=" + nomeArquivo + ", base64=" + base64 + "]";
	}
	
}
