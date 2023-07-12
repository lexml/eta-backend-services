package br.gov.lexml.eta.etaservices.parsing.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.EmendaJsonGeneratorBean;

class EmendaXmlUnmarshallerTest {

    private EmendaXmlUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new EmendaXmlUnmarshaller();
    }

    @Test
    void fromXml() throws Exception {
    	
//    	String fileName = "emenda_mpv_905_2019_completa_disp_mpv";
    	String fileName = "mp-com-revisoes";
//    	String fileName = "mp-com-revisoes-2";
    	
    	try {
        	String xml = IOUtils.resourceToString("/" + fileName + ".xml", StandardCharsets.UTF_8);
        	
            Emenda e = unmarshaller.fromXml(xml);

            assertThat(e.getLocal()).isEqualTo("Sala da comissão");
            
            FileWriter fileWriter = new FileWriter("target/" + fileName + ".json");
            
            new EmendaJsonGeneratorBean().writeJson(e, fileWriter);
            
            fileWriter.close();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}    	
        
    }
}