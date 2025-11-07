package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.Parecer;
import br.gov.lexml.eta.etaservices.util.BytesUtil;

@SuppressWarnings("unused")
public class PdfGeneratorParecer  {
    private final VelocityTemplateProcessorFactory templateProcessorFactory;

    public PdfGeneratorParecer(VelocityTemplateProcessorFactory templateProcessorFactory) {
        this.templateProcessorFactory = templateProcessorFactory;
    }

    public void generate(Parecer parecer, OutputStream outputStream) throws Exception {
        System.out.println("parecer");
        System.out.println(parecer);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(parecer);
        List<ByteArrayInputStream> anexos = getAnexos(parecer);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String parecerInJson = toParecerJson(parecer);

        new FOPProcessor().processFOP(baos, templateResult, parecerInJson, anexos, TipoDocumento.PARECER);
        
        // Insere hash de verificação
		byte[] bytearr = baos.toByteArray();
		int i = BytesUtil.lastIndexOf(bytearr, 
				"<check:hash>00000000000000000000000000000000".getBytes(StandardCharsets.UTF_8));
		if(i >= 0) {
			byte[] md5bytes = md5Hex(bytearr).getBytes(StandardCharsets.UTF_8);
			int openTagLen = "<check:hash>".getBytes(StandardCharsets.UTF_8).length;
			System.arraycopy(md5bytes, 0, bytearr, i + openTagLen, md5bytes.length);
		}
		outputStream.write(bytearr);
		outputStream.flush();
    }
    

    private String md5Hex(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();    	
    }

    private String toParecerJson(Parecer parecer) {
        ObjectMapper om = new ObjectMapper();

        om.registerModule(new JavaTimeModule());

        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        om.setTimeZone(TimeZone.getTimeZone("UTC"));
        om.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return om.writeValueAsString(parecer);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar Parecer para JSON", e);
        }
    }

    private List<ByteArrayInputStream> getAnexos(Parecer parecer) {
        if (parecer.getAnexos() != null) {
            return parecer.getAnexos().parallelStream()
        		.map(anexo -> Base64.getDecoder().decode(anexo.getBase64()))
        		.map(anexo -> new ByteArrayInputStream(anexo))
        		.collect(Collectors.toList());        	
        }
		return new ArrayList<>();
	}

}

