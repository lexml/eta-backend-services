package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;

import java.io.IOException;
import java.io.OutputStream;

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
        System.out.println(xml);
        final String templateResult =
                templateProcessorFactory.get().getTemplateResult(emenda);
        new FOPProcessor().processFOP(outputStream, templateResult, xml);
    }

}

