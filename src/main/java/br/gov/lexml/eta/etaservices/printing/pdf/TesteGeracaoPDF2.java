package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.AnexoPDFA;
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
        List<AnexoPDFA> anexos = criarAnexos();
		try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream("c:/Temp/parecer-exemplo.pdf")) {
            pdfGeneratorParecer.generate(parecer, anexos, fileOut);
		} catch (IOException e) {
			throw new RuntimeException("Erro ao gerar o PDF do parecer.", e);
		}
	}

    private static List<AnexoPDFA> criarAnexos() throws IOException {
        List<AnexoPDFA> anexos = new ArrayList<>();

        ClassLoader cl = TesteGeracaoPDF2.class.getClassLoader();

        anexos.add(AnexoPDFA.builder().nomeArquivo("A.pdf").file(cl.getResourceAsStream("anexoA.pdf")).imprimir(true)
                .build());

        anexos.add(AnexoPDFA.builder().nomeArquivo("B.pdf").file(cl.getResourceAsStream("anexoB.pdf")).imprimir(true)
                .build());

        anexos.add(AnexoPDFA.builder().nomeArquivo("C.docx").file(cl.getResourceAsStream("anexoB.docx")).imprimir(false)
                .build());

        return anexos;

    }

}
