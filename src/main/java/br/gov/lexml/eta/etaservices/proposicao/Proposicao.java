package br.gov.lexml.eta.etaservices.proposicao;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.gov.lexml.eta.etaservices.printing.json.AnexoPojo;
import br.gov.lexml.eta.etaservices.printing.json.AutoriaPojo;
import br.gov.lexml.eta.etaservices.printing.json.ColegiadoApreciadorPojo;
import br.gov.lexml.eta.etaservices.printing.json.EpigrafePojo;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.OpcoesImpressaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoPojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Proposicao {
    private String dataUltimaModificacao;
    private String aplicacao;
    private String versaoAplicacao;
    private Map<String, Object> metadados;
    private EpigrafePojo epigrafe;
    private List<String> pendenciasPreenchimento;
    private List<? extends AnexoPojo> anexos;
    private String justificativa;
    private String justificativaAntesRevisao;
    private String local;
    private AutoriaPojo autoria;
    private OpcoesImpressaoPojo opcoesImpressao;
    private List<? extends RevisaoPojo> revisoes;
    private ColegiadoApreciadorPojo colegiadoApreciador;
    private List<? extends NotaRodapePojo> notasRodape;
    private Object projetoNorma;
    private String urn;
    private String sigla;
    private String numero;
    private String ano;
    private String ementa;
    private boolean substitutivo;

    // Campo opcional de compatibilidade para payloads que enviam "texto" no raiz.
    private String texto;
}
