package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.gov.lexml.eta.etaservices.parecer.AnexoPDFA;
import br.gov.lexml.eta.etaservices.parecer.Parecer;
import br.gov.lexml.eta.etaservices.util.BytesUtil;

@SuppressWarnings("unused")
public class PdfGeneratorParecer  {
    private final VelocityTemplateProcessorFactory templateProcessorFactory;

    public PdfGeneratorParecer(VelocityTemplateProcessorFactory templateProcessorFactory) {
        this.templateProcessorFactory = templateProcessorFactory;
    }

    public void generate(Parecer parecer, List<AnexoPDFA> anexosPDF_A, OutputStream outputStream) throws Exception {
        System.out.println("parecer");
        System.out.println(parecer);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(parecer);

        List<AnexoPDFA> anexosParaPDFA = new ArrayList<>();
        List<ByteArrayInputStream> anexosByteParaImpressao = new ArrayList<>();

        if (anexosPDF_A != null) {
            for (AnexoPDFA anexo : anexosPDF_A) {
                if (anexo == null || anexo.getFile() == null) {
                    continue;
                }

                byte[] bytes = toByteArray(anexo.getFile());

                AnexoPDFA cloneParaPDFA = AnexoPDFA.builder().nomeArquivo(anexo.getNomeArquivo()).file(new ByteArrayInputStream(bytes)) // stream novo
                        .imprimir(anexo.isImprimir()).build();
                anexosParaPDFA.add(cloneParaPDFA);

                if (anexo.isImprimir()) {
                    anexosByteParaImpressao.add(new ByteArrayInputStream(bytes));
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String parecerInJson = toParecerJson(parecer);

        new FOPProcessor().processFOP(baos, templateResult, parecerInJson, anexosParaPDFA, anexosByteParaImpressao, TipoDocumento.PARECER);
        
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

    private byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

}

