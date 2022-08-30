package br.gov.lexml.eta.etaservices.parsing.xml;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.emenda.NotaAlteracao;

public final class DispositivoEmendaAdicionadoRecord implements DispositivoEmendaAdicionado {
    private final String tipo;
    private final String id;
    private final String rotulo;
    private final String texto;
    private final Boolean textoOmitido;
    private final Boolean abreAspas;
    private final Boolean fechaAspas;
    private final NotaAlteracao notaAlteracao;
    private final Boolean ondeCouber;
    private final String idPai;
    private final String idIrmaoAnterior;
    private final String urnNormaAlterada;
    private final Boolean existeNaNormaAlterada;
    private final List<DispositivoEmendaAdicionadoRecord> filhos;

    public DispositivoEmendaAdicionadoRecord(
            String tipo,
            String id,
            String rotulo,
            String texto,
            Boolean textoOmitido,
            Boolean abreAspas,
            Boolean fechaAspas,
            NotaAlteracao notaAlteracao,
            Boolean ondeCouber,
            String idPai,
            String idIrmaoAnterior,
            String urnNormaAlterada,
            Boolean existeNaNormaAlterada,
            List<DispositivoEmendaAdicionadoRecord> filhos) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
        this.texto = texto;
        this.textoOmitido = textoOmitido;
        this.abreAspas = abreAspas;
        this.fechaAspas = fechaAspas;
        this.notaAlteracao = notaAlteracao;
        this.ondeCouber = ondeCouber;
        this.idPai = idPai;
        this.idIrmaoAnterior = idIrmaoAnterior;
        this.urnNormaAlterada = urnNormaAlterada;
        this.existeNaNormaAlterada = existeNaNormaAlterada;
        this.filhos = filhos;
    }

    public String getTipo() {
        return tipo;
    }

    public String getId() {
        return id;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getTexto() {
        return texto;
    }

    @JsonInclude(Include.NON_DEFAULT)
    public Boolean isTextoOmitido() {
        return textoOmitido;
    }

    @JsonInclude(Include.NON_DEFAULT)
    public Boolean isAbreAspas() {
        return abreAspas;
    }

    @JsonInclude(Include.NON_DEFAULT)
    public Boolean isFechaAspas() {
        return fechaAspas;
    }

    public NotaAlteracao getNotaAlteracao() {
        return notaAlteracao;
    }

    @JsonInclude(Include.NON_DEFAULT)
    public Boolean isOndeCouber() {
        return ondeCouber;
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

    @JsonInclude(Include.NON_DEFAULT)
    public Boolean isExisteNaNormaAlterada() {
        return existeNaNormaAlterada;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<DispositivoEmendaAdicionadoRecord> getFilhos() {
        return filhos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        DispositivoEmendaAdicionadoRecord that = (DispositivoEmendaAdicionadoRecord) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.rotulo, that.rotulo) &&
                Objects.equals(this.texto, that.texto) &&
                Objects.equals(this.textoOmitido, that.textoOmitido) &&
                Objects.equals(this.abreAspas, that.abreAspas) &&
                Objects.equals(this.fechaAspas, that.fechaAspas) &&
                Objects.equals(this.notaAlteracao, that.notaAlteracao) &&
                Objects.equals(this.ondeCouber, that.ondeCouber) &&
                Objects.equals(this.idPai, that.idPai) &&
                Objects.equals(this.idIrmaoAnterior, that.idIrmaoAnterior) &&
                Objects.equals(this.urnNormaAlterada, that.urnNormaAlterada) &&
                Objects.equals(this.existeNaNormaAlterada, that.existeNaNormaAlterada) &&
                Objects.equals(this.filhos, that.filhos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao, ondeCouber, idPai, idIrmaoAnterior, urnNormaAlterada, existeNaNormaAlterada, filhos);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaAdicionado[" +
                "tipo=" + tipo + ", " +
                "id=" + id + ", " +
                "rotulo=" + rotulo + ", " +
                "texto=" + texto + ", " +
                "textoOmitido=" + textoOmitido + ", " +
                "abreAspas=" + abreAspas + ", " +
                "fechaAspas=" + fechaAspas + ", " +
                "notaAlteracao=" + notaAlteracao + ", " +
                "ondeCouber=" + ondeCouber + ", " +
                "idPai=" + idPai + ", " +
                "idIrmaoAnterior=" + idIrmaoAnterior + ", " +
                "urnNormaAlterada=" + urnNormaAlterada + ", " +
                "existeNaNormaAlterada=" + existeNaNormaAlterada + ", " +
                "filhos=" + filhos + ']';
    }


}
