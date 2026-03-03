package br.gov.lexml.eta.etaservices.parecer;

import br.gov.lexml.eta.etaservices.printing.json.ParlamentarPojo;

public class AutoriaParecer {

    private ParlamentarPojo relator;
    private ParlamentarPojo presidente;

    public ParlamentarPojo getRelator() {
        return relator;
    }

    public void setRelator(ParlamentarPojo relator) {
        this.relator = relator;
    }

    public ParlamentarPojo getPresidente() {
        return presidente;
    }

    public void setPresidente(ParlamentarPojo presidente) {
        this.presidente = presidente;
    }
}
