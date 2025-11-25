package br.gov.lexml.eta.etaservices.printing.xml;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.Parecer;
import br.gov.lexml.eta.etaservices.printing.pdf.PdfGeneratorParecer;
import br.gov.lexml.eta.etaservices.printing.pdf.VelocityTemplateProcessorFactory;

class Json2PDFParecer {

    private static PdfGeneratorParecer pdfGenerator = new PdfGeneratorParecer(new VelocityTemplateProcessorFactory());
	
    public static void main(String[] args) throws Exception {
        Json2PDFParecer.process("parecer");
    	System.out.println("Feito.");
	}
  

    private static void process(String nomeArquivo) throws Exception {
        Parecer parecer = setupParecer(nomeArquivo);
    	FileOutputStream fos = new FileOutputStream(new File("target", nomeArquivo + ".pdf"));
        pdfGenerator.generate(parecer, fos);
    }

    private static Parecer setupParecer(String nomeArquivo) {
        try {
            URL jsonUri = Json2PDFParecer.class.getResource("/" + nomeArquivo + ".json");

            if (jsonUri == null) {
                throw new IllegalStateException("Arquivo JSON não encontrado: " + nomeArquivo + ".json");
            }

            File file = new File(jsonUri.getFile());
            String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .build();

            return mapper.readValue(json, Parecer.class);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler/desserializar JSON do parecer", e);
        }
    }
}