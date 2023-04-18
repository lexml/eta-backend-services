package br.gov.lexml.eta.etaservices.parsing.lexml;

import java.io.File;
import java.util.Arrays;

import br.gov.lexml.eta.etaservices.util.EtaFileUtil;
import br.gov.lexml.parser.pl.ArticulacaoParser;

public class LexmlParserImpl implements LexmlParser {
	
	private static final String LEXML_TEMPLATE = EtaFileUtil.readFromResourceAsString("lexmlTemplate.txt");
	// urn:lex:br:federal:medida.provisoria:2015-03-19;671
	private static final String URN_CONTEXTO = "urn:lex:br:senado.federal:projeto.lei;pls:1991;352";
	private ArticulacaoParser parser = new ArticulacaoParser();
	private boolean usarLinker = new File("/usr/local/bin/linkertool").isFile();
	
	@Override
	public String parse(String texto) {
		String parserResult = usarLinker? 
			parser.parseJList(Arrays.asList(texto.split("\n|\r")), URN_CONTEXTO):
			parser.parseJList(Arrays.asList(texto.split("\n|\r"))); 
		return LEXML_TEMPLATE.replace("---ARTICULACAO---", parserResult);
	}

}
