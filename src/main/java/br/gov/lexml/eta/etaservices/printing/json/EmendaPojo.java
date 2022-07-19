package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.Autoria;
import br.gov.lexml.eta.etaservices.printing.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.printing.ComandoEmenda;
import br.gov.lexml.eta.etaservices.printing.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.Epigrafe;
import br.gov.lexml.eta.etaservices.printing.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.printing.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.printing.RefProposicaoEmendada;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmendaPojo implements Emenda {
    private Instant dataUltimaModificacao;
    private String aplicacao;
    private String versaoAplicacao;
    private ModoEdicaoEmenda modoEdicao;
    private Map<String, Object> metadados;
    private RefProposicaoEmendada proposicao;
    private ColegiadoApreciador colegiado;
    private Epigrafe epigrafe;
    private List<? extends ComponenteEmendado> componentes;
    private ComandoEmenda comandoEmenda;
    private String justificativa;
    private String local;
    private LocalDate data;
    private Autoria autoria;
    private OpcoesImpressao opcoesImpressao;

    @Override
    public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    public void setDataUltimaModificacao(Instant dataUltimaModificacao) {
        this.dataUltimaModificacao = dataUltimaModificacao;
    }

    @Override
    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    @Override
    public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    public void setVersaoAplicacao(String versaoAplicacao) {
        this.versaoAplicacao = versaoAplicacao;
    }

    @Override
    public ModoEdicaoEmenda getModoEdicao() {
        return modoEdicao;
    }

    public void setModoEdicao(ModoEdicaoEmenda modoEdicao) {
        this.modoEdicao = modoEdicao;
    }

    @Override
    public Map<String, Object> getMetadados() {
        return metadados;
    }

    public void setMetadados(Map<String, Object> metadados) {
        this.metadados = metadados;
    }

    @Override
    public RefProposicaoEmendada getProposicao() {
        return proposicao;
    }

    public void setProposicao(RefProposicaoEmendada proposicao) {
        this.proposicao = proposicao;
    }

    @Override
    public ColegiadoApreciador getColegiado() {
        return colegiado;
    }

    public void setColegiado(ColegiadoApreciador colegiado) {
        this.colegiado = colegiado;
    }

    @Override
    public Epigrafe getEpigrafe() {
        return epigrafe;
    }

    public void setEpigrafe(Epigrafe epigrafe) {
        this.epigrafe = epigrafe;
    }

    @Override
    public List<? extends ComponenteEmendado> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<? extends ComponenteEmendado> componentes) {
        this.componentes = componentes;
    }

    @Override
    public ComandoEmenda getComandoEmenda() {
        return comandoEmenda;
    }

    public void setComandoEmenda(ComandoEmenda comandoEmenda) {
        this.comandoEmenda = comandoEmenda;
    }

    @Override
    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    @Override
    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    @Override
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public Autoria getAutoria() {
        return autoria;
    }

    public void setAutoria(Autoria autoria) {
        this.autoria = autoria;
    }

    @Override
    public OpcoesImpressao getOpcoesImpressao() {
        return opcoesImpressao;
    }

    public void setOpcoesImpressao(OpcoesImpressao opcoesImpressao) {
        this.opcoesImpressao = opcoesImpressao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmendaPojo that = (EmendaPojo) o;
        return dataUltimaModificacao.equals(that.dataUltimaModificacao) && aplicacao.equals(that.aplicacao) && versaoAplicacao.equals(that.versaoAplicacao) && modoEdicao == that.modoEdicao && metadados.equals(that.metadados) && proposicao.equals(that.proposicao) && colegiado.equals(that.colegiado) && epigrafe.equals(that.epigrafe) && componentes.equals(that.componentes) && comandoEmenda.equals(that.comandoEmenda) && justificativa.equals(that.justificativa) && local.equals(that.local) && data.equals(that.data) && autoria.equals(that.autoria) && opcoesImpressao.equals(that.opcoesImpressao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUltimaModificacao, aplicacao, versaoAplicacao, modoEdicao, metadados, proposicao, colegiado, epigrafe, componentes, comandoEmenda, justificativa, local, data, autoria, opcoesImpressao);
    }

    @Override
    public String toString() {
        return "EmendaPojo{" +
                "dataUltimaModificacao=" + dataUltimaModificacao +
                ", aplicacao='" + aplicacao + '\'' +
                ", versaoAplicacao='" + versaoAplicacao + '\'' +
                ", modoEdicao=" + modoEdicao +
                ", metadados=" + metadados +
                ", proposicao=" + proposicao +
                ", colegiado=" + colegiado +
                ", epigrafe=" + epigrafe +
                ", componentes=" + componentes +
                ", comandoEmenda=" + comandoEmenda +
                ", justificativa='" + justificativa + '\'' +
                ", local='" + local + '\'' +
                ", data=" + data +
                ", autoria=" + autoria +
                ", opcoesImpressao=" + opcoesImpressao +
                '}';
    }
}
