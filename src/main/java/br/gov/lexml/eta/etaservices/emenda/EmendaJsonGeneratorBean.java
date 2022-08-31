package br.gov.lexml.eta.etaservices.emenda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parsing.xml.EmendaXmlUnmarshaller;
import br.gov.lexml.pdfa.PDFAttachmentHelper;

public class EmendaJsonGeneratorBean implements EmendaJsonGenerator {
	
	@Override
	public void extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws Exception {
		
		Path attachmentsDirPath = Files.createTempDirectory("extractJsonFromPdf");
		
		File pdfFile = new File(attachmentsDirPath.toFile(), "emenda.pdf");
		
		IOUtils.copy(pdfStream, new FileOutputStream(pdfFile));
		
		PDFAttachmentHelper.extractAttachments(pdfFile.getPath(), 
				attachmentsDirPath.toAbsolutePath().toString());
		
		File xmlFile = new File(attachmentsDirPath.toFile(), "emenda.xml");
		String xml = IOUtils.toString(new FileInputStream(xmlFile), StandardCharsets.UTF_8.name());
		
        Emenda e = new EmendaXmlUnmarshaller().fromXml(xml);
		
		writeJson(e, jsonWriter);
		
	}

	@Override
	public void writeJson(Emenda e, Writer writer) throws Exception {
		
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ObjectWriter objectWriter = objectMapper
        		.setSerializationInclusion(Include.NON_NULL)
        		.registerModule(new JavaTimeModule())
        		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)        		
        		.writerWithDefaultPrettyPrinter()
        		.forType(Emenda.class);
        
        JsonGenerator jsonGenerator = objectWriter.createGenerator(writer);
        jsonGenerator.writeObject(e);
        jsonGenerator.flush();
        
	}
	

}
