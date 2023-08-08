package br.gov.lexml.eta.etaservices.printing.xml;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.json.EmendaPojo;
import br.gov.lexml.eta.etaservices.printing.pdf.PdfGenerator;
import br.gov.lexml.eta.etaservices.printing.pdf.PdfGeneratorBean;
import br.gov.lexml.eta.etaservices.printing.pdf.VelocityTemplateProcessorFactory;

class Json2PDF {

	private static PdfGenerator pdfGenerator = new PdfGeneratorBean(new VelocityTemplateProcessorFactory(), new EmendaXmlMarshaller());
	
    public static void main(String[] args) throws Exception {
//    	Json2PDF.process("emenda_mpv_905_2019_agrupadores");
    	Json2PDF.process("test1");
    	System.out.println("Feito.");
	}
  

    private static void process(String nomeArquivo) throws Exception {
    	Emenda emenda = setupEmenda(nomeArquivo);
    	FileOutputStream fos = new FileOutputStream(new File("target", nomeArquivo + ".pdf"));
    	pdfGenerator.generate(emenda, fos);
    }

    private static Emenda setupEmenda(String nomeArquivo) {

        try {
            URL jsonUri = Json2PDF.class.getResource("/" + nomeArquivo + ".json");

            assert jsonUri != null;

            File file = new File(jsonUri.getFile());
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(text, EmendaPojo.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

}