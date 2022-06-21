package br.gov.lexml.eta.etaservices.printing;

public enum ModoEdicaoEmenda {
    EMENDA("emenda"),
    EMENDA_ARTIGO_ONDE_COUBER("emendaArtigoOndeCouber");

    private final String nome;

    private ModoEdicaoEmenda(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
