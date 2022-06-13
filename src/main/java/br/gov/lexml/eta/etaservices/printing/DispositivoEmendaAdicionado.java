package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Objects;

public final class DispositivoEmendaAdicionado implements DispositivoEmenda {
    private String tipo;
    private String id;
    private String rotulo;
    private String idPai;
    private String idIrmaoAnterior;
    private String urnNormaAlterada;
    private Boolean existeNaNormaAlterada;
    private List<DispositivoEmendaAdicionado> filhos;

    public DispositivoEmendaAdicionado(
            final String tipo,
            final String id,
            final String rotulo,
            final String idPai,
            final String idIrmaoAnterior,
            final String urnNormaAlterada,
            final Boolean existeNaNormaAlterada,
            final List<DispositivoEmendaAdicionado> filhos
    ) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
        this.idPai = idPai;
        this.idIrmaoAnterior = idIrmaoAnterior;
        this.urnNormaAlterada = urnNormaAlterada;
        this.existeNaNormaAlterada = existeNaNormaAlterada;
        this.filhos = filhos;
    }

    public DispositivoEmendaAdicionado() {

    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    public String getIdPai() {
        return idPai;
    }

    public String getIdIrmaoAnterior() {
        return idIrmaoAnterior;
    }

    public String getUrnNormaAlterada() {
        return urnNormaAlterada;
    }

    public Boolean getExisteNaNormaAlterada() {
        return existeNaNormaAlterada;
    }

    public List<DispositivoEmendaAdicionado> getFilhos() {
        return filhos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DispositivoEmendaAdicionado) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.rotulo, that.rotulo) &&
                Objects.equals(this.idPai, that.idPai) &&
                Objects.equals(this.idIrmaoAnterior, that.idIrmaoAnterior) &&
                Objects.equals(this.urnNormaAlterada, that.urnNormaAlterada) &&
                Objects.equals(this.existeNaNormaAlterada, that.existeNaNormaAlterada) &&
                Objects.equals(this.filhos, that.filhos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, idPai, idIrmaoAnterior, urnNormaAlterada, existeNaNormaAlterada, filhos);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaAdicionado[" +
                "tipo=" + tipo + ", " +
                "id=" + id + ", " +
                "rotulo=" + rotulo + ", " +
                "idPai=" + idPai + ", " +
                "idIrmaoAnterior=" + idIrmaoAnterior + ", " +
                "urnNormaAlterada=" + urnNormaAlterada + ", " +
                "existeNaNormaAlterada=" + existeNaNormaAlterada + ", " +
                "filhos=" + filhos + ']';
    }

}
