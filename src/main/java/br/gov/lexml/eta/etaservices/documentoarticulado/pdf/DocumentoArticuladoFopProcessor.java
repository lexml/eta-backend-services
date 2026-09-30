package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.pdf.PDFAMode;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.dom4j.io.DocumentSource;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.pdfa.PDFA;
import br.gov.lexml.pdfa.PDFAttachmentFile;

/**
 * Renderiza o XSL-FO do documento articulado em PDF/A-3B com o documento-articulado.xml embutido.
 * Derivado de {@code printing.pdf.FOPProcessor}, com fábrica FOP e configuração de fontes próprias.
 */
class DocumentoArticuladoFopProcessor {

    static final String NOME_ANEXO = "documento-articulado.xml";

    private static final String CONFIGURACAO_FOP = "/documentoarticulado/fop-documento-articulado.xconf";
    private static final String DIRETORIO_FONTES = "/pdfa-fonts/";
    private static final String[] FONTES = {
            "GenBasB.ttf", "GenBasBI.ttf", "GenBasI.ttf", "GenBasR.ttf", "ITC-StoneSansStd-Medium.ttf" };

    /** Inicialização tardia e única da fábrica FOP (holder idiom). */
    private static class Fabrica {
        static final FopFactory INSTANCIA = criarFopFactory();
    }

    /**
     * @param xslFo XSL-FO com o bloco {@code x:xmpmeta} de PDF/A
     * @param xmlDocumento conteúdo do documento-articulado.xml a embutir
     * @return os bytes do PDF/A
     */
    byte[] gerarPdf(String xslFo, String xmlDocumento) {
        DocumentoArticuladoFoHelper helper = new DocumentoArticuladoFoHelper(xslFo);
        if (!helper.isPdfa() || helper.getXmpmeta() == null) {
            throw new EtaBackendException("O XSL-FO do documento articulado não define os metadados de PDF/A.");
        }

        try {
            byte[] pdfFop = renderizar(helper);

            ByteArrayOutputStream pdfa = new ByteArrayOutputStream();
            PDFA stamper = PDFA.getNewInstance(pdfa, new ByteArrayInputStream(pdfFop), helper.getPdfaPart(), helper.getPdfaConformance());
            if (stamper == null) {
                throw new EtaBackendException("PDF/A parte " + helper.getPdfaPart() + ", conformidade "
                        + helper.getPdfaConformance() + " não suportado.");
            }
            stamper.addXMP(helper.getXmpmeta().getBytes(StandardCharsets.UTF_8));
            stamper.addAttachments(new PDFAttachmentFile(xmlDocumento.getBytes(StandardCharsets.UTF_8), NOME_ANEXO,
                    "text/xml", helper.getCreateDate(), PDFAttachmentFile.AFRelationShip.SOURCE));
            stamper.setVersion(PDFA.PDFVersion.PDF_VERSION_1_7);
            stamper.close();

            return pdfa.toByteArray();
        } catch (EtaBackendException e) {
            throw e;
        } catch (Exception e) {
            throw new EtaBackendException("Erro ao gerar o PDF do documento articulado: " + e.getMessage(), e);
        }
    }

    private static byte[] renderizar(DocumentoArticuladoFoHelper helper) throws Exception {
        FOUserAgent userAgent = Fabrica.INSTANCIA.newFOUserAgent();
        userAgent.getRendererOptions().put("pdf-a-mode", PDFAMode.PDFA_3B.getName());
        userAgent.setAccessibility(true);
        if (helper.getCreateDate() != null) {
            userAgent.setCreationDate(Date.from(ZonedDateTime.parse(helper.getCreateDate()).toInstant()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = Fabrica.INSTANCIA.newFop(MimeConstants.MIME_PDF, userAgent, out);
        Result resultado = new SAXResult(fop.getDefaultHandler());
        Transformer identidade = TransformerFactory.newInstance().newTransformer();
        identidade.transform(new DocumentSource(helper.getFoSemXmpmeta()), resultado);
        return out.toByteArray();
    }

    private static FopFactory criarFopFactory() {
        try (InputStream xconf = DocumentoArticuladoFopProcessor.class.getResourceAsStream(CONFIGURACAO_FOP)) {
            if (xconf == null) {
                throw new EtaBackendException("Configuração do FOP não encontrada: " + CONFIGURACAO_FOP);
            }
            FopConfParser parser = new FopConfParser(xconf, new URI("file://./"), new ResolverFontes(carregarFontes()));
            return parser.getFopFactoryBuilder().build();
        } catch (EtaBackendException e) {
            throw e;
        } catch (Exception e) {
            throw new EtaBackendException("Não foi possível configurar o FOP do documento articulado.", e);
        }
    }

    private static Map<String, byte[]> carregarFontes() throws IOException {
        Map<String, byte[]> fontes = new HashMap<>();
        for (String fonte : FONTES) {
            try (InputStream in = DocumentoArticuladoFopProcessor.class.getResourceAsStream(DIRETORIO_FONTES + fonte)) {
                if (in == null) {
                    throw new EtaBackendException("Fonte não encontrada: " + DIRETORIO_FONTES + fonte);
                }
                fontes.put(fonte, IOUtils.toByteArray(in));
            }
        }
        return fontes;
    }

    /** Serve as fontes carregadas do classpath; demais recursos seguem o resolver padrão do FOP. */
    private static class ResolverFontes implements ResourceResolver {

        private final ResourceResolver padrao = ResourceResolverFactory.createDefaultResourceResolver();
        private final Map<String, byte[]> fontes;

        ResolverFontes(Map<String, byte[]> fontes) {
            this.fontes = fontes;
        }

        @Override
        public OutputStream getOutputStream(URI uri) throws IOException {
            return padrao.getOutputStream(uri);
        }

        @Override
        public Resource getResource(URI uri) throws IOException {
            byte[] fonte = fontes.get(uri.toString().replaceAll("^file://\\./", ""));
            if (fonte != null) {
                return new Resource(MimeConstants.MIME_AFP_TRUETYPE, new ByteArrayInputStream(fonte));
            }
            return padrao.getResource(uri);
        }

    }

}
