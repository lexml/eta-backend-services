package br.gov.lexml.eta.etaservices.printing;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.Objects;

public final class RefProposicaoEmendada {
  private String urn;
  private String sigla;
  private String numero;
  private String ano;
  private String ementa;
  private String identificacaoTexto;

  public RefProposicaoEmendada() {

  }

  public RefProposicaoEmendada(
      final String urn,
      final String sigla,
      final String numero,
      final String ano,
      final String ementa,
      final String identificacaoTexto
  ) {
    this.urn = urn;
    this.sigla = sigla;
    this.numero = numero;
    this.ano = ano;
    this.ementa = ementa;
    this.identificacaoTexto = identificacaoTexto;
  }

  @XmlElement
  public String getUrn() {
    return urn;
  }

  @XmlElement
  public String getSigla() {
    return sigla;
  }

  @XmlElement
  public String getNumero() {
    return numero;
  }

  @XmlElement
  public String getAno() {
    return ano;
  }

  @XmlElement
  public String getEmenta() {
    return ementa;
  }

  @XmlElement
  public String getIdentificacaoTexto() {
    return identificacaoTexto;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (RefProposicaoEmendada) obj;
    return Objects.equals(this.urn, that.urn) &&
        Objects.equals(this.sigla, that.sigla) &&
        Objects.equals(this.numero, that.numero) &&
        Objects.equals(this.ano, that.ano) &&
        Objects.equals(this.ementa, that.ementa) &&
        Objects.equals(this.identificacaoTexto, that.identificacaoTexto);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urn, sigla, numero, ano, ementa, identificacaoTexto);
  }

  @Override
  public String toString() {
    return "RefProposicaoEmendada[" +
        "urn=" + urn + ", " +
        "sigla=" + sigla + ", " +
        "numero=" + numero + ", " +
        "ano=" + ano + ", " +
        "ementa=" + ementa + ", " +
        "identificacaoTexto=" + identificacaoTexto + ']';
  }


}
