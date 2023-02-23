package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.emenda.NotaAlteracao;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

public final class DispositivoEmendaModificadoRecord implements DispositivoEmendaModificado {
    private final String tipo;
    private final String id;
    private final String rotulo;
    private final String texto;
    private final Boolean textoOmitido;
    private final Boolean abreAspas;
    private final Boolean fechaAspas;
    private final NotaAlteracao notaAlteracao;
    private final String urnNormaAlterada;

    public DispositivoEmendaModificadoRecord(
            String tipo,
            String id,
            String rotulo,
            String texto,
            Boolean textoOmitido,
            Boolean abreAspas,
            Boolean fechaAspas,
            NotaAlteracao notaAlteracao,
            String urnNormaAlterada) {
        this.tipo = tipo;
        this.id = id;
        this.rotulo = rotulo;
        this.texto = texto;
        this.textoOmitido = textoOmitido;
        this.abreAspas = abreAspas;
        this.fechaAspas = fechaAspas;
        this.notaAlteracao = notaAlteracao;
        this.urnNormaAlterada = urnNormaAlterada;
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

    @Override
	public String getUrnNormaAlterada() {
        return urnNormaAlterada;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        DispositivoEmendaModificadoRecord that = (DispositivoEmendaModificadoRecord) obj;
        return Objects.equals(this.tipo, that.tipo) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.rotulo, that.rotulo) &&
                Objects.equals(this.texto, that.texto) &&
                Objects.equals(this.textoOmitido, that.textoOmitido) &&
                Objects.equals(this.abreAspas, that.abreAspas) &&
                Objects.equals(this.fechaAspas, that.fechaAspas) &&
                Objects.equals(this.notaAlteracao, that.notaAlteracao) &&
                Objects.equals(this.urnNormaAlterada, this.urnNormaAlterada);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, id, rotulo, texto, textoOmitido, abreAspas, fechaAspas, notaAlteracao, urnNormaAlterada);
    }

    @Override
    public String toString() {
        return "DispositivoEmendaModificado[" +
                "tipo=" + tipo + ", " +
                "id=" + id + ", " +
                "rotulo=" + rotulo + ", " +
                "texto=" + texto + ", " +
                "textoOmitido=" + textoOmitido + ", " +
                "abreAspas=" + abreAspas + ", " +
                "fechaAspas=" + fechaAspas + ", " +
                "notaAlteracao=" + notaAlteracao + ", " +
                "urnNormaAlterada=" + urnNormaAlterada + ']';
    }


}
