package br.gov.lexml.eta.etaservices.printing.json;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.Autoria;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.Epigrafe;
import br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;

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
    private List<? extends RevisaoPojo> revisoes;

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
    public List<? extends RevisaoPojo> getRevisoes() {
    	return revisoes;
    }
    
    public void setRevisoes(List<? extends RevisaoPojo> revisoes) {
		this.revisoes = revisoes;
	}

    @Override
	public int hashCode() {
		return Objects.hash(aplicacao, autoria, colegiado, comandoEmenda, componentes, data, dataUltimaModificacao,
				epigrafe, justificativa, local, metadados, modoEdicao, opcoesImpressao, proposicao, revisoes,
				versaoAplicacao);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmendaPojo other = (EmendaPojo) obj;
		return Objects.equals(aplicacao, other.aplicacao) && Objects.equals(autoria, other.autoria)
				&& Objects.equals(colegiado, other.colegiado) && Objects.equals(comandoEmenda, other.comandoEmenda)
				&& Objects.equals(componentes, other.componentes) && Objects.equals(data, other.data)
				&& Objects.equals(dataUltimaModificacao, other.dataUltimaModificacao)
				&& Objects.equals(epigrafe, other.epigrafe) && Objects.equals(justificativa, other.justificativa)
				&& Objects.equals(local, other.local) && Objects.equals(metadados, other.metadados)
				&& modoEdicao == other.modoEdicao && Objects.equals(opcoesImpressao, other.opcoesImpressao)
				&& Objects.equals(proposicao, other.proposicao) && Objects.equals(revisoes, other.revisoes)
				&& Objects.equals(versaoAplicacao, other.versaoAplicacao);
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
