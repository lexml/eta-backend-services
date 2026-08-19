package br.gov.lexml.eta.etaservices.printing.pdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;

class VelocityTemplateProcessorTest {

    // public static final String SOURCE_FILE_NAME = "emenda_mpv_905_2019_completa_disp_mpv.json";
	public static final String SOURCE_FILE_NAME = "test1.json";
    public static final String DESTINATION_FILE_NAME = "test1.pdf";

    public static final String XML_FILE = "emenda_mpv_905_2019_completa_disp_mpv.xml";
    private Emenda emenda;
    private String xml;

    private VelocityTemplateProcessor velocityTemplateProcessor;

    @Test
    @DisplayName("Não imprime justificação, local, data ou autoria quando a emenda é anexo de parecer")
    void naoImprimeDadosSuprimidosNoAnexoParecer() throws Exception {
        EmendaPojo emendaPojo = (EmendaPojo) emenda;
        emendaPojo.setAnexoParecer(true);
        emendaPojo.setJustificativa("<p>JUSTIFICATIVA_NAO_DEVE_APARECER</p>");
        emendaPojo.setLocal("LOCAL_NAO_DEVE_APARECER");

        String templateResult = velocityTemplateProcessor.getTemplateResult(emendaPojo);

        assertFalse(templateResult.contains("JUSTIFICATIVA_NAO_DEVE_APARECER"));
        assertFalse(templateResult.contains("LOCAL_NAO_DEVE_APARECER"));
        assertFalse(templateResult.contains("role=\"Justificativa\""));
        assertFalse(templateResult.contains("role=\"fecho\""));
        assertFalse(templateResult.contains("role=\"Signatários\""));
        assertFalse(templateResult.contains("Alessandro Vieira"));

        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        new FOPProcessor().processFOP(pdfOutput, templateResult, convertToXml(emendaPojo), TipoDocumento.EMENDA);

        try (PDDocument pdf = PDDocument.load(new ByteArrayInputStream(pdfOutput.toByteArray()))) {
            String textoPdf = new PDFTextStripper().getText(pdf);
            assertFalse(textoPdf.contains("JUSTIFICATIVA_NAO_DEVE_APARECER"));
            assertFalse(textoPdf.contains("LOCAL_NAO_DEVE_APARECER"));
            assertFalse(textoPdf.contains("Alessandro Vieira"));
        }
    }

    @Test
    @DisplayName("Mantém justificação, local, data e autoria na impressão comum")
    void mantemDadosNaEmendaComum() throws IOException {
        EmendaPojo emendaPojo = (EmendaPojo) emenda;
        emendaPojo.setAnexoParecer(false);

        String templateResult = velocityTemplateProcessor.getTemplateResult(emendaPojo);

        assertTrue(templateResult.contains("role=\"Justificativa\""));
        assertTrue(templateResult.contains("role=\"fecho\""));
        assertTrue(templateResult.contains("role=\"Signatários\""));
        assertTrue(templateResult.contains("Alessandro Vieira"));
    }

    
    @DisplayName("Verifica se nome da aplicação é preenchido")
    void testaMetadadosEmenda() throws IOException, URISyntaxException {
        final String templateResult =
                velocityTemplateProcessor.getTemplateResult(emenda);
        savePdf(templateResult);

        final Source result = Input.fromString(templateResult).build();

        assertThat(result)
                .withNamespaceContext(getXSLFoNamespaceContext())
                .valueByXPath("/fo:root/fo:declarations/x:xmpmeta/rdf:RDF/rdf:Description/xmp:CreatorTool")
                .isEqualTo(emenda.getAplicacao());
    }

    private void savePdf(String templateResult) throws IOException, URISyntaxException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(DESTINATION_FILE_NAME);

        // Gera pdf
        assert resource != null;

        try (final OutputStream out = Files.newOutputStream(Paths.get(resource.toURI()))) {
            new FOPProcessor().processFOP(out, templateResult, xml, TipoDocumento.EMENDA);
        }
    }

    private Map<String, String> getXSLFoNamespaceContext() {
        final Map<String, String> context = new HashMap<>();
        context.put("fo", "http://www.w3.org/1999/XSL/Format");
        context.put("x", "http://www.w3.org/2001/XMLSchema-instance");
        context.put("rdf", "https://www.lexml.gov.br/eta/1.0/emenda");
        context.put("xmp", "https://www.lexml.gov.br/eta/1.0/emenda/comando");
        return context;
    }

    @BeforeEach
    void setUp() {
        emenda = readEmendaFile();
        xml = convertToXml(emenda);
        velocityTemplateProcessor =
                new VelocityTemplateProcessor(new TemplateLoaderBean());
    }

    private Emenda readEmendaFile() {
        final ClassLoader classLoader = getClass().getClassLoader();
        try {
            final URL sourceUrl = classLoader.getResource(SOURCE_FILE_NAME);
            assert sourceUrl != null;
            final File sourceFile = new File(sourceUrl.getFile());
            final String json = FileUtils.readFileToString(sourceFile, UTF_8);
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(json, EmendaPojo.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToXml(final Emenda emenda) {
        return new EmendaXmlMarshaller().toXml(emenda);
    }
}
