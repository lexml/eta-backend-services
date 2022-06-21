package br.gov.lexml.eta.etaservices.printing.pdf;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOURIResolver;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;

/**
 * Faz a transformação do xml/xsl para outro formato usando FOP.
 */
public class TransformacaoFop extends TransformacaoXslt implements URIResolver {
	
	private static final Log log = LogFactory.getLog(TransformacaoFop.class);

    private String outputFormat;

    public TransformacaoFop(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override
    protected void doTransform(Transformer transformer, OutputStream out) throws TransformerException {

        FopFactory fopFactory = FopFactory.newInstance();

        try {
    		// font resolver
    		FOURIResolver uriResolver = (FOURIResolver) fopFactory.getURIResolver();
    		uriResolver.setCustomURIResolver(this);

    		try {
    			URL url = getClass().getResource("/fop.xconf");
    			fopFactory.setUserConfig(url.toString());
    		} catch (SAXException e) {
    			log.error("static FOPProcessor SAXException when loading fop.xconf: "+ e.getMessage(), e);
    		} catch (IOException e) {
    			log.error("static FOPProcessor: IOException when loading fop.xconf: "+ e.getMessage(), e);
    		}
            
            Fop fop = fopFactory.newFop(outputFormat, out);
            
            transformer.transform(new StreamSource(arquivoXml), new SAXResult(fop.getDefaultHandler()));
        }
        catch (FOPException e) {
            throw new TransformerException("Nao foi possivel executar a transformacao", e);
        }

    }
    
	public Source resolve(String href, String base) throws TransformerException {
		
		InputStream inputStream = getClass().getResourceAsStream("/pdfa-fonts/" + href);
		if (inputStream != null) {
			log.info("Font href=" + href + " found.");
			return new StreamSource(inputStream);
		}
		
		inputStream = getClass().getResourceAsStream(href);
		if(inputStream != null) {
			log.info("Resource href=" + href + " found.");
			return new StreamSource(inputStream);
		}
		
		return null;
		
	}

}
