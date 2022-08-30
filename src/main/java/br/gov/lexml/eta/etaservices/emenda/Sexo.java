package br.gov.lexml.eta.etaservices.emenda;

public enum Sexo {
    F,
    M;

    public static Sexo parse(String valueOf) {
    	if (valueOf == null) {
    		return null;
    	}
        switch (valueOf) {
            case "F":
                return F;
            case "M":
                return M;
            default:
                return null;
        }
    }
}
