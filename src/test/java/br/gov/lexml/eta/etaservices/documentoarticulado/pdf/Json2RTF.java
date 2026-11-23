package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ANEXO_PARECER;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.ARTICULACAO_E_ALTERACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JSON_EXEMPLO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.JUSTIFICACAO;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.comDocumentosDeTeste;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.json;
import static br.gov.lexml.eta.etaservices.documentoarticulado.ConversorDocumentoArticuladoFake.recurso;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;

/**
 * Utilitário manual (não é teste JUnit): gera arquivos {@code .rtf} em {@code target/} para inspeção visual no
 * Microsoft Word e no LibreOffice Writer.
 * <p>
 * Uso:
 * <ul>
 * <li>Sem argumentos: gera as fixtures principais em {@code target/rtf-inspecao/} usando o conversor fake (não exige
 * {@code jsonix-lexml}).</li>
 * <li>Com argumentos: {@code <executável jsonix-lexml> [json de entrada] [rtf de saída]}.</li>
 * </ul>
 */
public class Json2RTF {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            DocumentoArticuladoRtfGenerator gerador = new DocumentoArticuladoRtfGenerator(comDocumentosDeTeste());
            Path pasta = Paths.get("target/rtf-inspecao");
            Files.createDirectories(pasta);
            gerar(gerador, recurso(JSON_EXEMPLO), pasta.resolve("documento-articulado-exemplo.rtf"));
            gerar(gerador, json(JUSTIFICACAO), pasta.resolve("documento-com-justificacao.rtf"));
            gerar(gerador, json(ARTICULACAO_E_ALTERACAO), pasta.resolve("documento-com-articulacao-e-alteracao-de-norma.rtf"));
            gerar(gerador, json(ANEXO_PARECER), pasta.resolve("documento-anexo-parecer.rtf"));
            return;
        }

        Path executavel = Paths.get(args[0]);
        Path entrada = Paths.get(args.length > 1 ? args[1]
                : "src/test/resources/documentoarticulado/documento-articulado-exemplo.json");
        Path saida = Paths.get(args.length > 2 ? args[2] : "target/documento-articulado-exemplo.rtf");

        ConversorDocumentoArticulado conversor = new ConversorDocumentoArticuladoCli(executavel, Duration.ofSeconds(60));
        String json = new String(Files.readAllBytes(entrada), StandardCharsets.UTF_8);
        Files.createDirectories(saida.toAbsolutePath().getParent());
        gerar(new DocumentoArticuladoRtfGenerator(conversor), json, saida);
    }

    private static void gerar(DocumentoArticuladoRtfGenerator gerador, String json, Path saida) throws Exception {
        try (OutputStream out = Files.newOutputStream(saida)) {
            gerador.generate(json, out);
        }
        System.out.println("RTF: " + saida.toAbsolutePath());
    }

}
