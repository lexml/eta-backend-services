package br.gov.lexml.eta.etaservices.printing.xml;


import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.ArquivoEmenda;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.xmlunit.assertj3.XmlAssert.assertThat;

class EmendaXmlMarshallingTest {
    private Source xmlSource;

    @Test
    void testMetadados() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/Metadados/Aplicacao")
                .isEqualToIgnoringCase("");
    }

    @Test
    void testColegiadoApreciador() {

        assertThat(xmlSource)
                .valueByXPath("/Emenda/ColegiadoApreciador/@siglaCasaLegislativa")
                .isEqualToIgnoringCase("CN");
    }

    @BeforeEach
    void setUp() {
        final Emenda emenda = setupEmenda();
        xmlSource = getXmlSource(emenda);
    }

    private Emenda setupEmenda() {

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL sourceUrl = classLoader.getResource("test1.json");
            assert sourceUrl != null;
            File file = new File(sourceUrl.getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(text, ArquivoEmenda.class).getEmenda();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Source getXmlSource(Emenda emenda) {

        final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

        final String xml = marshaller.toXml(emenda);

        System.out.println(xml);

        return Input.fromString(xml).build();
    }

}