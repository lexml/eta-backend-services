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
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.Parecer;
import br.gov.lexml.eta.etaservices.parecer.ParecerJsonDTO;
import br.gov.lexml.eta.etaservices.parecer.ParecerMapper;
import br.gov.lexml.eta.etaservices.util.BytesUtil;

@SuppressWarnings("unused")
public class PdfGeneratorParecer  {
    private final VelocityTemplateProcessorFactory templateProcessorFactory;

    public PdfGeneratorParecer(VelocityTemplateProcessorFactory templateProcessorFactory) {
        this.templateProcessorFactory = templateProcessorFactory;
    }

    public void generate(String parecerInJson, OutputStream outputStream) throws Exception {
        Parecer parecer = getParecer(parecerInJson);
        System.out.println("parecer");
        System.out.println(parecer);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(parecer);
        List<ByteArrayInputStream> anexos = getAnexos(parecer);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
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
    
    private Parecer getParecer(String parecerInJson) {
        try {
            ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            ParecerJsonDTO dto = om.readValue(parecerInJson, ParecerJsonDTO.class);
            return ParecerMapper.from(dto);

        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao processar parecer.json", e);
        }
    }

    private String md5Hex(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();    	
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

