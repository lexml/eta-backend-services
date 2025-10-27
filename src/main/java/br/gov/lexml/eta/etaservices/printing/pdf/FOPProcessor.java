package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.pdf.PDFAMode;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.dom4j.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.lexml.pdfa.PDFA;
import br.gov.lexml.pdfa.PDFAttachmentFile;

public class FOPProcessor {

	private static final Logger log = LoggerFactory.getLogger(FOPProcessor.class);

	private static FopFactory fopFactory;
	
	private static Map<String, byte[]> fontes = new HashMap<>();

	static {
		try {
			carregaFonte("GenBasB.ttf");
			carregaFonte("GenBasBI.ttf");
			carregaFonte("GenBasI.ttf");
			carregaFonte("GenBasR.ttf");
			InputStream xconf = FOPProcessor.class.getResourceAsStream("/fop.xconf");
			ResourceResolver resolver = new UriResolver();
			FopConfParser parser = new FopConfParser(xconf, new URI("file://./"), resolver);
			FopFactoryBuilder builder = parser.getFopFactoryBuilder();
			fopFactory = builder.build();
		} catch (Exception e) {
			log.error("Não foi possível configurar o FOP.", e);
		}
	}
	
	private static void carregaFonte(String fonte) throws FileNotFoundException, IOException {
		InputStream is = FOPProcessor.class.getResourceAsStream("/pdfa-fonts/" + fonte);
		fontes.put(fonte, IOUtils.toByteArray(is));
	}

	static class UriResolver implements ResourceResolver {
		
		private static ResourceResolver defaultResolver = ResourceResolverFactory.createDefaultResourceResolver(); 

		@Override
		public OutputStream getOutputStream(URI uri) throws IOException {
			return defaultResolver.getOutputStream(uri);
		}

		@Override
		public Resource getResource(URI uri) throws IOException {

			String strUri = uri.toString().replaceAll("^file://\\./", "");
			
			byte[] fonte = fontes.get(strUri);
			
			if(fonte != null) {
				return new Resource(MimeConstants.MIME_AFP_TRUETYPE, new ByteArrayInputStream(fonte));
			}

			return defaultResolver.getResource(uri);
		}

	}
	
    public void processFOP(OutputStream outputStream, String xslFo, String objetoEmendaXMLOrPareceJSON, TipoDocumento tipoDocumento) {
        this.processFOP(outputStream, xslFo, objetoEmendaXMLOrPareceJSON, new ArrayList<>(), tipoDocumento);
	}
	
	/**
	 * Transforma XSL-FO em PDF com emenda.xml ou parecer.json embutido
	 * @param anexos 
	 */
	@SuppressWarnings("unchecked")
    public void processFOP(OutputStream outputStream, String xslFo, String objetoEmendaXMLOrPareceJSON, List<ByteArrayInputStream> anexos, TipoDocumento tipoDocumento) {
		
//		System.out.println(xslFo);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			// configure foUserAgent as desired
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

			// helper
			FOHelper helper = new FOHelper(xslFo);

			//fonte de dados
			Source src = new DocumentSource(helper.getFOPDocumentWithoutXmpmeta());

			// PDF-A additional information
			if (helper.isPDFAMode()) {

				// TODO Verificar possibilidade de gerar PDFA_3U
				foUserAgent.getRendererOptions().put("pdf-a-mode", PDFAMode.PDFA_3B.getName());
				foUserAgent.setAccessibility(true);

				// set createDate
				String cmpCreateDate = helper.getCmpCreateDate();
				if (cmpCreateDate == null) {
					log.info("cmpCreateDate is null");
				} else {
					Date date = Date.from(ZonedDateTime.parse(cmpCreateDate).toInstant());
					foUserAgent.setCreationDate(date);
				}
			}

			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

			// Resulting SAX events (the generated FO) must be piped
			// through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			// Setup JAXP using identity transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer(); // identity transformer
			transformer.transform(src, res);

			// putting XMP-meta
			byte[] data = out.toByteArray();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
			
			//PDF/A:
			boolean temAnexos = !anexos.isEmpty();
			OutputStream outPdfa = temAnexos? new ByteArrayOutputStream(): outputStream;
			PDFA pdfa = PDFA.getNewInstance(outPdfa, inputStream, helper.getPDFAPart(), helper.getPDFAConformance());
			if (pdfa == null) {
				log.error("Could not find a PDF/A part " + helper.getPDFAPart() + ", conformance " + helper.getPDFAConformance() + " constructor on PDFA class.");
			} else {
				pdfa.addXMP(helper.getXmpmeta().getBytes());

                if (TipoDocumento.EMENDA.equals(tipoDocumento)) {
                    // adding emenda.xml
                    pdfa.addAttachments(new PDFAttachmentFile(objetoEmendaXMLOrPareceJSON.getBytes(), "emenda.xml", "text/xml", helper.getCmpCreateDate(), PDFAttachmentFile.AFRelationShip.SOURCE));
                }
                if (TipoDocumento.PARECER.equals(tipoDocumento)) {
                    // adding emenda.xml
                    pdfa.addAttachments(new PDFAttachmentFile(objetoEmendaXMLOrPareceJSON.getBytes(StandardCharsets.UTF_8), "parecer.json", "application/json", helper.getCmpCreateDate(),
                            PDFAttachmentFile.AFRelationShip.SOURCE));
                }

				//setting version
				pdfa.setVersion(PDFA.PDFVersion.PDF_VERSION_1_7);
				pdfa.close();
				
				if(temAnexos) {					
					PDFMergerUtility merger = new PDFMergerUtility();
					
					merger.addSource(new ByteArrayInputStream(((ByteArrayOutputStream)outPdfa).toByteArray()));
					
					anexos.forEach(anexo -> merger.addSource(anexo));
					
					merger.setDestinationStream(outputStream);
					
					merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
				}
			    
			}

		} catch (Exception e) {
			throw new RuntimeException("Error processing FOP. " + e.getMessage(), e);
		}
		
		
	}

}
