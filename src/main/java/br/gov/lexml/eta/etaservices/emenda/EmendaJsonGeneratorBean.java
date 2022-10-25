package br.gov.lexml.eta.etaservices.emenda;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.pdfa.PDFAttachmentHelper;

public class EmendaJsonGeneratorBean implements EmendaJsonGenerator {

    @Override
    public void extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws Exception {

        Path attachmentsDirPath = Files.createTempDirectory("extractJsonFromPdf");

        File pdfFile = new File(attachmentsDirPath.toFile(), "emenda.pdf");

        try (OutputStream fos = Files.newOutputStream(pdfFile.toPath())) {
            IOUtils.copy(pdfStream, fos);
        }

		try {			
        PDFAttachmentHelper.extractAttachments(pdfFile.getPath(),
                attachmentsDirPath.toAbsolutePath().toString());
		} 
		catch(Exception e) {
			// Ou não é um PDF ou não foi possível obter os anexos por algum outro motivo.
			throw new EtaBackendException("Não se trata de um arquivo gerado pelo editor de emendas.", e);
		}

        File xmlFile = new File(attachmentsDirPath.toFile(), "emenda.xml");
		
		if(!xmlFile.isFile()) {
			// Não possui anexo emenda.xml
			throw new EtaBackendException("Não se trata de um arquivo gerado pelo editor de emendas.");
		}
		

        try (InputStream fin  = Files.newInputStream(xmlFile.toPath())) {

            String xml = IOUtils.toString(fin, StandardCharsets.UTF_8);

            Emenda e = new EmendaXmlUnmarshaller().fromXml(xml);

            writeJson(e, jsonWriter);
        }

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


        try(JsonGenerator jsonGenerator = objectWriter.createGenerator(writer)) {
            jsonGenerator.writeObject(e);
            jsonGenerator.flush();
        }

    }


}
