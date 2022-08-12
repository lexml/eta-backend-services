package br.gov.lexml.eta.etaservices.parsing.xml;

import java.time.LocalDate;
import java.util.Objects;

class AtributosEmenda {
    private final String local;
    private final LocalDate data;

    public AtributosEmenda(String local, LocalDate data) {
        this.local = local;
        this.data = data;
    }

    public String getLocal() {
        return local;
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtributosEmenda that = (AtributosEmenda) o;
        return Objects.equals(local, that.local) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local, data);
    }

    @Override
    public String toString() {
        return "AtributosEmenda{" +
                "local='" + local + '\'' +
                ", data=" + data +
                '}';
    }
}
