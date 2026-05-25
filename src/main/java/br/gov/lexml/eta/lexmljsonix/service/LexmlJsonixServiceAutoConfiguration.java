package br.gov.lexml.eta.lexmljsonix.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import br.gov.lexml.eta.lexmljsonix.conversor.ConversorLexmlJsonix;
import br.gov.lexml.eta.lexmljsonix.conversor.LexmlJsonixProperties;

@Configuration
@ConditionalOnClass(LexmlJsonixService.class)
public class LexmlJsonixServiceAutoConfiguration {
	
	@Bean
	@ConditionalOnMissingBean
	public LexmlJsonixService lexmlProposicaoService(ConversorLexmlJsonix conversorLexmlJsonix, LexmlJsonixProperties lexmlJsonixProperties, RestTemplate restTemplate) {
		return new LexmlJsonixServiceImpl(conversorLexmlJsonix, lexmlJsonixProperties, restTemplate);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
