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
 * Utilitário manual (não é teste JUnit): gera arquivos {@code .docx} em {@code target/} para inspeção visual no
 * Microsoft Word e no LibreOffice Writer.
 * <p>
 * Uso:
 * <ul>
 * <li>Sem argumentos: gera as fixtures principais em {@code target/docx-inspecao/} usando o conversor fake (não exige
 * {@code jsonix-lexml}).</li>
 * <li>Com argumentos: {@code <executável jsonix-lexml> [json de entrada] [docx de saída]}.</li>
 * </ul>
 * <p>
 * Checklist de inspeção visual no Word / LibreOffice Writer:
 * <ol>
 * <li>Abertura sem avisos de reparo ou corrupção;</li>
 * <li>Brasão, casa legislativa e texto do cabeçalho apenas na 1ª página;</li>
 * <li>Caixa de pendências em fundo amarelo contínuo com texto vermelho antes da epígrafe;</li>
 * <li>Epígrafe, ementa, preâmbulo, articulação, blocos de alteração com aspas/(NR) e omissis pontilhados;</li>
 * <li>Notas de rodapé no fim da página com numeração automática sem duplicação;</li>
 * <li>Listas simples/aninhadas, tabelas centralizadas (bordas, colspan/rowspan) e imagens PNG/JPEG;</li>
 * <li>Local, data e tabela de assinaturas em 2 colunas.</li>
 * </ol>
 */
public class Json2DOCX {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            DocumentoArticuladoDocxGenerator gerador = new DocumentoArticuladoDocxGenerator(comDocumentosDeTeste());
            Path pasta = Paths.get("target/docx-inspecao");
            Files.createDirectories(pasta);
            gerar(gerador, recurso(JSON_EXEMPLO), pasta.resolve("documento-articulado-exemplo.docx"));
            gerar(gerador, json(JUSTIFICACAO), pasta.resolve("documento-com-justificacao.docx"));
            gerar(gerador, json(ARTICULACAO_E_ALTERACAO), pasta.resolve("documento-com-articulacao-e-alteracao-de-norma.docx"));
            gerar(gerador, json(ANEXO_PARECER), pasta.resolve("documento-anexo-parecer.docx"));
            return;
        }

        Path executavel = Paths.get(args[0]);
        Path entrada = Paths.get(args.length > 1 ? args[1]
                : "src/test/resources/documentoarticulado/documento-articulado-exemplo.json");
        Path saida = Paths.get(args.length > 2 ? args[2] : "target/documento-articulado-exemplo.docx");

        ConversorDocumentoArticulado conversor = new ConversorDocumentoArticuladoCli(executavel, Duration.ofSeconds(60));
        String json = new String(Files.readAllBytes(entrada), StandardCharsets.UTF_8);
        Files.createDirectories(saida.toAbsolutePath().getParent());
        gerar(new DocumentoArticuladoDocxGenerator(conversor), json, saida);
    }

    private static void gerar(DocumentoArticuladoDocxGenerator gerador, String json, Path saida) throws Exception {
        try (OutputStream out = Files.newOutputStream(saida)) {
            gerador.generate(json, out);
        }
        System.out.println("DOCX: " + saida.toAbsolutePath());
    }

}
