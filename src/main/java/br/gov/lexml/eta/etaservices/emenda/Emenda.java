package br.gov.lexml.eta.etaservices.emenda;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vavr.collection.Stream;

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
    
    ComandoEmendaTextoLivre getComandoEmendaTextoLivre();

    SubstituicaoTermo getSubstituicaoTermo();
    
    String getJustificativa();
    
    String getJustificativaAntesRevisao();

    String getLocal();

    LocalDate getData();
    
    List<? extends Anexo> getAnexos();

    @JsonIgnore()
    default String getDataFormatada() {
        return getData().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)).toLowerCase();
    }

    Autoria getAutoria();

    OpcoesImpressao getOpcoesImpressao();
    
    List<? extends Revisao> getRevisoes();
    
    List<? extends NotaRodape> getNotasRodape();

    @JsonIgnore()
    default boolean isPossuiMarcasRevisao() {
    	return getRevisoes() != null && !getRevisoes().isEmpty();
    }
    
    @JsonIgnore()
    default boolean isMateriaCongressoNacional() {
    	return Stream.of(TipoMateria.values())
                    .exists(t -> t.name().equals(getProposicao().getSigla()));
    }
}
