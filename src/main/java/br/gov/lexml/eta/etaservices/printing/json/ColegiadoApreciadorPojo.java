package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.printing.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.printing.TipoColegiado;

import java.util.Objects;

public class ColegiadoApreciadorPojo implements ColegiadoApreciador {
    private SiglaCasaLegislativa siglaCasaLegislativa;
    private TipoColegiado tipoColegiado;
    private String siglaComissao;

    @Override
    public SiglaCasaLegislativa getSiglaCasaLegislativa() {
        return siglaCasaLegislativa;
    }

    @SuppressWarnings("unused")
    public void setSiglaCasaLegislativa(SiglaCasaLegislativa siglaCasaLegislativa) {
        this.siglaCasaLegislativa = siglaCasaLegislativa;
    }

    @Override
    public TipoColegiado getTipoColegiado() {
        return tipoColegiado;
    }

    @SuppressWarnings("unused")
    public void setTipoColegiado(TipoColegiado tipoColegiado) {
        this.tipoColegiado = tipoColegiado;
    }

    @Override
    public String getSiglaComissao() {
        return siglaComissao;
    }

    @SuppressWarnings("unused")
    public void setSiglaComissao(String siglaComissao) {
        this.siglaComissao = siglaComissao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColegiadoApreciadorPojo that = (ColegiadoApreciadorPojo) o;
        return siglaCasaLegislativa == that.siglaCasaLegislativa && tipoColegiado == that.tipoColegiado && siglaComissao.equals(that.siglaComissao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(siglaCasaLegislativa, tipoColegiado, siglaComissao);
    }

    @Override
    public String toString() {
        return "ColegiadoApreciadorPojo{" +
                "siglaCasaLegislativa=" + siglaCasaLegislativa +
                ", tipoColegiado=" + tipoColegiado +
                ", siglaComissao='" + siglaComissao + '\'' +
                '}';
    }
}
