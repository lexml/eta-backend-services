package br.gov.lexml.eta.etaservices.parecer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import br.gov.lexml.eta.etaservices.emenda.Anexo;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmendaTextoLivre;
import br.gov.lexml.eta.etaservices.emenda.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;
import br.gov.lexml.eta.etaservices.emenda.SubstituicaoTermo;
import br.gov.lexml.eta.etaservices.printing.json.AnexoPojo;
import br.gov.lexml.eta.etaservices.printing.json.ColegiadoApreciadorPojo;
import br.gov.lexml.eta.etaservices.printing.json.ComandoEmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.ComandoEmendaTextoLivrePojo;
import br.gov.lexml.eta.etaservices.printing.json.ComponenteEmendadoPojo;
import br.gov.lexml.eta.etaservices.printing.json.NotaRodapePojo;
import br.gov.lexml.eta.etaservices.printing.json.OpcoesImpressaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RefProposicaoEmendadaPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoPojo;
import br.gov.lexml.eta.etaservices.printing.json.SubstituicaoTermoPojo;

public class Parecer {

    private Instant dataUltimaModificacao;
    private String aplicacao;
    private String versaoAplicacao;
    private Map<String, Object> metadados;
    private RefProposicaoEmendadaPojo proposicao;
    private ColegiadoApreciadorPojo colegiado;
    private String epigrafe;
    private List<? extends ComponenteEmendadoPojo> componentes;
    private ComandoEmendaPojo comandoEmenda;
    private ComandoEmendaTextoLivrePojo comandoEmendaTextoLivre;
    private SubstituicaoTermoPojo substituicaoTermo;
    private List<? extends AnexoPojo> anexos;
    private String justificativa;
    private String justificativaAntesRevisao;
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

    public String getDataFormatada() {
        return getData().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)).toLowerCase();
    }

    public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    public void setDataUltimaModificacao(Instant dataUltimaModificacao) {
        this.dataUltimaModificacao = dataUltimaModificacao;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    public void setVersaoAplicacao(String versaoAplicacao) {
        this.versaoAplicacao = versaoAplicacao;
    }


    public Map<String, Object> getMetadados() {
        return metadados;
    }

    public void setMetadados(Map<String, Object> metadados) {
        this.metadados = metadados;
    }

    public RefProposicaoEmendada getProposicao() {
        return proposicao;
    }

    public void setProposicao(RefProposicaoEmendadaPojo proposicao) {
        this.proposicao = proposicao;
    }

    public ColegiadoApreciador getColegiadoApreciador() {
        return colegiado;
    }

    public void setColegiadoApreciador(ColegiadoApreciadorPojo colegiado) {
        this.colegiado = colegiado;
    }

    public String getEpigrafe() {
        return epigrafe;
    }

    public void setEpigrafe(String epigrafe) {
        this.epigrafe = epigrafe;
    }

    public List<? extends ComponenteEmendado> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<? extends ComponenteEmendadoPojo> componentes) {
        this.componentes = componentes;
    }

    public ComandoEmenda getComandoEmenda() {
        return comandoEmenda;
    }

    public ComandoEmendaTextoLivre getComandoEmendaTextoLivre() {
        return comandoEmendaTextoLivre;
    }

    public void setComandoEmenda(ComandoEmendaPojo comandoEmenda) {
        this.comandoEmenda = comandoEmenda;
    }

    public void setComandoEmendaTextoLivre(ComandoEmendaTextoLivrePojo comandoEmendaTextoLivre) {
        this.comandoEmendaTextoLivre = comandoEmendaTextoLivre;
    }

    public SubstituicaoTermo getSubstituicaoTermo() {
        return substituicaoTermo;
    }

    public void setSubstituicaoTermo(SubstituicaoTermoPojo substituicaoTermo) {
        this.substituicaoTermo = substituicaoTermo;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getJustificativaAntesRevisao() {
        return justificativaAntesRevisao;
    }

    public void setJustificativaAntesRevisao(String justificativaAntesRevisao) {
        this.justificativaAntesRevisao = justificativaAntesRevisao;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public AutoriaParecer getAutoria() {
        return autoria;
    }

    public void setAutoria(AutoriaParecer autoria) {
        this.autoria = autoria;
    }

    public OpcoesImpressao getOpcoesImpressao() {
        return opcoesImpressao;
    }

    public void setOpcoesImpressao(OpcoesImpressaoPojo opcoesImpressao) {
        this.opcoesImpressao = opcoesImpressao;
    }

    public List<? extends Anexo> getAnexos() {
        return this.anexos;
    }

    public List<? extends RevisaoPojo> getRevisoes() {
        return revisoes;
    }

    public void setRevisoes(List<? extends RevisaoPojo> revisoes) {
        this.revisoes = revisoes;
    }

    public List<? extends NotaRodapePojo> getNotasRodape() {
        return notasRodape;
    }

    public void setNotasRodape(List<? extends NotaRodapePojo> notasRodape) {
        this.notasRodape = notasRodape;
    }

    public List<String> getPendenciasPreenchimento() {
        return pendenciasPreenchimento;
    }

    public void setPendenciasPreenchimento(List<String> pendenciasPreenchimento) {
        this.pendenciasPreenchimento = pendenciasPreenchimento;
    }

    public Voto getVoto() {
        return voto;
    }

    public void setVoto(Voto voto) {
        this.voto = voto;
    }

    public String getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(String relatorio) {
        this.relatorio = relatorio;
    }

    public String getAnalise() {
        return analise;
    }

    public void setAnalise(String analise) {
        this.analise = analise;
    }

    public String getEmenta() {
        return ementa;
    }

    public void setEmenta(String ementa) {
        this.ementa = ementa;
    }

}
