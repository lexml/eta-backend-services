package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Gera o fragmento XSL-FO do conteúdo impresso (parte inicial e articulação) aplicando
 * {@code documento-articulado-conteudo.xsl} ao documento-articulado.xml.
 */
class DocumentoArticuladoConteudoTransformer {

    static final String XSLT = "/documentoarticulado/documento-articulado-conteudo.xsl";

    /** XSLT compilada uma única vez; {@link Templates} é thread-safe. */
    private static class Compilada {
        static final Templates INSTANCIA = compilar();
    }

    /**
     * @param xml conteúdo do documento-articulado.xml
     * @param parametros tamanhos de fonte e espaçamentos
     * @return fragmento XSL-FO com um único {@code fo:block} raiz
     * @throws EtaBackendException se o XML não puder ser transformado
     */
    String transformar(String xml, ParametrosImpressaoDocumentoArticulado parametros) {
        try {
            Transformer transformer = Compilada.INSTANCIA.newTransformer();
            transformer.setErrorListener(new ErroComoExcecao());
            transformer.setParameter("tamanhoFonte", parametros.getTamanhoFonte() + "pt");
            transformer.setParameter("maxTamanhoFonte", parametros.getMaxTamanhoFonte());
            transformer.setParameter("lineHeight", parametros.getLineHeight());
            transformer.setParameter("pMarginBottom", parametros.getPMarginBottom());

            StringWriter fo = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(fo));
            return fo.toString();
        } catch (TransformerException e) {
            throw new EtaBackendException("Não foi possível gerar o conteúdo do PDF do documento articulado: " + e.getMessage(), e);
        }
    }

    private static Templates compilar() {
        URL xslt = DocumentoArticuladoConteudoTransformer.class.getResource(XSLT);
        if (xslt == null) {
            throw new EtaBackendException("XSLT não encontrada: " + XSLT);
        }
        try (InputStream in = xslt.openStream()) {
            // Processador XSLT do próprio JDK, independente do que houver no classpath: o Xalan 2.7.2 (dependência
            // transitiva) trata o namespace XSL-FO como XSL e rejeita os atributos dos elementos fo:* em XSLT 1.0.
            TransformerFactory factory = TransformerFactory.newDefaultInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            bloquearAcessoExterno(factory, XMLConstants.ACCESS_EXTERNAL_DTD);
            bloquearAcessoExterno(factory, XMLConstants.ACCESS_EXTERNAL_STYLESHEET);
            factory.setErrorListener(new ErroComoExcecao());
            return factory.newTemplates(new StreamSource(in, xslt.toExternalForm()));
        } catch (IOException | TransformerException e) {
            throw new EtaBackendException("Não foi possível carregar a XSLT " + XSLT + ": " + e.getMessage(), e);
        }
    }

    /** Nem todo processador XSLT reconhece os atributos de acesso externo (JAXP 1.5). */
    private static void bloquearAcessoExterno(TransformerFactory factory, String atributo) {
        try {
            factory.setAttribute(atributo, "");
        } catch (IllegalArgumentException e) {
            // Processador sem suporte ao atributo: vale o FEATURE_SECURE_PROCESSING.
        }
    }

    /** Transforma erros em exceção, sem escrever no stderr (comportamento padrão do processador). */
    private static class ErroComoExcecao implements ErrorListener {

        @Override
        public void warning(TransformerException exception) {
            // Avisos não interrompem a transformação.
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
            throw exception;
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
            throw exception;
        }

    }

}
