package br.gov.lexml.eta.etaservices.documentoarticulado.conversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Testa o conversor por linha de comando sem o jsonix-lexml real: o "executável" é a JVM corrente
 * rodando {@link JsonixLexmlSimulado}.
 */
class ConversorDocumentoArticuladoCliTest {

    private static final Duration TIMEOUT_PADRAO = Duration.ofSeconds(30);

    @TempDir
    Path tempDir;

    private Path diretorioBase;

    @BeforeEach
    void setUp() throws IOException {
        diretorioBase = Files.createDirectories(tempDir.resolve("temporarios"));
    }

    @Test
    void jsonParaXmlPreservaAcentosEmUtf8() {
        ConversorDocumentoArticuladoCli conversor = conversor("eco", TIMEOUT_PADRAO, classpathAtual());

        String resultado = conversor.jsonParaXml("{\"texto\":\"Sala das Sessões — Fica instituído\"}");

        assertThat(resultado).isEqualTo("toxml:{\"texto\":\"Sala das Sessões — Fica instituído\"}");
    }

    @Test
    void xmlParaJsonUsaSubcomandoTojson() {
        ConversorDocumentoArticuladoCli conversor = conversor("eco", TIMEOUT_PADRAO, classpathAtual());

        assertThat(conversor.xmlParaJson("<LexML>ação</LexML>")).isEqualTo("tojson:<LexML>ação</LexML>");
    }

    @Test
    void executavelInexistente() {
        Path inexistente = tempDir.resolve("nao-existe").resolve("jsonix-lexml");
        ConversorDocumentoArticuladoCli conversor = new ConversorDocumentoArticuladoCli(inexistente, TIMEOUT_PADRAO);

        assertThatThrownBy(() -> conversor.jsonParaXml("{}"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining(inexistente.toString());
    }

    @Test
    void codigoZeroSemSaidaEhFalhaComMensagemDoStderr() {
        ConversorDocumentoArticuladoCli conversor = conversor("erro-silencioso", TIMEOUT_PADRAO, classpathAtual());

        assertThatThrownBy(() -> conversor.jsonParaXml("{\"invalido\":1}"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("is not known in this context");
    }

    @Test
    void saidaVaziaEhFalha() {
        ConversorDocumentoArticuladoCli conversor = conversor("saida-vazia", TIMEOUT_PADRAO, classpathAtual());

        assertThatThrownBy(() -> conversor.xmlParaJson("<LexML/>"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("não produziu resultado");
    }

    @Test
    void codigoDiferenteDeZeroEhFalha() {
        ConversorDocumentoArticuladoCli conversor = conversor("codigo-erro", TIMEOUT_PADRAO, classpathAtual());

        assertThatThrownBy(() -> conversor.jsonParaXml("{}"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("código 3")
                .hasMessageContaining("falha simulada");
    }

    @Test
    void tempoLimiteExcedidoEncerraProcesso() {
        ConversorDocumentoArticuladoCli conversor = conversor("dormir", Duration.ofSeconds(2), classpathAtual());

        long inicio = System.nanoTime();
        assertThatThrownBy(() -> conversor.jsonParaXml("{}"))
                .isInstanceOf(EtaBackendException.class)
                .hasMessageContaining("tempo limite");
        assertThat(Duration.ofNanos(System.nanoTime() - inicio)).isLessThan(Duration.ofSeconds(30));
        assertThat(diretorioBase).isEmptyDirectory();
    }

    /** Nomes só com ASCII para não depender da codificação de caminhos do ambiente de build. */
    @Test
    void caminhosComEspacos() throws Exception {
        Path classpathComEspaco = copiarSimuladoPara(tempDir.resolve("classes com espaco"));
        diretorioBase = Files.createDirectories(tempDir.resolve("temporarios com espaco"));
        ConversorDocumentoArticuladoCli conversor = conversor("eco", TIMEOUT_PADRAO, classpathComEspaco.toString());

        assertThat(conversor.jsonParaXml("{}")).isEqualTo("toxml:{}");
    }

    @Test
    void naoDeixaDiretoriosTemporarios() {
        conversor("eco", TIMEOUT_PADRAO, classpathAtual()).jsonParaXml("{}");
        assertThatThrownBy(() -> conversor("erro-silencioso", TIMEOUT_PADRAO, classpathAtual()).jsonParaXml("{}"))
                .isInstanceOf(EtaBackendException.class);
        assertThatThrownBy(() -> conversor("codigo-erro", TIMEOUT_PADRAO, classpathAtual()).xmlParaJson("<LexML/>"))
                .isInstanceOf(EtaBackendException.class);

        assertThat(diretorioBase).isEmptyDirectory();
    }

    private ConversorDocumentoArticuladoCli conversor(String modo, Duration timeout, String classpath) {
        List<String> comando = Arrays.asList(javaAtual(), "-cp", classpath, JsonixLexmlSimulado.class.getName(), modo);
        return new ConversorDocumentoArticuladoCli(comando, timeout, diretorioBase);
    }

    private static String javaAtual() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String classpathAtual() {
        try {
            return Paths.get(JsonixLexmlSimulado.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Copia o .class do simulador para um diretório de classpath próprio. */
    private static Path copiarSimuladoPara(Path destino) throws IOException {
        String relativo = JsonixLexmlSimulado.class.getName().replace('.', '/') + ".class";
        Path origem = Paths.get(classpathAtual()).resolve(relativo);
        Path alvo = destino.resolve(relativo);
        Files.createDirectories(alvo.getParent());
        Files.copy(origem, alvo);
        return destino;
    }

}
