package br.gov.lexml.eta.etaservices.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

public class EtaFileUtil {
	
	private EtaFileUtil() {}

	public static String readFromResourceAsString(String resourceName) {
		ClassPathResource classPathResource = new ClassPathResource(resourceName);

		try {
			InputStream inputStream = classPathResource.getInputStream();
			
			return new BufferedReader(
					new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				        .lines()
				        .collect(Collectors.joining("\n"));
				        
		} catch (Exception e) {
			throw new RuntimeException("Arquivo " + resourceName + " não encontrado.", e);
		} 		
	}

}
