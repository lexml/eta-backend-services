package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public record ComandoEmenda(String cabecalhoComum, List<ItemComandoEmenda> comandos) {

}
