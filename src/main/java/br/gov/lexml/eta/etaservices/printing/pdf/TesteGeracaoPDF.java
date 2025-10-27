package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import br.gov.lexml.eta.etaservices.emenda.Emenda;

/**
 * Para executar e testar geração do PDF durante desenvolvimento.
 */
public class TesteGeracaoPDF {

	public static final String EMENDA_XML = "<emenda>Teste</emenda>";
	private static final String TARGET_FO_EMENDA_XML = "target/fo-emenda.xml";
	private static final String TARGET_EMENDA_PDF = "target/emenda.pdf";

	public static void main(String[] args) throws IOException {
		processaVelocity();
		processaFOEmenda();
	}

	private static void processaVelocity() throws IOException {
        Emenda emenda = null;
        String fo = new VelocityTemplateProcessor(new TemplateLoaderBean()).getTemplateResult(emenda);
		FileUtils.writeStringToFile(new File(TARGET_FO_EMENDA_XML), fo, StandardCharsets.UTF_8);

	}

	private static void processaFOEmenda() throws IOException {
		try(OutputStream out = Files.newOutputStream(Paths.get(TARGET_EMENDA_PDF))) {
			String xslFo = FileUtils.readFileToString(new File(TARGET_FO_EMENDA_XML),
					StandardCharsets.UTF_8);
            new FOPProcessor().processFOP(out, xslFo, EMENDA_XML, TipoDocumento.EMENDA);
		}
	}

}
