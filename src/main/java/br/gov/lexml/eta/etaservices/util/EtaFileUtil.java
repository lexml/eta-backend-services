package br.gov.lexml.eta.etaservices.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class EtaFileUtil {
    
    private static final Logger log = LoggerFactory.getLogger(EtaFileUtil.class);    
	
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

	public static String readFromImageAsBase64String(InputStream is) {
		if (is == null) {
			return null;
		}

		String base64Image = "";
		try {
			byte imageData[] = IOUtils.toByteArray(is);
			base64Image = Base64.getEncoder().encodeToString(imageData);
		} catch (IOException ioe) {
			log.error("Exception while reading the Image ", ioe);
		}
		return base64Image;	
	}

}
