package br.gov.lexml.eta.etaservices.emenda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModoEdicaoEmenda {
    EMENDA("emenda"),
    EMENDA_TEXTO_LIVRE("emendaTextoLivre"),
    EMENDA_ARTIGO_ONDE_COUBER("emendaArtigoOndeCouber"),
    EMENDA_SUBSTITUICAO_TERMO("emendaSubstituicaoTermo");

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
        } else if (EMENDA_TEXTO_LIVRE.nome.equals(me)) {
        	return EMENDA_TEXTO_LIVRE;
        } else if (EMENDA_SUBSTITUICAO_TERMO.nome.equals(me)) {
        	return EMENDA_SUBSTITUICAO_TERMO;
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
