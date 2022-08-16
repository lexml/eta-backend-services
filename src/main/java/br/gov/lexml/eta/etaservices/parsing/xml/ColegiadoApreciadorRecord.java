package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.emenda.TipoColegiado;

import java.util.Objects;

public final class ColegiadoApreciadorRecord implements ColegiadoApreciador {
    private final SiglaCasaLegislativa siglaCasaLegislativa;
    private final TipoColegiado tipoColegiado;
    private final String siglaComissao;

    public ColegiadoApreciadorRecord(
            SiglaCasaLegislativa siglaCasaLegislativa,
            TipoColegiado tipoColegiado,
            String siglaComissao) {
        this.siglaCasaLegislativa = siglaCasaLegislativa;
        this.tipoColegiado = tipoColegiado;
        this.siglaComissao = siglaComissao;
    }

    public SiglaCasaLegislativa getSiglaCasaLegislativa() {
        return siglaCasaLegislativa;
    }

    public TipoColegiado getTipoColegiado() {
        return tipoColegiado;
    }

    public String getSiglaComissao() {
        return siglaComissao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ColegiadoApreciadorRecord that = (ColegiadoApreciadorRecord) obj;
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
