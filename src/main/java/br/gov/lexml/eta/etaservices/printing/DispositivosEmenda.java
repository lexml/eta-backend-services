package br.gov.lexml.eta.etaservices.printing;

import java.util.List;
import java.util.Objects;

public final class DispositivosEmenda {
    private List<DispositivoEmendaSuprimido> dispositivosSuprimidos;
    private List<DispositivoEmendaModificado> dispositivosModificados;
    private List<DispositivoEmendaAdicionado> dispositivosAdicionados;

    public DispositivosEmenda(
            final List<DispositivoEmendaSuprimido> dispositivosSuprimidos,
            final List<DispositivoEmendaModificado> dispositivosModificados,
            final List<DispositivoEmendaAdicionado> dispositivosAdicionados
    ) {
        this.dispositivosSuprimidos = dispositivosSuprimidos;
        this.dispositivosModificados = dispositivosModificados;
        this.dispositivosAdicionados = dispositivosAdicionados;
    }

    public DispositivosEmenda() {

    }

    public List<DispositivoEmendaSuprimido> getDispositivosSuprimidos() {
        return dispositivosSuprimidos;
    }

    public List<DispositivoEmendaModificado> getDispositivosModificados() {
        return dispositivosModificados;
    }

    public List<DispositivoEmendaAdicionado> getDispositivosAdicionados() {
        return dispositivosAdicionados;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DispositivosEmenda) obj;
        return Objects.equals(this.dispositivosSuprimidos, that.dispositivosSuprimidos) &&
                Objects.equals(this.dispositivosModificados, that.dispositivosModificados) &&
                Objects.equals(this.dispositivosAdicionados, that.dispositivosAdicionados);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dispositivosSuprimidos, dispositivosModificados, dispositivosAdicionados);
    }

    @Override
    public String toString() {
        return "DispositivosEmenda[" +
                "dispositivosSuprimidos=" + dispositivosSuprimidos + ", " +
                "dispositivosModificados=" + dispositivosModificados + ", " +
                "dispositivosAdicionados=" + dispositivosAdicionados + ']';
    }

}
