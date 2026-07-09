package br.gov.lexml.eta.etaservices.printing.json;

import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaSuprimido;
import br.gov.lexml.eta.etaservices.emenda.DispositivosEmenda;

import java.util.List;
import java.util.Objects;

public class DispositivosEmendaPojo implements DispositivosEmenda {
    private List<? extends DispositivoEmendaSuprimidoPojo> dispositivosSuprimidos;
    private List<? extends DispositivoEmendaModificadoPojo> dispositivosModificados;
    private List<? extends DispositivoEmendaAdicionadoPojo> dispositivosAdicionados;

    public DispositivosEmendaPojo() {
    }

    public DispositivosEmendaPojo(List<? extends DispositivoEmendaSuprimidoPojo> dispositivosSuprimidos,
                                  List<? extends DispositivoEmendaModificadoPojo> dispositivosModificados,
                                  List<? extends DispositivoEmendaAdicionadoPojo> dispositivosAdicionados) {
        this.dispositivosSuprimidos = dispositivosSuprimidos;
        this.dispositivosModificados = dispositivosModificados;
        this.dispositivosAdicionados = dispositivosAdicionados;
    }

    @Override
    public List<? extends DispositivoEmendaSuprimido> getDispositivosSuprimidos() {
        return dispositivosSuprimidos;
    }

    @SuppressWarnings("unused")
    public void setDispositivosSuprimidos(List<? extends DispositivoEmendaSuprimidoPojo> dispositivosSuprimidos) {
        this.dispositivosSuprimidos = dispositivosSuprimidos;
    }

    @Override
    public List<? extends DispositivoEmendaModificado> getDispositivosModificados() {
        return dispositivosModificados;
    }

    @SuppressWarnings("unused")
    public void setDispositivosModificados(List<? extends DispositivoEmendaModificadoPojo> dispositivosModificados) {
        this.dispositivosModificados = dispositivosModificados;
    }

    @Override
    public List<? extends DispositivoEmendaAdicionado> getDispositivosAdicionados() {
        return dispositivosAdicionados;
    }

    @SuppressWarnings("unused")
    public void setDispositivosAdicionados(List<? extends DispositivoEmendaAdicionadoPojo> dispositivosAdicionados) {
        this.dispositivosAdicionados = dispositivosAdicionados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivosEmendaPojo that = (DispositivosEmendaPojo) o;
        return Objects.equals(dispositivosSuprimidos, that.dispositivosSuprimidos) && Objects.equals(dispositivosModificados, that.dispositivosModificados) && Objects.equals(dispositivosAdicionados, that.dispositivosAdicionados);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dispositivosSuprimidos, dispositivosModificados, dispositivosAdicionados);
    }

    @Override
    public String toString() {
        return "DispositivosEmendaPojo{" +
                "dispositivosSuprimidos=" + dispositivosSuprimidos +
                ", dispositivosModificados=" + dispositivosModificados +
                ", dispositivosAdicionados=" + dispositivosAdicionados +
                '}';
    }
}
