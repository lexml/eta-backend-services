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

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;
import br.gov.lexml.eta.etaservices.util.BytesUtil;

@SuppressWarnings("unused")
public class PdfGeneratorBean implements PdfGenerator {
    private final VelocityTemplateProcessorFactory templateProcessorFactory;
    private final EmendaXmlMarshaller emendaXmlMarshaller;

    public PdfGeneratorBean(VelocityTemplateProcessorFactory templateProcessorFactory,
                            EmendaXmlMarshaller emendaXmlMarshaller) {
        this.templateProcessorFactory = templateProcessorFactory;
        this.emendaXmlMarshaller = emendaXmlMarshaller;
    }

    @Override
    public void generate(Emenda emenda, OutputStream outputStream) throws Exception {
        final String xml = emendaXmlMarshaller.toXml(emenda);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(emenda);
        List<ByteArrayInputStream> anexos = getAnexos(emenda);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
        new FOPProcessor().processFOP(baos, templateResult, xml, anexos);
        
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

	private List<ByteArrayInputStream> getAnexos(Emenda emenda) {
		if(emenda.getAnexos() != null) {
        	return emenda.getAnexos().parallelStream()
        		.map(anexo -> Base64.getDecoder().decode(anexo.getBase64()))
        		.map(anexo -> new ByteArrayInputStream(anexo))
        		.collect(Collectors.toList());        	
        }
		return new ArrayList<>();
	}

}

