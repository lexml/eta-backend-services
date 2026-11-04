package br.gov.lexml.eta.lexmljsonix.conversor;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;

/**
 * Classe usada para converter, de xml para json e vice-versa, o texto em formato Lexml de uma proposição.
 * Esta classe utiliza o executável gerado a partir do código do projeto
 * <a href="https://github.com/lexml/jsonix-lexml">https://github.com/lexml/jsonix-lexml</a> (versão 2.0.0 ou
 * superior) para realizar a conversão.
 * <p>
 * A execução é delegada ao {@link ConversorDocumentoArticuladoCli}: cada conversão usa arquivos em um diretório
 * temporário próprio, removido ao final, e uma conversão que falha (código de saída diferente de 0, saída ausente
 * ou vazia, tempo limite excedido) lança exceção com a mensagem de erro do executável.
 *
 * @author Robson Barros
 */
public class ConversorLexmlJsonixImpl implements ConversorLexmlJsonix {

	private static final Logger log = LoggerFactory.getLogger(ConversorLexmlJsonixImpl.class);

	private final ConversorDocumentoArticulado conversor;

	public ConversorLexmlJsonixImpl(LexmlJsonixProperties lexmlJsonixProperties, String activeProfile) throws FileNotFoundException {
		this(lexmlJsonixProperties, activeProfile, null);
	}

	/**
	 * Permite informar o conversor que executa o jsonix-lexml ({@code null} cria um
	 * {@link ConversorDocumentoArticuladoCli} com o executável e o tempo limite das propriedades).
	 */
	ConversorLexmlJsonixImpl(LexmlJsonixProperties lexmlJsonixProperties, String activeProfile,
			ConversorDocumentoArticulado conversor) throws FileNotFoundException {
		checarExistenciaArquivo(lexmlJsonixProperties.getCli(), activeProfile);
		avisarPropriedadesObsoletas(lexmlJsonixProperties);
		this.conversor = conversor != null ? conversor
				: new ConversorDocumentoArticuladoCli(Paths.get(lexmlJsonixProperties.getCli()), lexmlJsonixProperties.getTimeout());
	}

	private static void checarExistenciaArquivo(String filename, String activeProfile) throws FileNotFoundException {
		if ("test".equals(activeProfile))
			return;

		if (!(new File(filename).isFile()))
			throw new FileNotFoundException(filename);
	}

	@SuppressWarnings("deprecation")
	private static void avisarPropriedadesObsoletas(LexmlJsonixProperties propriedades) {
		if (Boolean.TRUE.equals(propriedades.getUsarEntradaSaidaPadraoDuranteConversao())) {
			log.warn("A propriedade lexml-jsonix.usar-entrada-saida-padrao-durante-conversao está obsoleta e não tem efeito: "
					+ "a conversão é sempre feita por arquivo.");
		}
		if (Boolean.FALSE.equals(propriedades.getRemoverDiretorioAposConversao())) {
			log.warn("A propriedade lexml-jsonix.remover-diretorio-apos-conversao está obsoleta e não tem efeito: "
					+ "o diretório temporário da conversão é sempre removido.");
		}
	}

	public String xmlToJson(String xml) {
		try {
			return conversor.xmlParaJson(xml);
		} catch (RuntimeException e) {
			throw new RuntimeException("Erro ao converter de xml para jsonix: " + e.getMessage(), e);
		}
	}

	public String jsonToXml(String json) {
		try {
			return conversor.jsonParaXml(json);
		} catch (RuntimeException e) {
			throw new RuntimeException("Erro ao converter de json para xml: " + e.getMessage(), e);
		}
	}

}
