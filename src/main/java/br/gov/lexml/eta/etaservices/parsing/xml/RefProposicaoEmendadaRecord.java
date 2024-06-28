package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.Objects;

import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;

public final class RefProposicaoEmendadaRecord implements RefProposicaoEmendada {
    private final String urn;
    private final String sigla;
    private final String numero;
    private final String ano;
    private final String ementa;
    private final String identificacaoTexto;
    private final boolean emendarTextoSubstitutivo;

    public RefProposicaoEmendadaRecord(
            String urn,
            String sigla,
            String numero,
            String ano,
            String ementa,
            String identificacaoTexto,
            boolean emendarTextoSubstitutivo) {
        this.urn = urn;
        this.sigla = sigla;
        this.numero = numero;
        this.ano = ano;
        this.ementa = ementa;
        this.identificacaoTexto = identificacaoTexto;
        this.emendarTextoSubstitutivo = emendarTextoSubstitutivo;
    }

    public String getUrn() {
        return urn;
    }

    public String getSigla() {
        return sigla;
    }

    public String getNumero() {
        return numero;
    }

    public String getAno() {
        return ano;
    }

    public String getEmenta() {
        return ementa;
    }

    public String getIdentificacaoTexto() {
        return identificacaoTexto;
    }

    public boolean isEmendarTextoSubstitutivo() {
		return emendarTextoSubstitutivo;
	}
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        RefProposicaoEmendadaRecord that = (RefProposicaoEmendadaRecord) obj;
        return Objects.equals(this.urn, that.urn) &&
                Objects.equals(this.sigla, that.sigla) &&
                Objects.equals(this.numero, that.numero) &&
                Objects.equals(this.ano, that.ano) &&
                Objects.equals(this.ementa, that.ementa) &&
                Objects.equals(this.identificacaoTexto, that.identificacaoTexto) &&
                Objects.equals(this.emendarTextoSubstitutivo, that.emendarTextoSubstitutivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, sigla, numero, ano, ementa, identificacaoTexto, emendarTextoSubstitutivo);
    }

    @Override
    public String toString() {
        return "RefProposicaoEmendadaRecord[" +
                "urn=" + urn + ", " +
                "sigla=" + sigla + ", " +
                "numero=" + numero + ", " +
                "ano=" + ano + ", " +
                "ementa=" + ementa + ", " +
                "identificacaoTexto=" + identificacaoTexto + ", " +
                "emendarTextoSubstitutivo=" + emendarTextoSubstitutivo + ']';
    }
}
