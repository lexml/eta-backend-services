package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.extracao.DocumentoArticuladoJsonExtractor;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.parser.pl.block.Block;
import scala.collection.immutable.List;

/**
 * Faz o parseamento ou extração de proposições em DOCX, TXT, XML (LexML) ou PDF (com
 * {@code documento-articulado.xml} embutido) e devolve o documento articulado em formato Jsonix.
 */
public class DocumentoArticuladoParser {

    private final ConversorDocumentoArticulado conversor;
    private final DocumentoArticuladoJsonExtractor pdfExtractor;
    private final LeitorArquivoProposicao leitorArquivo;
    private final ParserBlocosProposicao parserBlocos;
    private final ValidadorXmlLexml validadorXml;
    private final OpcoesParseProposicao opcoesPadrao;

    public DocumentoArticuladoParser(ConversorDocumentoArticulado conversor) {
        this(conversor, null);
    }

    /**
     * Cria uma instância com opções padrão personalizadas (por exemplo, listas extras de regex de epígrafe
     * carregadas da configuração da aplicação), ainda sobrescrevíveis por chamada.
     */
    public DocumentoArticuladoParser(ConversorDocumentoArticulado conversor, OpcoesParseProposicao opcoesPadrao) {
        this.conversor = Objects.requireNonNull(conversor, "conversor");
        this.pdfExtractor = new DocumentoArticuladoJsonExtractor(conversor);
        this.leitorArquivo = new LeitorArquivoProposicao();
        this.parserBlocos = new ParserBlocosProposicao();
        this.validadorXml = new ValidadorXmlLexml();
        this.opcoesPadrao = opcoesPadrao != null ? opcoesPadrao : OpcoesParseProposicao.aproveitamento();
    }

    public ResultadoParseProposicao parse(InputStream arquivo, TipoArquivoProposicao tipo) throws IOException {
        return parse(arquivo, tipo, null);
    }

    public ResultadoParseProposicao parse(
            InputStream arquivo, TipoArquivoProposicao tipo, OpcoesParseProposicao opcoes) throws IOException {
        Objects.requireNonNull(arquivo, "arquivo");
        Objects.requireNonNull(tipo, "tipo");
        OpcoesParseProposicao efetivas = opcoesEfetivas(opcoes);
        switch (tipo) {
            case DOCX:
                return parseDocx(arquivo, efetivas);
            case TXT:
                return parseTxt(arquivo, efetivas);
            case XML:
                return parseXml(arquivo, efetivas);
            case PDF:
                return parsePdf(arquivo);
            default:
                throw new EtaBackendException("Tipo de arquivo não suportado: " + tipo);
        }
    }

    public ResultadoParseProposicao parsePorContentType(InputStream arquivo, String contentType) throws IOException {
        return parsePorContentType(arquivo, contentType, null);
    }

    public ResultadoParseProposicao parsePorContentType(
            InputStream arquivo, String contentType, OpcoesParseProposicao opcoes) throws IOException {
        return parse(arquivo, TipoArquivoProposicao.porContentType(contentType), opcoes);
    }

    public ResultadoParseProposicao parseDocx(InputStream docx) throws IOException {
        return parseDocx(docx, null);
    }

    public ResultadoParseProposicao parseDocx(InputStream docx, OpcoesParseProposicao opcoes) throws IOException {
        Objects.requireNonNull(docx, "docx");
        List<Block> blocks = leitorArquivo.lerDocx(IOUtils.toByteArray(docx));
        return finalizar(parserBlocos.parse(blocks, opcoesEfetivas(opcoes)));
    }

    public ResultadoParseProposicao parseTxt(InputStream txt) throws IOException {
        return parseTxt(txt, null);
    }

    public ResultadoParseProposicao parseTxt(InputStream txt, OpcoesParseProposicao opcoes) throws IOException {
        Objects.requireNonNull(txt, "txt");
        List<Block> blocks = leitorArquivo.lerTxt(IOUtils.toByteArray(txt));
        return finalizar(parserBlocos.parse(blocks, opcoesEfetivas(opcoes)));
    }

    public ResultadoParseProposicao parseTxt(String texto) {
        return parseTxt(texto, null);
    }

    public ResultadoParseProposicao parseTxt(String texto, OpcoesParseProposicao opcoes) {
        List<Block> blocks = leitorArquivo.lerTxt(texto);
        return finalizar(parserBlocos.parse(blocks, opcoesEfetivas(opcoes)));
    }

    public ResultadoParseProposicao parseXml(InputStream xml) throws IOException {
        return parseXml(xml, null);
    }

    public ResultadoParseProposicao parseXml(InputStream xml, OpcoesParseProposicao opcoes) throws IOException {
        Objects.requireNonNull(xml, "xml");
        ValidadorXmlLexml.ResultadoXml res = validadorXml.validarEPreparar(
                IOUtils.toByteArray(xml), opcoesEfetivas(opcoes));
        return finalizar(res);
    }

    public ResultadoParseProposicao parseXml(String xml) {
        return parseXml(xml, null);
    }

    public ResultadoParseProposicao parseXml(String xml, OpcoesParseProposicao opcoes) {
        ValidadorXmlLexml.ResultadoXml res = validadorXml.validarEPreparar(xml, opcoesEfetivas(opcoes));
        return finalizar(res);
    }

    public ResultadoParseProposicao parsePdf(InputStream pdf) throws IOException {
        Objects.requireNonNull(pdf, "pdf");
        String xml = pdfExtractor.extractXmlFromPdf(pdf);
        String jsonix = conversor.xmlParaJson(xml);
        return new ResultadoParseProposicao(jsonix, xml, Collections.emptyList());
    }

    public String parseParaJson(InputStream arquivo, TipoArquivoProposicao tipo) throws IOException {
        return parse(arquivo, tipo).getJsonix();
    }

    public String parseParaJson(
            InputStream arquivo, TipoArquivoProposicao tipo, OpcoesParseProposicao opcoes) throws IOException {
        return parse(arquivo, tipo, opcoes).getJsonix();
    }

    public String parsePorContentTypeParaJson(InputStream arquivo, String contentType) throws IOException {
        return parsePorContentType(arquivo, contentType).getJsonix();
    }

    public String parsePorContentTypeParaJson(
            InputStream arquivo, String contentType, OpcoesParseProposicao opcoes) throws IOException {
        return parsePorContentType(arquivo, contentType, opcoes).getJsonix();
    }

    private ResultadoParseProposicao finalizar(ValidadorXmlLexml.ResultadoXml res) {
        String jsonix = conversor.xmlParaJson(res.xml);
        return new ResultadoParseProposicao(jsonix, res.xml, res.avisos);
    }

    private OpcoesParseProposicao opcoesEfetivas(OpcoesParseProposicao opcoes) {
        return opcoes != null ? opcoes : opcoesPadrao;
    }

}

