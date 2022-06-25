package br.gov.lexml.eta.etaservices.printing.xml;

import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.printing.DispositivoEmendaSuprimido;
import br.gov.lexml.eta.etaservices.printing.DispositivosEmenda;

import java.util.List;
import java.util.Objects;

public final class DispositivosEmendaRecord implements DispositivosEmenda {
    private final List<? extends DispositivoEmendaSuprimido> dispositivosSuprimidos;
    private final List<? extends DispositivoEmendaModificado> dispositivosModificados;
    private final List<? extends DispositivoEmendaAdicionado> dispositivosAdicionados;

    public DispositivosEmendaRecord(
            List<? extends DispositivoEmendaSuprimido> dispositivosSuprimidos,
            List<? extends DispositivoEmendaModificado> dispositivosModificados,
            List<? extends DispositivoEmendaAdicionado> dispositivosAdicionados) {
        this.dispositivosSuprimidos = dispositivosSuprimidos;
        this.dispositivosModificados = dispositivosModificados;
        this.dispositivosAdicionados = dispositivosAdicionados;
    }

    public List<? extends DispositivoEmendaSuprimido> getDispositivosSuprimidos() {
        return dispositivosSuprimidos;
    }

    public List<? extends DispositivoEmendaModificado> getDispositivosModificados() {
        return dispositivosModificados;
    }

    public List<? extends DispositivoEmendaAdicionado> getDispositivosAdicionados() {
        return dispositivosAdicionados;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        DispositivosEmendaRecord that = (DispositivosEmendaRecord) obj;
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
