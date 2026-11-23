package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MedidasFoTest {

    @Test
    void converteComprimentosParaMilimetros() {
        assertThat(MedidasFo.milimetros("2.5cm", 164, 14, 0)).isCloseTo(25.0, within(0.01));
        assertThat(MedidasFo.milimetros("10mm", 164, 14, 0)).isCloseTo(10.0, within(0.01));
        assertThat(MedidasFo.milimetros("72pt", 164, 14, 0)).isCloseTo(25.4, within(0.01));
        assertThat(MedidasFo.milimetros("1in", 164, 14, 0)).isCloseTo(25.4, within(0.01));
        assertThat(MedidasFo.milimetros("50%", 164, 14, 0)).isCloseTo(82.0, within(0.01));
        assertThat(MedidasFo.milimetros("1em", 164, 14, 0)).isCloseTo(14 * 25.4 / 72.0, within(0.01));
        assertThat(MedidasFo.milimetros(".3cm", 164, 14, 0)).isCloseTo(3.0, within(0.01));
        assertThat(MedidasFo.milimetros("invalido", 164, 14, 99.0)).isEqualTo(99.0);
    }

    @Test
    void converteFontSizeEmPontos() {
        assertThat(MedidasFo.pontos("14pt", 14, 12)).isEqualTo(14.0);
        assertThat(MedidasFo.pontos("80%", 14, 12)).isCloseTo(11.2, within(0.01));
        assertThat(MedidasFo.pontos("0.7em", 14, 12)).isCloseTo(9.8, within(0.01));
        assertThat(MedidasFo.pontos(null, 14, 12)).isEqualTo(12.0);
    }

    @Test
    void converteTwipsEMeiosPontos() {
        assertThat(MedidasFo.twips("2.5cm", 164, 14, 0)).isEqualTo(1417);
        assertThat(MedidasFo.twips("48pt", 164, 14, 0)).isEqualTo(960);
        assertThat(MedidasFo.twips("0", 164, 14, 99)).isZero();
        assertThat(MedidasFo.ptParaMeiosPontos(14)).isEqualTo(28);
        assertThat(MedidasFo.ptParaMeiosPontos(11.2)).isEqualTo(22);
    }

    @Test
    void converteEntrelinhaDoPdfParaWord() {
        assertThat(MedidasFo.entrelinhaWord("150%")).isEqualTo(313);
        assertThat(MedidasFo.entrelinhaWord("120%")).isEqualTo(250);
        assertThat(MedidasFo.entrelinhaWord("1.5em")).isEqualTo(313);
        assertThat(MedidasFo.entrelinhaWord(null)).isEqualTo(240);
    }

    @Test
    void normalizaCoresHexENomeadas() {
        assertThat(MedidasFo.corHex("yellow")).isEqualTo("FFFF00");
        assertThat(MedidasFo.corHex("red")).isEqualTo("FF0000");
        assertThat(MedidasFo.corHex("#808080")).isEqualTo("808080");
        assertThat(MedidasFo.corHex("#f0a")).isEqualTo("FF00AA");
        assertThat(MedidasFo.corHex("desconhecida")).isNull();
    }

}
