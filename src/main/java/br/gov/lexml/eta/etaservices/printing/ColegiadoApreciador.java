package br.gov.lexml.eta.etaservices.printing;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.Objects;
import java.util.Optional;

public final class ColegiadoApreciador {
  private SiglaCasaLegislativa siglaCasaLegislativa;
  private TipoColegiado tipoColegiado;
  private String siglaComissao;

  public ColegiadoApreciador(
      final SiglaCasaLegislativa siglaCasaLegislativa,
      final TipoColegiado tipoColegiado,
      final String siglaComissao
  ) {
    this.siglaCasaLegislativa = siglaCasaLegislativa;
    this.tipoColegiado = tipoColegiado;
    this.siglaComissao = siglaComissao;
  }

  @XmlElement
  public SiglaCasaLegislativa getSiglaCasaLegislativa() {
    return siglaCasaLegislativa;
  }

  @XmlElement
  public TipoColegiado getTipoColegiado() {
    return tipoColegiado;
  }

  @XmlElement
  public String getSiglaComissao() {
    return siglaComissao;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (ColegiadoApreciador) obj;
    return Objects.equals(this.siglaCasaLegislativa, that.siglaCasaLegislativa) &&
        Objects.equals(this.tipoColegiado, that.tipoColegiado) &&
        Objects.equals(this.siglaComissao, that.siglaComissao);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siglaCasaLegislativa, tipoColegiado, siglaComissao);
  }

  @Override
  public String toString() {
    return "ColegiadoApreciador[" +
        "siglaCasaLegislativa=" + siglaCasaLegislativa + ", " +
        "tipoColegiado=" + tipoColegiado + ", " +
        "siglaComissao=" + siglaComissao + ']';
  }

}
