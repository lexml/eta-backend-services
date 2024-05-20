package br.gov.lexml.eta.etaservices.emenda;

public enum SiglaCasaLegislativa {
    CN,
    CD,
    SF;

    public static SiglaCasaLegislativa parse(String valueOf) {
        if (valueOf == null) {return null;}
        switch (valueOf) {
            case "CN":
                return CN;
            case "CD":
                return CD;
            case "SF":
                return SF;
            default:
                return null;
        }
    }
}
