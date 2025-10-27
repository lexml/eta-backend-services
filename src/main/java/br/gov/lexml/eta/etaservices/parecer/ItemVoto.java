package br.gov.lexml.eta.etaservices.parecer;

public class ItemVoto {
    private String texto;
    private DocumentoVoto documentoVoto;
    private Integer posicao;

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public DocumentoVoto getDocumentoVoto() {
        return documentoVoto;
    }

    public void setDocumentoVoto(DocumentoVoto documentoVoto) {
        this.documentoVoto = documentoVoto;
    }

    public Integer getPosicao() {
        return posicao;
    }

    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }
}
