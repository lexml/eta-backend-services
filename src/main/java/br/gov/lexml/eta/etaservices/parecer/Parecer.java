package br.gov.lexml.eta.etaservices.parecer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.emenda.TipoMateria;
import br.gov.lexml.eta.etaservices.printing.json.AnexoPojo;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.OpcoesImpressaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RefProposicaoEmendadaPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoPojo;
import io.vavr.collection.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Parecer {
    private Instant dataUltimaModificacao;
    private String aplicacao;
    private String versaoAplicacao;
    private Map<String, Object> metadados;
    private String epigrafe;
    private List<? extends AnexoPojo> anexos;
    private String local;
    private LocalDate data;
    private AutoriaParecer autoria;
    private OpcoesImpressaoPojo opcoesImpressao;
    private List<? extends RevisaoPojo> revisoes;
    private List<? extends NotaRodapePojo> notasRodape;
    private List<String> pendenciasPreenchimento;
    private Voto voto;
    private String ementa;
    private String relatorio;
    private String analise;
    private Long ano;
    private Destino destino;
    private RefProposicaoEmendadaPojo materia;
    public SiglaCasaLegislativa siglaCasaLegislativa;
    private String tituloSecao2;
    private String tituloSecao3;

    @JsonIgnore()
    public String getDataFormatada() {
        return getData().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)).toLowerCase();
    }

    @JsonIgnore()
    public boolean isPossuiMarcasRevisao() {
        return getRevisoes() != null && !getRevisoes().isEmpty();
    }

    @JsonIgnore()
    public boolean isMateriaCongressoNacional() {
        return Stream.of(TipoMateria.values()).exists(t -> t.name().equals(getMateria().getSigla()));
    }

}
