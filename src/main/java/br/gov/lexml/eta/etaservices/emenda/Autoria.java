package br.gov.lexml.eta.etaservices.emenda;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vavr.Tuple2;

public interface Autoria {
    TipoAutoria getTipo();

    boolean isImprimirPartidoUF();

    int getQuantidadeAssinaturasAdicionaisSenadores();

    @SuppressWarnings("unused")
    @JsonIgnore()
    default int[] getAssinaturasAdicionaisSenadores() {
        return IntStream.range(0, getQuantidadeAssinaturasAdicionaisSenadores())
                .toArray();
    }

    int getQuantidadeAssinaturasAdicionaisDeputados();

    @SuppressWarnings("unused")
    @JsonIgnore()
    default int[] getAssinaturasAdicionaisDeputados() {
        return IntStream.range(0, getQuantidadeAssinaturasAdicionaisDeputados())
                .toArray();
    }

    List<? extends Parlamentar> getParlamentares();

    @SuppressWarnings("unused")
    @JsonIgnore()
    default List<Tuple2<? extends Parlamentar, ? extends Parlamentar>> getParlamentaresPair() {
        final List<Tuple2<? extends Parlamentar, ? extends Parlamentar>> result = new ArrayList<>();
        final int listSize = getParlamentares().size();
        for (int i = 0; i < listSize; i += 2) {
            if (listSize == i + 1) {
                result.add(new Tuple2<>(getParlamentares().get(i), null));
            } else {
                result.add(new Tuple2<>(getParlamentares().get(i), getParlamentares().get(i + 1)));
            }

        }
        return result;
    }

    ColegiadoAutor getColegiado();
}
