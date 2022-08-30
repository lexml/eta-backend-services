package br.gov.lexml.eta.etaservices.parsing.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.emenda.Emenda;

class EmendaXmlUnmarshallerTest {

    private EmendaXmlUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new EmendaXmlUnmarshaller();
    }

    @Test
    void fromXml() throws Exception {
    	
    	String fileName = "emenda_mpv_905_2019_completa_disp_mpv";
    	
    	String xml = IOUtils.resourceToString("/" + fileName + ".xml", StandardCharsets.UTF_8);
    	
        Emenda e = unmarshaller.fromXml(xml);

        assertThat(e.getLocal()).isEqualTo("Sala da comissão");
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ObjectWriter objectWriter = objectMapper
        		.setSerializationInclusion(Include.NON_NULL)
        		.registerModule(new JavaTimeModule())
        		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)        		
        		.writerWithDefaultPrettyPrinter()
        		.forType(Emenda.class);
        
        FileWriter fileWriter = new FileWriter("target/" + fileName + ".json");
        JsonGenerator jsonGenerator = objectWriter.createGenerator(fileWriter);
        jsonGenerator.writeObject(e);
        jsonGenerator.flush();
        fileWriter.close();
    }
}