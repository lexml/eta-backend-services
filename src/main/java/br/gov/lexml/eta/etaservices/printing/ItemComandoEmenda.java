package br.gov.lexml.eta.etaservices.printing;

import java.util.Objects;

public final class ItemComandoEmenda {
    private String cabecalho;
    private String citacao;
    private String rotulo;
    private String complemento;

    public ItemComandoEmenda(
            final String cabecalho,
            final String citacao,
            final String rotulo,
            final String complemento
    ) {
        this.cabecalho = cabecalho;
        this.citacao = citacao;
        this.rotulo = rotulo;
        this.complemento = complemento;
    }

    public ItemComandoEmenda() {
    }

    public String getCabecalho() {
        return cabecalho;
    }

    public String getCitacao() {
        return citacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getComplemento() {
        return complemento;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ItemComandoEmenda) obj;
        return Objects.equals(this.cabecalho, that.cabecalho) &&
                Objects.equals(this.citacao, that.citacao) &&
                Objects.equals(this.rotulo, that.rotulo) &&
                Objects.equals(this.complemento, that.complemento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cabecalho, citacao, rotulo, complemento);
    }

    @Override
    public String toString() {
        return "ItemComandoEmenda[" +
                "cabecalho=" + cabecalho + ", " +
                "citacao=" + citacao + ", " +
                "rotulo=" + rotulo + ", " +
                "complemento=" + complemento + ']';
    }

}
