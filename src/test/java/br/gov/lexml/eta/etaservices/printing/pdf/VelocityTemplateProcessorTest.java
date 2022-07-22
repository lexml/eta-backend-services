package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.printing.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.ArquivoEmenda;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.xmlunit.assertj3.XmlAssert.assertThat;

class VelocityTemplateProcessorTest {

    private Emenda emenda;

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            File file = new File(classLoader.getResource("test1.json").getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            emenda = objectMapper.readValue(text, ArquivoEmenda.class).getEmenda();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Verifica se nome da aplicação é preenchido")
    void testaMetadadosEmenda() throws Exception {
        Map<String, Object> mapaParaContexto = new HashMap<>();
        mapaParaContexto.put("emenda", emenda);

        VelocityTemplateProcessor velocityTemplateProcessor =
                new VelocityTemplateProcessor(mapaParaContexto);

        String templateResult = velocityTemplateProcessor.getTemplateResult();

        Source result = Input.fromString(templateResult).build();

        Map<String, String> context = new HashMap<>();
        context.put("fo", "http://www.w3.org/1999/XSL/Format");
        context.put("x", "http://www.w3.org/2001/XMLSchema-instance");
        context.put("rdf", "http://www.lexml.gov.br/eta/1.0/emenda");
        context.put("xmp", "http://www.lexml.gov.br/eta/1.0/emenda/comando");

        assertThat(result)
                .withNamespaceContext(context)
                .valueByXPath("/fo:root/fo:declarations/x:xmpmeta/rdf:RDF/rdf:Description/xmp:CreatorTool[1]")
                .isEqualTo(emenda.getAplicacao());


        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test1.pdf");

        // Gera pdf
        assert resource != null;
        try(OutputStream out = Files.newOutputStream(Paths.get(resource.toURI()))) {
            new FOPProcessor().processFOP(out, templateResult, "");
        }
    }

}