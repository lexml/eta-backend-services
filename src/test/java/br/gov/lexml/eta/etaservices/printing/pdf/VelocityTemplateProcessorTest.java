package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.xmlunit.assertj3.XmlAssert.assertThat;

class VelocityTemplateProcessorTest {

    public static final String SOURCE_FILE_NAME = "emenda_mpv_905_2019_completa_disp_mpv.json";
    public static final String DESTINATION_FILE_NAME = "test1.pdf";
    private Emenda emenda;
    private String xml;

    private VelocityTemplateProcessor velocityTemplateProcessor;


    @Test
    @DisplayName("Verifica se nome da aplicação é preenchido")
    void testaMetadadosEmenda() throws IOException, URISyntaxException {
        final String templateResult =
                velocityTemplateProcessor.getTemplateResult(emenda);
        savePdf(templateResult);

        final Source result = Input.fromString(templateResult).build();

        assertThat(result)
                .withNamespaceContext(getXSLFoNamespaceContext())
                .valueByXPath("/fo:root/fo:declarations/x:xmpmeta/rdf:RDF/rdf:Description/xmp:CreatorTool[1]")
                .isEqualTo(emenda.getAplicacao());
    }

    private void savePdf(String templateResult) throws IOException, URISyntaxException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(DESTINATION_FILE_NAME);

        // Gera pdf
        assert resource != null;

        try (final OutputStream out = Files.newOutputStream(Paths.get(resource.toURI()))) {
            new FOPProcessor().processFOP(out, templateResult, xml);
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