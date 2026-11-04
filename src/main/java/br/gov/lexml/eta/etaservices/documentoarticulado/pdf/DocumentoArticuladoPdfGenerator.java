package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFoGerador.FoGerado;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFopProcessor.AnexoLido;
import br.gov.lexml.eta.etaservices.parecer.AnexoPDFA;
import br.gov.lexml.eta.etaservices.util.BytesUtil;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Gera o PDF/A-3B de uma proposição a partir do documento-articulado.json, embutindo o
 * documento-articulado.xml (LexML) como anexo. O texto impresso é gerado a partir desse XML pela XSLT
 * do conteúdo: parte inicial (epígrafe, ementa e preâmbulo) e articulação. Justificação, local e data e
 * assinaturas ainda não são impressos.
 */
public class DocumentoArticuladoPdfGenerator {

    private static final byte[] HASH_PLACEHOLDER =
            "<check:hash>00000000000000000000000000000000".getBytes(StandardCharsets.UTF_8);
    private static final int TAMANHO_TAG_HASH = "<check:hash>".getBytes(StandardCharsets.UTF_8).length;

    private final DocumentoArticuladoFoGerador foGerador;
    private final DocumentoArticuladoFopProcessor fopProcessor = new DocumentoArticuladoFopProcessor();

    public DocumentoArticuladoPdfGenerator(ConversorDocumentoArticulado conversor) {
        this.foGerador = new DocumentoArticuladoFoGerador(conversor);
    }

    /**
     * @param json conteúdo do documento-articulado.json
     * @param outputStream destino do PDF; nada é escrito se a geração falhar
     * @throws EtaBackendException se o JSON não for um documento LexML ou se a conversão/geração falhar
     * @throws IOException se houver erro ao escrever no destino
     */
    public void generate(String json, OutputStream outputStream) throws IOException {
        generate(json, null, outputStream);
    }

    /**
     * Como {@link #generate(String, OutputStream)}, com anexos: todos são embutidos no PDF/A e os com
     * {@code imprimir} têm as páginas concatenadas ao final. Itens nulos ou sem conteúdo são ignorados.
     *
     * @param anexos pode ser nulo ou vazio; os streams são lidos por inteiro uma única vez
     * @throws EtaBackendException também se um anexo se chamar {@code documento-articulado.xml} ou se um anexo
     *         a imprimir não for um PDF válido
     */
    public void generate(String json, List<AnexoPDFA> anexos, OutputStream outputStream) throws IOException {
        JsonNode documento = foGerador.validar(json);
        List<AnexoLido> anexosLidos = lerAnexos(anexos);
        FoGerado fo = foGerador.gerar(json, documento);
        byte[] pdf = fopProcessor.gerarPdf(fo.xslFo, fo.xml, anexosLidos);
        inserirHash(pdf);

        outputStream.write(pdf);
        outputStream.flush();
    }

    private static List<AnexoLido> lerAnexos(List<AnexoPDFA> anexos) throws IOException {
        List<AnexoLido> lidos = new ArrayList<>();
        if (anexos == null) {
            return lidos;
        }
        for (AnexoPDFA anexo : anexos) {
            if (anexo == null || anexo.getFile() == null) {
                continue;
            }
            String nome = anexo.getNomeArquivo();
            if (nome != null && DocumentoArticuladoFopProcessor.NOME_ANEXO.equalsIgnoreCase(nome.trim())) {
                throw new EtaBackendException("O nome de anexo \"" + DocumentoArticuladoFopProcessor.NOME_ANEXO
                        + "\" é reservado ao modelo do documento articulado.");
            }
            lidos.add(new AnexoLido(nome, IOUtils.toByteArray(anexo.getFile()), anexo.isImprimir()));
        }
        return lidos;
    }

    /** Substitui o placeholder do {@code check:hash} no XMP pelo MD5 do PDF (derivado de PdfGeneratorBean). */
    private static void inserirHash(byte[] pdf) {
        int i = BytesUtil.lastIndexOf(pdf, HASH_PLACEHOLDER);
        if (i >= 0) {
            byte[] md5 = md5Hex(pdf).getBytes(StandardCharsets.UTF_8);
            System.arraycopy(md5, 0, pdf, i + TAMANHO_TAG_HASH, md5.length);
        }
    }

    private static String md5Hex(byte[] bytes) {
        try {
            StringBuilder hex = new StringBuilder();
            for (byte b : MessageDigest.getInstance("MD5").digest(bytes)) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new EtaBackendException("MD5 indisponível.", e);
        }
    }

}
