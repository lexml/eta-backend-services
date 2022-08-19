package br.gov.lexml.eta.etaservices.emenda;

import io.vavr.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public interface Autoria {
    TipoAutoria getTipo();

    boolean isImprimirPartidoUF();

    int getQuantidadeAssinaturasAdicionaisSenadores();

    @SuppressWarnings("unused")
    default int[] getAssinaturasAdicionaisSenadores() {
        return IntStream.range(0, getQuantidadeAssinaturasAdicionaisSenadores())
                .toArray();
    }

    int getQuantidadeAssinaturasAdicionaisDeputados();

    @SuppressWarnings("unused")
    default int[] getAssinaturasAdicionaisDeputados() {
        return IntStream.range(0, getQuantidadeAssinaturasAdicionaisDeputados())
                .toArray();
    }

    List<? extends Parlamentar> getParlamentares();

    @SuppressWarnings("unused")
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