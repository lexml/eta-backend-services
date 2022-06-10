package br.gov.lexml.eta.etaservices.printing;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@XmlRootElement(name = "emenda")
public final class Emenda {
  private String dataUltimaModificacao;
  private String aplicacao;
  private String versaoAplicacao;
  private ModoEdicaoEmenda modoEdicao;
  private Map<String, Object> metadados;
  private RefProposicaoEmendada proposicao;
  private ColegiadoApreciador colegiado;
  private Epigrafe epigrafe;
  private List<ComponenteEmendado> componentes;
  private ComandoEmenda comandoEmenda;
  private String justificativa;
  private String local;
  private String data;
  private Autoria autoria;
  private OpcoesImpressao opcoesImpressao;

  public Emenda() {

  }
  public Emenda(
      final String dataUltimaModificacao,
      final String aplicacao,
      final String versaoAplicacao,
      final ModoEdicaoEmenda modoEdicao,
      final Map<String, Object> metadados,
      final RefProposicaoEmendada proposicao,
      final ColegiadoApreciador colegiado,
      final Epigrafe epigrafe,
      final List<ComponenteEmendado> componentes,
      final ComandoEmenda comandoEmenda,
      final String justificativa,
      final String local,
      final String data,
      final Autoria autoria,
      final OpcoesImpressao opcoesImpressao
  ) {
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

  @XmlAttribute public String getDataUltimaModificacao() {
    return dataUltimaModificacao;
  }

  @XmlAttribute public String getAplicacao() {
    return aplicacao;
  }

  @XmlAttribute public String getVersaoAplicacao() {
    return versaoAplicacao;
  }

  @XmlAttribute public ModoEdicaoEmenda getModoEdicao() {
    return modoEdicao;
  }

  @XmlElement public Map<String, Object> getMetadados() {
    return metadados;
  }

  @XmlElement public RefProposicaoEmendada getProposicao() {
    return proposicao;
  }

  @XmlElement public ColegiadoApreciador getColegiado() {
    return colegiado;
  }

  @XmlElement public Epigrafe getEpigrafe() {
    return epigrafe;
  }

  @XmlElement public List<ComponenteEmendado> getComponentes() {
    return componentes;
  }

  @XmlElement public ComandoEmenda getComandoEmenda() {
    return comandoEmenda;
  }

  @XmlElement public String getJustificativa() {
    return justificativa;
  }

  @XmlElement public String getLocal() {
    return local;
  }

  @XmlAttribute public String getData() {
    return data;
  }

  @XmlElement public Autoria getAutoria() {
    return autoria;
  }

  @XmlElement public OpcoesImpressao getOpcoesImpressao() {
    return opcoesImpressao;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (Emenda) obj;
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
    return Objects.hash(dataUltimaModificacao,
        aplicacao,
        versaoAplicacao,
        modoEdicao,
        metadados,
        proposicao,
        colegiado,
        epigrafe,
        componentes,
        comandoEmenda,
        justificativa,
        local,
        data,
        autoria,
        opcoesImpressao);
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
