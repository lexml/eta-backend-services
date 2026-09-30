package br.gov.lexml.eta.etaservices.documentoarticulado.conversor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Conversor que executa o jsonix-lexml (versão 2.0.0 ou superior,
 * <a href="https://github.com/lexml/jsonix-lexml">github.com/lexml/jsonix-lexml</a>) por linha de comando.
 * <p>
 * O executável encerra com código 0 mesmo quando a conversão falha, informando o erro apenas no stderr.
 * Por isso a ausência de saída também é tratada como falha. Cada conversão usa um diretório temporário
 * próprio, removido ao final.
 */
public class ConversorDocumentoArticuladoCli implements ConversorDocumentoArticulado {

    private static final Logger log = LoggerFactory.getLogger(ConversorDocumentoArticuladoCli.class);

    private static final int TAMANHO_MAXIMO_MENSAGEM_ERRO = 2000;
    private static final int TENTATIVAS_REMOCAO = 20;
    private static final long INTERVALO_REMOCAO_MS = 100;

    private final List<String> comando;
    private final Duration timeout;
    private final Path diretorioBase;

    /**
     * @param executavel caminho do executável jsonix-lexml da plataforma
     * @param timeout tempo máximo de cada conversão
     */
    public ConversorDocumentoArticuladoCli(Path executavel, Duration timeout) {
        this(Collections.singletonList(Objects.requireNonNull(executavel, "executavel").toString()), timeout, null);
    }

    /**
     * Permite informar um comando com argumentos iniciais (ex.: {@code java -cp ... Classe}) e o diretório
     * onde os diretórios temporários são criados ({@code null} usa o diretório temporário do sistema).
     */
    ConversorDocumentoArticuladoCli(List<String> comando, Duration timeout, Path diretorioBase) {
        if (comando == null || comando.isEmpty()) {
            throw new IllegalArgumentException("O comando do conversor deve ser informado.");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("O tempo limite do conversor deve ser positivo.");
        }
        this.comando = Collections.unmodifiableList(new ArrayList<>(comando));
        this.timeout = timeout;
        this.diretorioBase = diretorioBase;
    }

    @Override
    public String jsonParaXml(String json) {
        return converter(json, "toxml", "documento-articulado.json");
    }

    @Override
    public String xmlParaJson(String xml) {
        return converter(xml, "tojson", "documento-articulado.xml");
    }

    private String converter(String conteudo, String subcomando, String nomeEntrada) {
        Objects.requireNonNull(conteudo, "conteudo");
        Path diretorio = null;
        try {
            diretorio = diretorioBase == null
                    ? Files.createTempDirectory("documento-articulado-")
                    : Files.createTempDirectory(diretorioBase, "documento-articulado-");
            Path entrada = Files.write(diretorio.resolve(nomeEntrada), conteudo.getBytes(StandardCharsets.UTF_8));
            Path saida = diretorio.resolve("saida");
            Path stderr = diretorio.resolve("stderr.txt");

            List<String> argumentos = new ArrayList<>(comando);
            argumentos.add(subcomando);
            argumentos.add(entrada.toString());
            argumentos.add("-o");
            argumentos.add(saida.toString());

            Process processo = iniciar(argumentos, diretorio.resolve("stdout.txt"), stderr);

            if (!processo.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                processo.destroyForcibly();
                // Aguarda o encerramento para liberar os arquivos antes da remoção do diretório.
                processo.waitFor(10, TimeUnit.SECONDS);
                throw new EtaBackendException(String.format(
                        "A conversão pelo jsonix-lexml (%s) excedeu o tempo limite de %d ms.", subcomando, timeout.toMillis()));
            }

            if (processo.exitValue() != 0) {
                throw new EtaBackendException(String.format("A conversão pelo jsonix-lexml (%s) terminou com código %d: %s",
                        subcomando, processo.exitValue(), lerErro(stderr)));
            }

            if (!Files.isRegularFile(saida) || Files.size(saida) == 0) {
                throw new EtaBackendException(String.format("A conversão pelo jsonix-lexml (%s) não produziu resultado: %s",
                        subcomando, lerErro(stderr)));
            }

            return new String(Files.readAllBytes(saida), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new EtaBackendException("Erro na conversão pelo jsonix-lexml: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EtaBackendException("A conversão pelo jsonix-lexml foi interrompida.", e);
        } finally {
            if (diretorio != null) {
                remover(diretorio);
            }
        }
    }

    /**
     * No Windows, os arquivos de um processo encerrado à força podem continuar bloqueados por alguns
     * instantes; por isso a remoção é repetida por um curto período.
     */
    private static void remover(Path diretorio) {
        for (int tentativa = 1; tentativa <= TENTATIVAS_REMOCAO; tentativa++) {
            try {
                FileUtils.deleteDirectory(diretorio.toFile());
                return;
            } catch (IOException e) {
                if (tentativa == TENTATIVAS_REMOCAO) {
                    log.warn("Não foi possível remover o diretório temporário {}", diretorio, e);
                    return;
                }
                try {
                    Thread.sleep(INTERVALO_REMOCAO_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private Process iniciar(List<String> argumentos, Path stdout, Path stderr) {
        try {
            return new ProcessBuilder(argumentos)
                    .redirectOutput(stdout.toFile())
                    .redirectError(stderr.toFile())
                    .start();
        } catch (IOException e) {
            throw new EtaBackendException("Não foi possível executar o jsonix-lexml: " + comando.get(0), e);
        }
    }

    private static String lerErro(Path stderr) throws IOException {
        if (!Files.isRegularFile(stderr)) {
            return "(sem mensagem de erro)";
        }
        String erro = new String(Files.readAllBytes(stderr), StandardCharsets.UTF_8).trim();
        if (erro.isEmpty()) {
            return "(sem mensagem de erro)";
        }
        return erro.length() > TAMANHO_MAXIMO_MENSAGEM_ERRO ? erro.substring(0, TAMANHO_MAXIMO_MENSAGEM_ERRO) + "..." : erro;
    }

}
