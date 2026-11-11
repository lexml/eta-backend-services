package br.gov.lexml.eta.etaservices.printing.pdf;

import java.util.Locale;

public enum TipoDocumento {
    EMENDA, PARECER, SUBSTITUTIVO, OUTRO;

    /**
     * Faz o parse de uma string para TipoDocumento. - Ignora maiúsculas/minúsculas
     * - Desconsidera espaços nas extremidades - Retorna OUTRO quando não reconhecer
     */
    public static TipoDocumento parse(String s) {
        if (s == null)
            return OUTRO;
        String n = s.trim();
        if (n.isEmpty())
            return OUTRO;

        for (TipoDocumento td : values()) {
            if (td.name().equalsIgnoreCase(n)) {
                return td;
            }
        }

        switch (n.toLowerCase(Locale.ROOT)) {
        case "emenda":
            return EMENDA;
        case "parecer":
            return PARECER;
        case "substitutivo":
            return SUBSTITUTIVO;
        case "outro":
            return OUTRO;
        default:
            return OUTRO;
        }
    }
}
