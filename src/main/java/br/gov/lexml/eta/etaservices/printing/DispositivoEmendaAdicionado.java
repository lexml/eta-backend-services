package br.gov.lexml.eta.etaservices.printing;

import java.util.List;

public record DispositivoEmendaAdicionado(
    String tipo,
    String id,
    String rotulo,
    String texto,
    Boolean textoOmitido,
    Boolean abreAspas,
    Boolean fechaAspas,
    NotaAlteracao notaAlteracao,
    Boolean ondeCouber,
    String idPai,
    String idIrmaoAnterior,
    String urnNormaAlterada,
    Boolean existeNaNormaAlterada,
    List<DispositivoEmendaAdicionado> filhos) implements DispositivoEmenda {

}
