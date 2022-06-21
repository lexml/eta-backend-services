package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe abstrata para transformação de xml/xsl.
 * <p>
 * <a href="TransformacaoXslt.java.html"> <i>Ver código-fonte </i> </a>
 * </p>
 *
 * @version $Revision$ de $Date$
 * @author $Author$
 */
public class TransformacaoXslt {

    private static final Log logger = LogFactory.getLog(TransformacaoXslt.class);

    protected Reader arquivoXml;
    protected Reader arquivoXsl;

    private String charsetName = "UTF-8";

    public TransformacaoXslt() {
        super();
    }

    public TransformacaoXslt(String charsetName) {
        super();
        setCharSetName(charsetName);
    }

    public void setCharSetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getCharSetName() {
        return charsetName;
    }

    public void setXml(String xml) throws UnsupportedEncodingException {
        this.arquivoXml = getReader(xml);
    }

    public void setXml(File arquivoXml) throws FileNotFoundException {
        this.arquivoXml = getReader(arquivoXml);
    }

    public void setXml(InputStream arquivoXml) throws UnsupportedEncodingException {
        this.arquivoXml = getReader(arquivoXml);
    }

    public void setXsl(String xsl) throws UnsupportedEncodingException {
        this.arquivoXsl = getReader(xsl);
    }

    public void setXsl(File arquivoXsl) throws FileNotFoundException {
        this.arquivoXsl = getReader(arquivoXsl);
    }

    public void setXsl(InputStream arquivoXsl) throws UnsupportedEncodingException {
        this.arquivoXsl = getReader(arquivoXsl);
    }

    /**
     * Faz a transformação e retorna um ByteArrayOutputStream com o resultado.
     */
    public ByteArrayOutputStream transforma(Properties parametros) throws TransformerException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        transforma(parametros, saida);

        return saida;
    }

    /**
     * Faz a transformação e grava o resultado no arquivo indicado no parâmetro nomeArquivo.
     */
    public void transforma(Properties parametros, String nomeArquivo) throws TransformerException, IOException {
        transforma(parametros, new File(nomeArquivo));
    }

    /**
     * Faz a transformação e grava o resultado no arquivo indicado no parâmetro arquivo.
     */
    public void transforma(Properties parametros, File arquivo) throws TransformerException, IOException {
        OutputStream saidaArquivo = new FileOutputStream(arquivo);
        transforma(parametros, saidaArquivo);
        saidaArquivo.close();
    }

    public void transforma(Properties parametros, OutputStream out) throws TransformerException {
        TransformerFactory factory = new TransformerFactoryImpl();
        Transformer transformer = factory.newTransformer(new StreamSource(arquivoXsl));

        if (parametros != null) {
            Iterator<Object> i = parametros.keySet().iterator();

            while (i.hasNext()) {
                String nome = (String) i.next();
                String value = parametros.getProperty(nome);
                transformer.setParameter(nome, value);
            }
        }

        doTransform(transformer, out);
    }

    protected void doTransform(Transformer transformer, OutputStream out) throws TransformerException {
        transformer.transform(new StreamSource(arquivoXml), new StreamResult(out));
    }

    private Reader getReader(String str) throws UnsupportedEncodingException {
        return new BufferedReader(new StringReader(str));
    }

    private Reader getReader(File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    private Reader getReader(InputStream is) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(is, charsetName));
    }

}
