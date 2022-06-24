package br.gov.lexml.eta.etaservices.printing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface Emenda {
    Instant getDataUltimaModificacao();

    String getAplicacao();

    String getVersaoAplicacao();

    ModoEdicaoEmenda getModoEdicao();

    Map<String, Object> getMetadados();

    RefProposicaoEmendada getProposicao();

    ColegiadoApreciador getColegiado();

    Epigrafe getEpigrafe();

    List<? extends ComponenteEmendado> getComponentes();

    ComandoEmenda getComandoEmenda();

    String getJustificativa();

    String getLocal();

    LocalDate getData();

    Autoria getAutoria();

    OpcoesImpressao getOpcoesImpressao();
}
