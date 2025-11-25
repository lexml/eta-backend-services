package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.Parecer;

/**
 * Para executar e testar geração do PDF durante desenvolvimento.
 */
public class TesteGeracaoPDF2 {
	private static final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private static Parecer readParecerFromJsonFile(String filePath) throws IOException {
		String jsonContent = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
		return objectMapper.readValue(jsonContent, Parecer.class);
	}

	public static void main(String[] args) throws Exception {
		Parecer parecer = readParecerFromJsonFile("c:/Temp/parecer-exemplo.json");
		VelocityTemplateProcessorFactory templateProcessorFactory = new VelocityTemplateProcessorFactory();
		PdfGeneratorParecer pdfGeneratorParecer = new PdfGeneratorParecer(templateProcessorFactory);
		try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream("c:/Temp/parecer-exemplo.pdf")) {
			pdfGeneratorParecer.generate(parecer, fileOut);
		} catch (IOException e) {
			throw new RuntimeException("Erro ao gerar o PDF do parecer.", e);
		}
	}

}
