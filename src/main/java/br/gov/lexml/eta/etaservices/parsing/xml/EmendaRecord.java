package br.gov.lexml.eta.etaservices.parsing.xml;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

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
import br.gov.lexml.eta.etaservices.emenda.Revisao;

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
    private final ComandoEmendaTextoLivre comandoEmendaTextoLivre;
    private final ComandoEmenda comandoEmenda;
    private final List<? extends Anexo> anexos;
    private final String justificativa;
    private final String local;
    private final LocalDate data;
    private final Autoria autoria;
    private final OpcoesImpressao opcoesImpressao;
    // Utilizando RevisaoPojo para permitir marshaling jaxb entre jsone xml e vice versa.
    private final List<? extends Revisao> revisoes;

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
            ComandoEmendaTextoLivre comandoEmendaTextoLivre,
            List<? extends Anexo> anexos,
            String justificativa,
            String local,
            LocalDate data,
            Autoria autoria,
            OpcoesImpressao opcoesImpressao,
            List<? extends Revisao> revisoes) {
        this.dataUltimaModificacao = dataUltimaModificacao;
        this.aplicacao = aplicacao;
        this.versaoAplicacao = versaoAplicacao;
        this.modoEdicao = modoEdicao;
        this.metadados = metadados;
        this.proposicao = proposicao;
        this.colegiado = colegiado;
        this.epigrafe = epigrafe;
        this.componentes = componentes;
        this.comandoEmendaTextoLivre = comandoEmendaTextoLivre;
        this.anexos = anexos;
        this.comandoEmenda = comandoEmenda;
        this.justificativa = justificativa;
        this.local = local;
        this.data = data;
        this.autoria = autoria;
        this.opcoesImpressao = opcoesImpressao;
        this.revisoes = revisoes;
    }

    @Override
	public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    @Override
	public String getAplicacao() {
        return aplicacao;
    }

    @Override
	public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    @Override
	public ModoEdicaoEmenda getModoEdicao() {
        return modoEdicao;
    }

    @Override
	public Map<String, Object> getMetadados() {
        return metadados;
    }

    @Override
	public RefProposicaoEmendada getProposicao() {
        return proposicao;
    }

    @Override
	public ColegiadoApreciador getColegiadoApreciador() {
        return colegiado;
    }

    @Override
	public Epigrafe getEpigrafe() {
        return epigrafe;
    }

    @Override
	public List<? extends ComponenteEmendado> getComponentes() {
        return componentes;
    }

    @Override
    public ComandoEmendaTextoLivre getComandoEmendaTextoLivre() {
    	return comandoEmendaTextoLivre;
    }
    
	public ComandoEmenda getComandoEmenda() {
        return comandoEmenda;
    }

    @Override
	public String getJustificativa() {
        return justificativa;
    }

    @Override
	public String getLocal() {
        return local;
    }

    @Override
	@JsonSerialize(using = LocalDateSerializer.class) 
    public LocalDate getData() {
        return data;
    }

    @Override
	public Autoria getAutoria() {
        return autoria;
    }

    @Override
	public OpcoesImpressao getOpcoesImpressao() {
        return opcoesImpressao;
    }
    
    @Override
	public List<? extends Anexo> getAnexos() {
		return this.anexos;
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
                Objects.equals(this.comandoEmendaTextoLivre, that.comandoEmendaTextoLivre) &&
                Objects.equals(this.comandoEmenda, that.comandoEmenda) &&
                Objects.equals(this.justificativa, that.justificativa) &&
                Objects.equals(this.local, that.local) &&
                Objects.equals(this.data, that.data) &&
                Objects.equals(this.autoria, that.autoria) &&
                Objects.equals(this.opcoesImpressao, that.opcoesImpressao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataUltimaModificacao, aplicacao, versaoAplicacao, modoEdicao, metadados, proposicao, colegiado, epigrafe, componentes, comandoEmendaTextoLivre, comandoEmenda, justificativa, local, data, autoria, opcoesImpressao);
    }
    
    @Override
    public List<? extends Revisao> getRevisoes() {
    	return revisoes;
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
                "comandoEmendaTextoLivre=" + comandoEmendaTextoLivre + ", " +
                "comandoEmenda=" + comandoEmenda + ", " +
                "justificativa=" + justificativa + ", " +
                "local=" + local + ", " +
                "data=" + data + ", " +
                "autoria=" + autoria + ", " +
                "opcoesImpressao=" + opcoesImpressao + ']';
    }

}
