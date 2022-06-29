package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import br.gov.lexml.pdfa.PDFA;
import br.gov.lexml.pdfa.PDFAttachmentFile;

public class FOPProcessor {

	private static final Log log = LogFactory.getLog(FOPProcessor.class);

	private static FopFactory fopFactory;

	static {
		try {
			InputStream xconf = FOPProcessor.class.getResourceAsStream("/fop.xconf");
			ResourceResolver resolver = new ClasspathUriResolver();
			FopConfParser parser = new FopConfParser(xconf, new URI("file://."), resolver);
			FopFactoryBuilder builder = parser.getFopFactoryBuilder();
			fopFactory = builder.build();
		} catch (Exception e) {
			log.fatal("Não foi possível configurar o FOP.", e);
		}
	}

	static class ClasspathUriResolver implements ResourceResolver {

		@Override
		public OutputStream getOutputStream(URI arg0) throws IOException {
			return null;
		}

		@Override
		public Resource getResource(URI uri) throws IOException {

			String strUri = uri.toString().replaceAll("^file://\\.", "");

			InputStream inputStream = FOPProcessor.class
					.getResourceAsStream("/pdfa-fonts/" + strUri);
			if (inputStream != null) {
				return new Resource(MimeConstants.MIME_AFP_TRUETYPE, inputStream);
			}
			return null;
		}

	}

	/**
	 * Transforma xslfo em PDF com emenda.xml embutido
	 */
	@SuppressWarnings("unchecked")
	public void processFOP(OutputStream outputStream, String xslfo, String emendaXML) {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {

				// configure foUserAgent as desired
				FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

				// helper
				FOHelper helper = new FOHelper(xslfo);

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

				// putting XMPmeta
				byte[] data = out.toByteArray();

				//PDF/A:
				PDFA pdfa = PDFA.getNewInstance(outputStream, new ByteArrayInputStream(data), helper.getPDFAPart(), helper.getPDFAConformance());
				if (pdfa == null){
					log.error("Could not find a PDF/A part "+helper.getPDFAPart()+", conformance "+helper.getPDFAConformance()+" constructor on PDFA class.");
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
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("I/O error when processing FOP. " + e, e);
		}
	}

}
