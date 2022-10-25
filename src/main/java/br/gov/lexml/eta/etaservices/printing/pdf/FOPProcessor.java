package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.pdf.PDFAMode;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.dom4j.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import br.gov.lexml.pdfa.PDFA;
import br.gov.lexml.pdfa.PDFAttachmentFile;

public class FOPProcessor {

	private static final Logger log = LoggerFactory.getLogger(FOPProcessor.class);

	private static FopFactory fopFactory;

	static {
		try {
			InputStream xconf = FOPProcessor.class.getResourceAsStream("/fop.xconf");
			ResourceResolver resolver = new ClasspathUriResolver();
			FopConfParser parser = new FopConfParser(xconf, new URI("file://."), resolver);
			FopFactoryBuilder builder = parser.getFopFactoryBuilder();
			fopFactory = builder.build();
		} catch (Exception e) {
			log.error("Não foi possível configurar o FOP.", e);
		}
	}

	static class ClasspathUriResolver implements ResourceResolver {

		@Override
		public OutputStream getOutputStream(URI arg0) {
			return null;
		}

		@Override
		public Resource getResource(URI uri) {

			String strUri = uri.toString().replaceAll("^file://\\.", "");

			try {				
				ClassPathResource resource = new ClassPathResource("pdfa-fonts/" + strUri);
				InputStream inputStream = resource.getInputStream();
				if (inputStream != null) {
					return new Resource(MimeConstants.MIME_AFP_TRUETYPE, inputStream);
				}
			}
			catch(Exception e) {
				log.error("Arquivo de fonte /pdfa-fonts/" + strUri + " não encontrado.");
			}
//			InputStream inputStream = FOPProcessor.class.getClassLoader()
//					.getResourceAsStream("/pdfa-fonts/" + strUri);
			return null;
		}

	}

	/**
	 * Transforma XSL-FO em PDF com emenda.xml embutido
	 */
	@SuppressWarnings("unchecked")
	public void processFOP(OutputStream outputStream, String xslFo, String emendaXML) {

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

			//PDF/A:
			PDFA pdfa = PDFA.getNewInstance(outputStream, new ByteArrayInputStream(data), helper.getPDFAPart(), helper.getPDFAConformance());
			if (pdfa == null) {
				log.error("Could not find a PDF/A part " + helper.getPDFAPart() + ", conformance " + helper.getPDFAConformance() + " constructor on PDFA class.");
			} else {
				pdfa.addXMP(helper.getXmpmeta().getBytes());

				//adding emenda.xml
				pdfa.addAttachments(
						new PDFAttachmentFile(
								emendaXML.getBytes(),
								"emenda.xml",
								"text/xml",
								helper.getCmpCreateDate(),
								PDFAttachmentFile.AFRelationShip.SOURCE));

				//setting version
				pdfa.setVersion(PDFA.PDFVersion.PDF_VERSION_1_7);
				pdfa.close();
			}

		} catch (Exception e) {
			throw new RuntimeException("Error processing FOP. " + e.getMessage(), e);
		}
	}

}
