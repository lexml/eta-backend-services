package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.ItemComandoEmenda;

import java.util.Objects;

public class ItemComandoEmendaPojo implements ItemComandoEmenda {
    private String cabecalho;
    private String citacao;
    private String rotulo;
    private String complemento;

    @Override
    public String getCabecalho() {
        return cabecalho;
    }

    @SuppressWarnings("unused")
    public void setCabecalho(String cabecalho) {
        this.cabecalho = cabecalho;
    }

    @Override
    public String getCitacao() {
        return citacao;
    }

    @SuppressWarnings("unused")
    public void setCitacao(String citacao) {
        this.citacao = citacao;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getComplemento() {
        return complemento;
    }

    @SuppressWarnings("unused")
    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemComandoEmendaPojo that = (ItemComandoEmendaPojo) o;
        return cabecalho.equals(that.cabecalho) && citacao.equals(that.citacao) && rotulo.equals(that.rotulo) && complemento.equals(that.complemento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cabecalho, citacao, rotulo, complemento);
    }

    @Override
    public String toString() {
        return "ItemComandoEmendaPojo{" +
                "cabecalho='" + cabecalho + '\'' +
                ", citacao='" + citacao + '\'' +
                ", rotulo='" + rotulo + '\'' +
                ", complemento='" + complemento + '\'' +
                '}';
    }
}
