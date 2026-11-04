package br.gov.lexml.eta.lexmljsonix.conversor;

import java.time.Duration;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@ConfigurationProperties(prefix = "lexml-jsonix")
@Data
@Validated
public class LexmlJsonixProperties {
	
	/**
	 * Caminho e nome do executável do conversor jsonix
	 * @see <a href="https://github.com/lexml/jsonix-lexml#jsonix-lexml">github.com/lexml/jsonix-lexml#jsonix-lexml</a>
	 */
	@NotBlank
	private String cli;
	
	/**
	 * Url do serviço que retorna os metadados da proposição
	 */
	private String urlProposicoes = "https://legis.senado.gov.br/legis/resources/lex/proposicoes";
	
	/**
	 * Url do serviço que retorna o arquivo zip contendo o arquivo "Texto.xml" (que traz o texto da norma em formato lexml)
	 */
	private String urlSdleg = "https://legis.senado.gov.br/sdleg-getter/documento/download";
	
	
	private String urlDatasMPVs = "https://legis.senado.gov.br/legis/resources/lex/proposicoes/datas-mpvs";
	
	/**
	 * Tempo máximo de cada conversão pelo executável jsonix-lexml (padrão: 60 segundos). Aceita valores como
	 * {@code 60s} ou {@code 2m}.
	 */
	private Duration timeout = Duration.ofSeconds(60);

	/**
	 * Obsoleta: não tem efeito. A conversão é sempre feita por arquivo, em um diretório temporário próprio.
	 * Continua aceita para não quebrar configurações existentes; informada como {@code true}, gera um aviso no log.
	 * Fica {@code null} quando não informada.
	 */
	@Deprecated
	private Boolean usarEntradaSaidaPadraoDuranteConversao;

	/**
	 * Obsoleta: não tem efeito. O diretório temporário da conversão é sempre removido ao final.
	 * Continua aceita para não quebrar configurações existentes; informada como {@code false}, gera um aviso no log.
	 * Fica {@code null} quando não informada.
	 */
	@Deprecated
	private Boolean removerDiretorioAposConversao;
}
