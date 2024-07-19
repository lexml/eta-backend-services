
package br.gov.lexml.eta.etaservices.printing.json;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.gov.lexml.eta.etaservices.emenda.Anexo;
import br.gov.lexml.eta.etaservices.emenda.Autoria;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmendaTextoLivre;
import br.gov.lexml.eta.etaservices.emenda.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.Epigrafe;
import br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;
import br.gov.lexml.eta.etaservices.emenda.SubstituicaoTermo;

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
    private ComandoEmendaTextoLivrePojo comandoEmendaTextoLivre;
    private SubstituicaoTermoPojo substituicaoTermo;
    private List<? extends AnexoPojo> anexos;
    private String justificativa;
    private String justificativaAntesRevisao;
    private String local;
    private LocalDate data;
    private AutoriaPojo autoria;
    private OpcoesImpressaoPojo opcoesImpressao;
    private List<? extends RevisaoPojo> revisoes;
    private List<? extends NotaRodapePojo> notasRodape;
    private List<String> pendenciasPreenchimento;

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
    
    @Override
    public ComandoEmendaTextoLivre getComandoEmendaTextoLivre() {
        return comandoEmendaTextoLivre;
    }

    @SuppressWarnings("unused")
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

    @Override
    public String getJustificativa() {
        return justificativa;
    }

    @SuppressWarnings("unused")
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    @Override
    public String getJustificativaAntesRevisao() {
		return justificativaAntesRevisao;
	}

	public void setJustificativaAntesRevisao(String justificativaAntesRevisao) {
		this.justificativaAntesRevisao = justificativaAntesRevisao;
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

    @Override
    public List<String> getPendenciasPreenchimento() {
        return pendenciasPreenchimento;
    }

    public void setPendenciasPreenchimento(List<String> pendenciasPreenchimento) {
        this.pendenciasPreenchimento = pendenciasPreenchimento;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anexos == null) ? 0 : anexos.hashCode());
		result = prime * result + ((aplicacao == null) ? 0 : aplicacao.hashCode());
		result = prime * result + ((autoria == null) ? 0 : autoria.hashCode());
		result = prime * result + ((colegiado == null) ? 0 : colegiado.hashCode());
		result = prime * result + ((comandoEmenda == null) ? 0 : comandoEmenda.hashCode());
		result = prime * result + ((comandoEmendaTextoLivre == null) ? 0 : comandoEmendaTextoLivre.hashCode());
		result = prime * result + ((componentes == null) ? 0 : componentes.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((dataUltimaModificacao == null) ? 0 : dataUltimaModificacao.hashCode());
		result = prime * result + ((epigrafe == null) ? 0 : epigrafe.hashCode());
		result = prime * result + ((justificativa == null) ? 0 : justificativa.hashCode());
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		result = prime * result + ((metadados == null) ? 0 : metadados.hashCode());
		result = prime * result + ((modoEdicao == null) ? 0 : modoEdicao.hashCode());
		result = prime * result + ((opcoesImpressao == null) ? 0 : opcoesImpressao.hashCode());
		result = prime * result + ((proposicao == null) ? 0 : proposicao.hashCode());
		result = prime * result + ((revisoes == null) ? 0 : revisoes.hashCode());
		result = prime * result + ((versaoAplicacao == null) ? 0 : versaoAplicacao.hashCode());
		result = prime * result + ((substituicaoTermo == null) ? 0 : substituicaoTermo.hashCode());
		return result;
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
		if (anexos == null) {
			if (other.anexos != null)
				return false;
		} else if (!anexos.equals(other.anexos))
			return false;
		if (aplicacao == null) {
			if (other.aplicacao != null)
				return false;
		} else if (!aplicacao.equals(other.aplicacao))
			return false;
		if (autoria == null) {
			if (other.autoria != null)
				return false;
		} else if (!autoria.equals(other.autoria))
			return false;
		if (colegiado == null) {
			if (other.colegiado != null)
				return false;
		} else if (!colegiado.equals(other.colegiado))
			return false;
		if (comandoEmenda == null) {
			if (other.comandoEmenda != null)
				return false;
		} else if (!comandoEmenda.equals(other.comandoEmenda))
			return false;
		if (comandoEmendaTextoLivre == null) {
			if (other.comandoEmendaTextoLivre != null)
				return false;
		} else if (!comandoEmendaTextoLivre.equals(other.comandoEmendaTextoLivre))
			return false;
		if (substituicaoTermo == null) {
			if (other.substituicaoTermo !=null)
				return false;
		} else if (!substituicaoTermo.equals(other.substituicaoTermo))
			return false;
		if (componentes == null) {
			if (other.componentes != null)
				return false;
		} else if (!componentes.equals(other.componentes))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (dataUltimaModificacao == null) {
			if (other.dataUltimaModificacao != null)
				return false;
		} else if (!dataUltimaModificacao.equals(other.dataUltimaModificacao))
			return false;
		if (epigrafe == null) {
			if (other.epigrafe != null)
				return false;
		} else if (!epigrafe.equals(other.epigrafe))
			return false;
		if (justificativa == null) {
			if (other.justificativa != null)
				return false;
		} else if (!justificativa.equals(other.justificativa))
			return false;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		if (metadados == null) {
			if (other.metadados != null)
				return false;
		} else if (!metadados.equals(other.metadados))
			return false;
		if (modoEdicao != other.modoEdicao)
			return false;
		if (opcoesImpressao == null) {
			if (other.opcoesImpressao != null)
				return false;
		} else if (!opcoesImpressao.equals(other.opcoesImpressao))
			return false;
		if (proposicao == null) {
			if (other.proposicao != null)
				return false;
		} else if (!proposicao.equals(other.proposicao))
			return false;
		if (revisoes == null) {
			if (other.revisoes != null)
				return false;
		} else if (!revisoes.equals(other.revisoes))
			return false;
		if (versaoAplicacao == null) {
			if (other.versaoAplicacao != null)
				return false;
		} else if (!versaoAplicacao.equals(other.versaoAplicacao))
			return false;
        if (pendenciasPreenchimento == null) {
            if (other.pendenciasPreenchimento != null)
                return false;
        } else if (!pendenciasPreenchimento.equals(other.pendenciasPreenchimento))
            return false;
		return true;
	}

	@Override
	public String toString() {
		return "EmendaPojo [dataUltimaModificacao=" + dataUltimaModificacao + ", aplicacao=" + aplicacao
				+ ", versaoAplicacao=" + versaoAplicacao + ", modoEdicao=" + modoEdicao + ", metadados=" + metadados
				+ ", proposicao=" + proposicao + ", colegiado=" + colegiado + ", epigrafe=" + epigrafe
				+ ", componentes=" + componentes + ", comandoEmenda=" + comandoEmenda + ", comandoEmendaTextoLivre=" + comandoEmendaTextoLivre 
                + ", substituicaoTermo" + substituicaoTermo + ", anexos=" + anexos + ", justificativa=" + justificativa 
                + ", local=" + local + ", data=" + data + ", autoria=" + autoria + ", opcoesImpressao=" + opcoesImpressao
				+ ", revisoes=" + revisoes + ", pendenciasPreenchimento=" + pendenciasPreenchimento + "]";
	}
}
