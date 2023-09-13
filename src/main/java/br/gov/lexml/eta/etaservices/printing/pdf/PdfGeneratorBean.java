package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;

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
    public void generate(Emenda emenda, OutputStream outputStream) throws IOException {
        final String xml = emendaXmlMarshaller.toXml(emenda);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(emenda);
        List<ByteArrayInputStream> anexos = getAnexos(emenda);
    
        new FOPProcessor().processFOP(outputStream, templateResult, xml, anexos);
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

