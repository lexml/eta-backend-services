package br.gov.lexml.eta.etaservices.documentoarticulado.conversor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simula o executável jsonix-lexml nos testes: {@code <modo> <tojson|toxml> <entrada> -o <saida>}.
 * <ul>
 * <li>{@code eco}: grava na saída {@code <subcomando>:<conteúdo da entrada>} em UTF-8;</li>
 * <li>{@code erro-silencioso}: como o jsonix-lexml 2.0.0 em erro — nada na saída, erro no stderr, código 0;</li>
 * <li>{@code saida-vazia}: cria a saída vazia e termina com código 0;</li>
 * <li>{@code codigo-erro}: erro no stderr e código 3;</li>
 * <li>{@code dormir}: não termina dentro de um tempo razoável.</li>
 * </ul>
 */
public class JsonixLexmlSimulado {

    public static void main(String[] args) throws Exception {
        String modo = args[0];
        String subcomando = args[1];
        Path entrada = Paths.get(args[2]);
        Path saida = Paths.get(args[4]);

        switch (modo) {
        case "eco":
            String conteudo = new String(Files.readAllBytes(entrada), StandardCharsets.UTF_8);
            Files.write(saida, (subcomando + ":" + conteudo).getBytes(StandardCharsets.UTF_8));
            break;
        case "erro-silencioso":
            System.err.println("(node:5084) UnhandledPromiseRejectionWarning: Error: Element [{http://www.lexml.gov.br/1.0}invalido] is not known in this context, could not determine its type.");
            break;
        case "saida-vazia":
            Files.createFile(saida);
            break;
        case "codigo-erro":
            System.err.println("falha simulada");
            System.exit(3);
            break;
        case "dormir":
            Thread.sleep(120_000);
            break;
        default:
            throw new IllegalArgumentException(modo);
        }
    }

}
