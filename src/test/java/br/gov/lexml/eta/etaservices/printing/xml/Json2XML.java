package br.gov.lexml.eta.etaservices.printing.xml;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoElementoPojo;
import br.gov.lexml.eta.etaservices.printing.json.RevisaoJustificativaPojo;

class Json2XML {

    public static void main(String[] args) {
//    	Json2XML.process("emenda_mpv_885_2019_incompleta_dois_itens");
//    	Json2XML.process("emenda_mpv_905_2019_completa_disp_mpv");
//    	Json2XML.process("emenda_mpv_905_2019_agrupadores");
    	Json2XML.process("mp-com-revisoes");
//    	Json2XML.process("mp-com-revisoes-2");
    	System.out.println("Feito.");
	}
  

    private static void process(String nomeArquivo) {
    	Emenda emenda = setupEmenda(nomeArquivo); 
    	String xml = getXml(emenda);
    	try {
			FileUtils.write(new File("target/" + nomeArquivo + ".xml"), xml, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    private static Emenda setupEmenda(String nomeArquivo) {

        try {
            URL jsonUri = Json2XML.class.getResource("/" + nomeArquivo + ".json");

            assert jsonUri != null;

            File file = new File(jsonUri.getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerSubtypes(
                new NamedType(RevisaoElementoPojo.class, "RevisaoElemento"),
                new NamedType(RevisaoJustificativaPojo.class, "RevisaoJustificativa"));
            return objectMapper.readValue(text, EmendaPojo.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    private static String getXml(Emenda emenda) {

        final EmendaXmlMarshaller marshaller = new EmendaXmlMarshaller();

        return marshaller.toXml(emenda);

    }

}