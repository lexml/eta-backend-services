package br.gov.lexml.eta.lexmljsonix.conversor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

@SuppressWarnings("deprecation")
class LexmlJsonixPropertiesTest {

    @Test
    void semPropriedadesInformadasUsaTimeoutDe60sEDeixaAsObsoletasSemValor() {
        LexmlJsonixProperties propriedades = vincular(new HashMap<>());

        assertThat(propriedades.getTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(propriedades.getUsarEntradaSaidaPadraoDuranteConversao()).isNull();
        assertThat(propriedades.getRemoverDiretorioAposConversao()).isNull();
    }

    @Test
    void aceitaTimeoutEPropriedadesObsoletasInformadas() {
        Map<String, String> valores = new HashMap<>();
        valores.put("lexml-jsonix.timeout", "2m");
        valores.put("lexml-jsonix.usar-entrada-saida-padrao-durante-conversao", "true");
        valores.put("lexml-jsonix.remover-diretorio-apos-conversao", "false");

        LexmlJsonixProperties propriedades = vincular(valores);

        assertThat(propriedades.getTimeout()).isEqualTo(Duration.ofMinutes(2));
        assertThat(propriedades.getUsarEntradaSaidaPadraoDuranteConversao()).isTrue();
        assertThat(propriedades.getRemoverDiretorioAposConversao()).isFalse();
    }

    private static LexmlJsonixProperties vincular(Map<String, String> valores) {
        valores.put("lexml-jsonix.cli", "jsonix-lexml");
        Binder binder = new Binder(new MapConfigurationPropertySource(valores));
        return binder.bindOrCreate("lexml-jsonix", LexmlJsonixProperties.class);
    }

}
