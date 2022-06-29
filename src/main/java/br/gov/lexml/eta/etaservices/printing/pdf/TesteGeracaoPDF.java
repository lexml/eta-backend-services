package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Para executar e testar geração do PDF durante desenvolvimento.
 */
public class TesteGeracaoPDF {

	public static void main(String[] args) throws Exception {

		// Geração do PDF
		processaVelocity();
		processaFOEmenda();

		// Transformação de um xslfo simples, adicionando um emenda.xml
//		processaExemploFORequerimento();
//		processaExemploFOEmenda();

	}

	private static void processaVelocity() throws Exception {

		Map<String, Object> infos = new HashMap<>();
		infos.put("aplicacao", "LexEdit");
		infos.put("imprimirBrasao", true);
		infos.put("textoCabecalho", "Gabinete do Senador Ricardo Lima");

		String fo = new VelocityTemplateProcessor(infos).getTemplateResult();
		FileUtils.writeStringToFile(new File("target/fo-emenda.xml"), fo);

	}

	private static void processaFOEmenda() throws Exception {
		OutputStream out = new FileOutputStream("target/emenda.pdf");
		String xslfo = FileUtils.readFileToString(new File("target/fo-emenda.xml"), "UTF-8");
		String emendaXML = "<emenda>Teste</emenda>";
		new FOPProcessor().processFOP(out, xslfo, emendaXML);
	}


	private static void processaExemploFORequerimento() throws Exception {
		OutputStream out = new FileOutputStream("target/exemplo-requerimento.pdf");
		String xslfo = IOUtils.toString(TesteGeracaoPDF.class.getResourceAsStream("/exemplo-fo-requerimento.xml"), "UTF-8");
		String emendaXML = "<emenda>Teste</emenda>";
		new FOPProcessor().processFOP(out, xslfo, emendaXML);
	}

	private static void processaExemploFOEmenda() throws Exception {
		OutputStream out = new FileOutputStream("target/exemplo-emenda.pdf");
		String xslfo = IOUtils.toString(TesteGeracaoPDF.class.getResourceAsStream("/exemplo-fo-emenda-lexedit-swing.xml"), "UTF-8");
		String emendaXML = "<emenda>Teste</emenda>";
		new FOPProcessor().processFOP(out, xslfo, emendaXML);
	}

}
