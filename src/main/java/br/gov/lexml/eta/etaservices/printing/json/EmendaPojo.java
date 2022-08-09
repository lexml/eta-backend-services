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
    private RefProposicaoEmendadaPojo proposicao;
    private ColegiadoApreciadorPojo colegiado;
    private EpigrafePojo epigrafe;
    private List<? extends ComponenteEmendadoPojo> componentes;
    private ComandoEmendaPojo comandoEmenda;
    private String justificativa;
    private String local;
    private LocalDate data;
    private AutoriaPojo autoria;
    private OpcoesImpressaoPojo opcoesImpressao;

    @Override
    public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    @SuppressWarnings("unused")
    public void setDataUltimaModificacao(Instant dataUltimaModificacao) {
        this.dataUltimaModificacao = dataUltimaModificacao;
    }

    @Override
    public String getAplicacao() {
        return aplicacao;
    }

    @SuppressWarnings("unused")
    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    @Override
    public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    @SuppressWarnings("unused")
    public void setVersaoAplicacao(String versaoAplicacao) {
        this.versaoAplicacao = versaoAplicacao;
    }

    @Override
    public ModoEdicaoEmenda getModoEdicao() {
        return modoEdicao;
    }

    @SuppressWarnings("unused")
    public void setModoEdicao(ModoEdicaoEmenda modoEdicao) {
        this.modoEdicao = modoEdicao;
    }

    @Override
    public Map<String, Object> getMetadados() {
        return metadados;
    }

    @SuppressWarnings("unused")
    public void setMetadados(Map<String, Object> metadados) {
        this.metadados = metadados;
    }

    @Override
    public RefProposicaoEmendada getProposicao() {
        return proposicao;
    }

    @SuppressWarnings("unused")
    public void setProposicao(RefProposicaoEmendadaPojo proposicao) {
        this.proposicao = proposicao;
    }

    @Override
    public ColegiadoApreciador getColegiadoApreciador() {
        return colegiado;
    }

    @SuppressWarnings("unused")
    public void setColegiadoApreciador(ColegiadoApreciadorPojo colegiado) {
        this.colegiado = colegiado;
    }

    @Override
    public Epigrafe getEpigrafe() {
        return epigrafe;
    }

    @SuppressWarnings("unused")
    public void setEpigrafe(EpigrafePojo epigrafe) {
        this.epigrafe = epigrafe;
    }

    @Override
    public List<? extends ComponenteEmendado> getComponentes() {
        return componentes;
    }

    @SuppressWarnings("unused")
    public void setComponentes(List<? extends ComponenteEmendadoPojo> componentes) {
        this.componentes = componentes;
    }

    @Override
    public ComandoEmenda getComandoEmenda() {
        return comandoEmenda;
    }

    @SuppressWarnings("unused")
    public void setComandoEmenda(ComandoEmendaPojo comandoEmenda) {
        this.comandoEmenda = comandoEmenda;
    }

    @Override
    public String getJustificativa() {
        return justificativa;
    }

    @SuppressWarnings("unused")
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    @Override
    public String getLocal() {
        return local;
    }

    @SuppressWarnings("unused")
    public void setLocal(String local) {
        this.local = local;
    }

    @Override
    public LocalDate getData() {
        return data;
    }

    @SuppressWarnings("unused")
    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public Autoria getAutoria() {
        return autoria;
    }

    @SuppressWarnings("unused")
    public void setAutoria(AutoriaPojo autoria) {
        this.autoria = autoria;
    }

    @Override
    public OpcoesImpressao getOpcoesImpressao() {
        return opcoesImpressao;
    }

    @SuppressWarnings("unused")
    public void setOpcoesImpressao(OpcoesImpressaoPojo opcoesImpressao) {
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
