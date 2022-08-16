package br.gov.lexml.eta.etaservices.emenda;

public enum NotaAlteracao {
    AC,
    NR;

    public static NotaAlteracao parse(String valueOf) {
        switch (valueOf) {
            case "AC":
                return AC;
            case "NR":
                return NR;
            default:
                return null;
        }
    }
}
