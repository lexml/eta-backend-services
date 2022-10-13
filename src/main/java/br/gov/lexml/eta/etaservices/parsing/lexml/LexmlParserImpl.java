package br.gov.lexml.eta.etaservices.parsing.lexml;

import java.util.Arrays;

import br.gov.lexml.eta.etaservices.util.EtaFileUtil;
import br.gov.lexml.parser.pl.ArticulacaoParser;

public class LexmlParserImpl implements LexmlParser {
	
	private static final String LEXML_TEMPLATE = EtaFileUtil.readFromResourceAsString("lexmlTemplate.txt");
	private ArticulacaoParser parser = new ArticulacaoParser();

	@Override
	public String parse(String texto) {
		String parserResult = parser.parseJList(Arrays.asList(texto.split("\n|\r"))); 
		return LEXML_TEMPLATE.replace("---ARTICULACAO---", parserResult);
	}

}
