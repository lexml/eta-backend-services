package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModoEdicaoEmenda {
    EMENDA("emenda"),
    EMENDA_ARTIGO_ONDE_COUBER("emendaArtigoOndeCouber");

    @JsonValue
    private final String nome;

    ModoEdicaoEmenda(String nome) {
        this.nome = nome;
    }

    public static ModoEdicaoEmenda parse(String me) {
        if (EMENDA.nome.equals(me)) {
            return EMENDA;
        } else if (EMENDA_ARTIGO_ONDE_COUBER.nome.equals(me)) {
            return EMENDA_ARTIGO_ONDE_COUBER;
        }

        return null;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
