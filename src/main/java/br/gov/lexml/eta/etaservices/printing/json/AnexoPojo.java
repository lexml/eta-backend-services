package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.Anexo;

public class AnexoPojo implements Anexo {

	private String nomeArquivo;
	private String base64;
	
	@Override
	public String getNomeArquivo() {	
		return this.nomeArquivo;
	}
	@Override
	public String getBase64() {
		return this.base64;
	}
	@Override
	public String toString() {
		return "AnexoPojo [nomeArquivo=" + nomeArquivo + ", base64=" + base64 + "]";
	}
	
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
		

}
