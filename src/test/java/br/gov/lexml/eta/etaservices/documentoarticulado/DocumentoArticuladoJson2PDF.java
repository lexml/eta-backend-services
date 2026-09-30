package br.gov.lexml.eta.etaservices.documentoarticulado;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;
import br.gov.lexml.eta.etaservices.documentoarticulado.extracao.DocumentoArticuladoJsonExtractor;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;

/**
 * Utilitário manual (não é teste JUnit): gera o PDF de um documento-articulado.json com o jsonix-lexml real,
 * para inspeção visual e validação PDF/A, e grava ao lado o JSON recuperado desse PDF.
 * <p>
 * Argumentos: {@code <executável jsonix-lexml> [json de entrada] [pdf de saída]}. Sem os dois últimos, usa o
 * exemplo de {@code src/test/resources/documentoarticulado} e grava em {@code target/}.
 */
class DocumentoArticuladoJson2PDF {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: DocumentoArticuladoJson2PDF <executável jsonix-lexml> [json de entrada] [pdf de saída]");
            System.exit(1);
        }
        Path executavel = Paths.get(args[0]);
        Path entrada = Paths.get(args.length > 1 ? args[1] : "src/test/resources/documentoarticulado/documento-articulado-exemplo.json");
        Path saida = Paths.get(args.length > 2 ? args[2] : "target/documento-articulado-exemplo.pdf");

        ConversorDocumentoArticulado conversor = new ConversorDocumentoArticuladoCli(executavel, Duration.ofSeconds(60));
        String json = new String(Files.readAllBytes(entrada), StandardCharsets.UTF_8);

        Files.createDirectories(saida.toAbsolutePath().getParent());
        try (OutputStream out = Files.newOutputStream(saida)) {
            new DocumentoArticuladoPdfGenerator(conversor).generate(json, out);
        }

        StringWriter recuperado = new StringWriter();
        try (InputStream in = Files.newInputStream(saida)) {
            new DocumentoArticuladoJsonExtractor(conversor).extractJsonFromPdf(in, recuperado);
        }
        Path jsonRecuperado = Paths.get(saida.toString().replaceAll("\\.pdf$", "") + ".recuperado.json");
        Files.write(jsonRecuperado, recuperado.toString().getBytes(StandardCharsets.UTF_8));

        System.out.println("PDF: " + saida.toAbsolutePath());
        System.out.println("JSON recuperado: " + jsonRecuperado.toAbsolutePath());
    }

}
