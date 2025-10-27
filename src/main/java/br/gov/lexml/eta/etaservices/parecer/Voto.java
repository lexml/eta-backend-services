package br.gov.lexml.eta.etaservices.parecer;

import java.util.List;

public class Voto {
    private List<ItemVoto> itensVoto;

    public List<ItemVoto> getItensVoto() {
        return itensVoto;
    }

    public void setItensVoto(List<ItemVoto> itensVoto) {
        this.itensVoto = itensVoto;
    }
}
