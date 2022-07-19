package br.gov.lexml.eta.etaservices.printing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EmendaRecord implements Emenda {
    private final Instant dataUltimaModificacao;
    private final String aplicacao;
    private final String versaoAplicacao;
    private final ModoEdicaoEmenda modoEdicao;
    private final Map<String, Object> metadados;
    private final RefProposicaoEmendada proposicao;
    private final ColegiadoApreciador colegiado;
    private final Epigrafe epigrafe;
    private final List<? extends ComponenteEmendado> componentes;
    private final ComandoEmenda comandoEmenda;
    private final String justificativa;
    private final String local;
    private final LocalDate data;
    private final Autoria autoria;
    private final OpcoesImpressao opcoesImpressao;

    public EmendaRecord(
            Instant dataUltimaModificacao,
            String aplicacao,
            String versaoAplicacao,
            ModoEdicaoEmenda modoEdicao,
            Map<String, Object> metadados,
            RefProposicaoEmendada proposicao,
            ColegiadoApreciador colegiado,
            Epigrafe epigrafe,
            List<? extends ComponenteEmendado> componentes,
            ComandoEmenda comandoEmenda,
            String justificativa,
            String local,
            LocalDate data,
            Autoria autoria,
            OpcoesImpressao opcoesImpressao) {
        this.dataUltimaModificacao = dataUltimaModificacao;
        this.aplicacao = aplicacao;
        this.versaoAplicacao = versaoAplicacao;
        this.modoEdicao = modoEdicao;
        this.metadados = metadados;
        this.proposicao = proposicao;
        this.colegiado = colegiado;
        this.epigrafe = epigrafe;
        this.componentes = componentes;
        this.comandoEmenda = comandoEmenda;
        this.justificativa = justificativa;
        this.local = local;
        this.data = data;
        this.autoria = autoria;
        this.opcoesImpressao = opcoesImpressao;
    }

    public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    public ModoEdicaoEmenda getModoEdicao() {
        return modoEdicao;
    }

    public Map<String, Object> getMetadados() {
        return metadados;
    }

    public RefProposicaoEmendada getProposicao() {
        return proposicao;
    }

    public ColegiadoApreciador getColegiado() {
        return colegiado;
    }

    public Epigrafe getEpigrafe() {
        return epigrafe;
    }

    public List<? extends ComponenteEmendado> getComponentes() {
        return componentes;
    }

    public ComandoEmenda getComandoEmenda() {
        return comandoEmenda;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getLocal() {
        return local;
    }

    public LocalDate getData() {
        return data;
    }

    public Autoria getAutoria() {
        return autoria;
    }

    public OpcoesImpressao getOpcoesImpressao() {
        return opcoesImpressao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        EmendaRecord that = (EmendaRecord) obj;
        return Objects.equals(this.dataUltimaModificacao, that.dataUltimaModificacao) &&
                Objects.equals(this.aplicacao, that.aplicacao) &&
                Objects.equals(this.versaoAplicacao, that.versaoAplicacao) &&
                Objects.equals(this.modoEdicao, that.modoEdicao) &&
                Objects.equals(this.metadados, that.metadados) &&
                Objects.equals(this.proposicao, that.proposicao) &&
                Objects.equals(this.colegiado, that.colegiado) &&
                Objects.equals(this.epigrafe, that.epigrafe) &&
                Objects.equals(this.componentes, that.componentes) &&
                Objects.equals(this.comandoEmenda, that.comandoEmenda) &&
                Objects.equals(this.justificativa, that.justificativa) &&
                Objects.equals(this.local, that.local) &&
                Objects.equals(this.data, that.data) &&
                Objects.equals(this.autoria, that.autoria) &&
                Objects.equals(this.opcoesImpressao, that.opcoesImpressao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUltimaModificacao, aplicacao, versaoAplicacao, modoEdicao, metadados, proposicao, colegiado, epigrafe, componentes, comandoEmenda, justificativa, local, data, autoria, opcoesImpressao);
    }

    @Override
    public String toString() {
        return "Emenda[" +
                "dataUltimaModificacao=" + dataUltimaModificacao + ", " +
                "aplicacao=" + aplicacao + ", " +
                "versaoAplicacao=" + versaoAplicacao + ", " +
                "modoEdicao=" + modoEdicao + ", " +
                "metadados=" + metadados + ", " +
                "proposicao=" + proposicao + ", " +
                "colegiado=" + colegiado + ", " +
                "epigrafe=" + epigrafe + ", " +
                "componentes=" + componentes + ", " +
                "comandoEmenda=" + comandoEmenda + ", " +
                "justificativa=" + justificativa + ", " +
                "local=" + local + ", " +
                "data=" + data + ", " +
                "autoria=" + autoria + ", " +
                "opcoesImpressao=" + opcoesImpressao + ']';
    }

}
