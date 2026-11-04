package br.gov.lexml.eta.lexmljsonix.conversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SuppressWarnings("deprecation")
class ConversorLexmlJsonixImplTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConversorLexmlJsonixImpl.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void capturarLog() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void liberarLog() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void xmlToJsonDelegaAoTojson() throws Exception {
        ConversorRegistrado delegado = new ConversorRegistrado();
        ConversorLexmlJsonixImpl conversor = new ConversorLexmlJsonixImpl(propriedades(), "test", delegado);

        assertThat(conversor.xmlToJson("<xml/>")).isEqualTo("json de <xml/>");
        assertThat(delegado.chamadas).containsExactly("tojson:<xml/>");
    }

    @Test
    void jsonToXmlDelegaAoToxml() throws Exception {
        ConversorRegistrado delegado = new ConversorRegistrado();
        ConversorLexmlJsonixImpl conversor = new ConversorLexmlJsonixImpl(propriedades(), "test", delegado);

        assertThat(conversor.jsonToXml("{}")).isEqualTo("xml de {}");
        assertThat(delegado.chamadas).containsExactly("toxml:{}");
    }

    @Test
    void falhaNoXmlToJsonViraExcecaoComPrefixoECausa() throws Exception {
        EtaBackendException causa = new EtaBackendException("A conversão pelo jsonix-lexml (tojson) não produziu resultado: erro X");
        ConversorLexmlJsonixImpl conversor = new ConversorLexmlJsonixImpl(propriedades(), "test", new ConversorQueFalha(causa));

        assertThatThrownBy(() -> conversor.xmlToJson("<xml/>"))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Erro ao converter de xml para jsonix: " + causa.getMessage())
                .hasCause(causa);
    }

    @Test
    void falhaNoJsonToXmlViraExcecaoComPrefixoECausa() throws Exception {
        EtaBackendException causa = new EtaBackendException("A conversão pelo jsonix-lexml (toxml) terminou com código 1: erro Y");
        ConversorLexmlJsonixImpl conversor = new ConversorLexmlJsonixImpl(propriedades(), "test", new ConversorQueFalha(causa));

        assertThatThrownBy(() -> conversor.jsonToXml("{}"))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("Erro ao converter de json para xml: " + causa.getMessage())
                .hasCause(causa);
    }

    @Test
    void executavelInexistenteForaDoPerfilTestFalhaNaCriacao(@TempDir Path diretorio) {
        String cli = diretorio.resolve("jsonix-lexml-inexistente").toString();
        LexmlJsonixProperties propriedades = propriedades();
        propriedades.setCli(cli);

        assertThatThrownBy(() -> new ConversorLexmlJsonixImpl(propriedades, "unknown"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(cli);
    }

    @Test
    void executavelInexistenteNoPerfilTestNaoFalha(@TempDir Path diretorio) throws Exception {
        LexmlJsonixProperties propriedades = propriedades();
        propriedades.setCli(diretorio.resolve("jsonix-lexml-inexistente").toString());

        assertThat(new ConversorLexmlJsonixImpl(propriedades, "test")).isNotNull();
    }

    @Test
    void usarEntradaSaidaPadraoVerdadeiroRegistraAviso() throws Exception {
        LexmlJsonixProperties propriedades = propriedades();
        propriedades.setUsarEntradaSaidaPadraoDuranteConversao(true);

        new ConversorLexmlJsonixImpl(propriedades, "test", new ConversorRegistrado());

        assertThat(avisos()).singleElement().asString()
                .contains("lexml-jsonix.usar-entrada-saida-padrao-durante-conversao", "obsoleta");
    }

    @Test
    void removerDiretorioFalsoRegistraAviso() throws Exception {
        LexmlJsonixProperties propriedades = propriedades();
        propriedades.setRemoverDiretorioAposConversao(false);

        new ConversorLexmlJsonixImpl(propriedades, "test", new ConversorRegistrado());

        assertThat(avisos()).singleElement().asString()
                .contains("lexml-jsonix.remover-diretorio-apos-conversao", "obsoleta");
    }

    @Test
    void propriedadesObsoletasComOComportamentoAtualNaoRegistramAviso() throws Exception {
        LexmlJsonixProperties propriedades = propriedades();
        propriedades.setUsarEntradaSaidaPadraoDuranteConversao(false);
        propriedades.setRemoverDiretorioAposConversao(true);

        new ConversorLexmlJsonixImpl(propriedades, "test", new ConversorRegistrado());

        assertThat(avisos()).isEmpty();
    }

    @Test
    void semPropriedadesObsoletasNaoRegistraAviso() throws Exception {
        new ConversorLexmlJsonixImpl(propriedades(), "test", new ConversorRegistrado());

        assertThat(avisos()).isEmpty();
    }

    private List<String> avisos() {
        return appender.list.stream()
                .filter(evento -> evento.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    private static LexmlJsonixProperties propriedades() {
        LexmlJsonixProperties propriedades = new LexmlJsonixProperties();
        propriedades.setCli("jsonix-lexml");
        return propriedades;
    }

    private static class ConversorRegistrado implements ConversorDocumentoArticulado {

        private final List<String> chamadas = new java.util.ArrayList<>();

        @Override
        public String jsonParaXml(String json) {
            chamadas.add("toxml:" + json);
            return "xml de " + json;
        }

        @Override
        public String xmlParaJson(String xml) {
            chamadas.add("tojson:" + xml);
            return "json de " + xml;
        }
    }

    private static class ConversorQueFalha implements ConversorDocumentoArticulado {

        private final RuntimeException erro;

        ConversorQueFalha(RuntimeException erro) {
            this.erro = erro;
        }

        @Override
        public String jsonParaXml(String json) {
            throw erro;
        }

        @Override
        public String xmlParaJson(String xml) {
            throw erro;
        }
    }

}
