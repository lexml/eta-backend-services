package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.eta.etaservices.util.XMLUtil;
import br.gov.lexml.parser.pl.block.Block;
import br.gov.lexml.parser.pl.block.Block$;
import br.gov.lexml.parser.pl.docx.DOCXReader$;
import br.gov.lexml.parser.pl.xhtml.XHTMLProcessor$;
import scala.Option;
import scala.collection.immutable.List;
import scala.xml.Elem;
import scala.xml.Node;

/**
 * Converte arquivos DOCX e texto puro (TXT) para a lista de {@link Block} da biblioteca
 * {@code lexml-parser-projeto-lei}, preservando a ordem exata dos parágrafos.
 */
final class LeitorArquivoProposicao {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    List<Block> lerDocx(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new EtaBackendException("O arquivo DOCX está vazio.");
        }
        Option<Elem> xhtml;
        try {
            xhtml = DOCXReader$.MODULE$.readDOCX(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new EtaBackendException(
                    "Não foi possível ler o arquivo DOCX: o arquivo não é um documento Word (.docx) válido.", e);
        }
        if (xhtml == null || xhtml.isEmpty()) {
            throw new EtaBackendException(
                    "Não foi possível ler o arquivo DOCX: o arquivo não é um documento Word (.docx) válido.");
        }
        byte[] xhtmlBytes = xhtml.get().toString().getBytes(StandardCharsets.UTF_8);
        return processarXhtml(xhtmlBytes, "DOCX");
    }

    List<Block> lerTxt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new EtaBackendException("O arquivo TXT está vazio.");
        }
        return lerTxt(decodificarTexto(bytes));
    }

    List<Block> lerTxt(String texto) {
        if (texto == null) {
            throw new EtaBackendException("O texto informado está vazio.");
        }
        String semBom = texto.startsWith("\uFEFF") ? texto.substring(1) : texto;
        if (semBom.trim().isEmpty()) {
            throw new EtaBackendException("O texto informado está vazio.");
        }
        // Não delega para o handler "text/plain" do XHTMLProcessor porque ele inverte linhas consecutivas sem linha em branco.
        String[] linhas = semBom.split("\\r?\\n|\\r", -1);
        StringBuilder html = new StringBuilder("<html><body><div>");
        for (String linha : linhas) {
            html.append("<p>").append(escaparXml(XMLUtil.sanitize(linha))).append("</p>");
        }
        html.append("</div></body></html>");
        return processarXhtml(html.toString().getBytes(StandardCharsets.UTF_8), "TXT");
    }

    private static List<Block> processarXhtml(byte[] xhtmlBytes, String tipo) {
        Option<List<Node>> nodes;
        try {
            nodes = XHTMLProcessor$.MODULE$.pipelineWithDefaultConverter(xhtmlBytes, "application/xhtml+xml");
        } catch (Exception e) {
            throw new EtaBackendException("Não foi possível processar o conteúdo do arquivo " + tipo + ".", e);
        }
        if (nodes == null || nodes.isEmpty()) {
            throw new EtaBackendException("Não foi possível processar o conteúdo do arquivo " + tipo + ".");
        }
        return Block$.MODULE$.fromNodes(nodes.get());
    }

    private static String decodificarTexto(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, WINDOWS_1252);
        }
    }

    private static String escaparXml(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

}

