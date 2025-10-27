package br.gov.lexml.eta.etaservices.printing.xml;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import br.gov.lexml.eta.etaservices.printing.pdf.PdfGeneratorParecer;
import br.gov.lexml.eta.etaservices.printing.pdf.VelocityTemplateProcessorFactory;

class Json2PDFParecer {

    private static PdfGeneratorParecer pdfGenerator = new PdfGeneratorParecer(new VelocityTemplateProcessorFactory());
	
    public static void main(String[] args) throws Exception {
        Json2PDFParecer.process("parecer");
    	System.out.println("Feito.");
	}
  

    private static void process(String nomeArquivo) throws Exception {
        String parecerInJson = setupParecer(nomeArquivo);
        System.out.println("parecerInJson");
        System.out.println(parecerInJson);
    	FileOutputStream fos = new FileOutputStream(new File("target", nomeArquivo + ".pdf"));
        pdfGenerator.generate(parecerInJson, fos);
    }

    private static String setupParecer(String nomeArquivo) {

        try {
            URL jsonUri = Json2PDFParecer.class.getResource("/" + nomeArquivo + ".json");

            assert jsonUri != null;

            File file = new File(jsonUri.getFile());
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

}