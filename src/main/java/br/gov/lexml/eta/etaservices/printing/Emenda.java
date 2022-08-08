package br.gov.lexml.eta.etaservices.printing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

public interface Emenda {
    Instant getDataUltimaModificacao();

    String getAplicacao();

    String getVersaoAplicacao();

    ModoEdicaoEmenda getModoEdicao();

    Map<String, Object> getMetadados();

    RefProposicaoEmendada getProposicao();

    ColegiadoApreciador getColegiadoApreciador();

    Epigrafe getEpigrafe();

    List<? extends ComponenteEmendado> getComponentes();

    ComandoEmenda getComandoEmenda();

    String getJustificativa();

    String getLocal();

    LocalDate getData();

    @SuppressWarnings("unused")
    default String getDataFormatada() {
        return getData().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
    }

    Autoria getAutoria();

    OpcoesImpressao getOpcoesImpressao();
}
