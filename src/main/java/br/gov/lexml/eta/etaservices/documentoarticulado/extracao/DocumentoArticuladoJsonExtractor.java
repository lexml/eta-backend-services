package br.gov.lexml.eta.etaservices.documentoarticulado.extracao;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Recupera o documento-articulado.json a partir de um PDF gerado pelo editor de proposições: lê o anexo
 * documento-articulado.xml (em memória, sem arquivos temporários) e o converte para JSON.
 */
public class DocumentoArticuladoJsonExtractor {

    static final String NOME_ANEXO = "documento-articulado.xml";

    private static final String MENSAGEM_ARQUIVO_INVALIDO = "Não se trata de um arquivo gerado pelo editor de proposições.";

    private final ConversorDocumentoArticulado conversor;

    public DocumentoArticuladoJsonExtractor(ConversorDocumentoArticulado conversor) {
        this.conversor = Objects.requireNonNull(conversor, "conversor");
    }

    /**
     * @param pdfStream PDF gerado pelo editor de proposições
     * @param jsonWriter destino do documento-articulado.json
     * @throws EtaBackendException se o arquivo não for um PDF com o anexo documento-articulado.xml ou se a
     *         conversão falhar
     * @throws IOException se houver erro de leitura da entrada ou de escrita no destino
     */
    public void extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws IOException {
        String xml = lerAnexo(IOUtils.toByteArray(pdfStream));
        jsonWriter.write(conversor.xmlParaJson(xml));
        jsonWriter.flush();
    }

    private static String lerAnexo(byte[] pdf) {
        PDEmbeddedFile anexo;
        byte[] conteudo;
        try (PDDocument documento = PDDocument.load(pdf)) {
            anexo = procurar(new PDDocumentNameDictionary(documento.getDocumentCatalog()).getEmbeddedFiles());
            conteudo = anexo == null ? null : anexo.toByteArray();
        } catch (IOException e) {
            // Não é um PDF ou não foi possível ler os anexos.
            throw new EtaBackendException(MENSAGEM_ARQUIVO_INVALIDO, e);
        }
        if (conteudo == null) {
            throw new EtaBackendException(MENSAGEM_ARQUIVO_INVALIDO);
        }
        return new String(conteudo, StandardCharsets.UTF_8);
    }

    /** Procura o anexo na árvore de nomes {@code EmbeddedFiles}, incluindo os nós filhos. */
    private static PDEmbeddedFile procurar(PDNameTreeNode<PDComplexFileSpecification> no) throws IOException {
        if (no == null) {
            return null;
        }
        Map<String, PDComplexFileSpecification> nomes = no.getNames();
        if (nomes != null && nomes.get(NOME_ANEXO) != null) {
            return nomes.get(NOME_ANEXO).getEmbeddedFile();
        }
        List<PDNameTreeNode<PDComplexFileSpecification>> filhos = no.getKids();
        if (filhos != null) {
            for (PDNameTreeNode<PDComplexFileSpecification> filho : filhos) {
                PDEmbeddedFile anexo = procurar(filho);
                if (anexo != null) {
                    return anexo;
                }
            }
        }
        return null;
    }

}
