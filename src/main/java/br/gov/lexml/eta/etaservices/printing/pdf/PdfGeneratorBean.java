package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.printing.xml.EmendaXmlMarshaller;

import java.io.IOException;
import java.io.OutputStream;

public class PdfGeneratorBean implements PdfGenerator {
    private final VelocityTemplateProcessor templateProcessor;
    private final EmendaXmlMarshaller emendaXmlMarshaller;

    public PdfGeneratorBean(VelocityTemplateProcessor templateProcessor,
                            EmendaXmlMarshaller emendaXmlMarshaller) {
        this.templateProcessor = templateProcessor;
        this.emendaXmlMarshaller = emendaXmlMarshaller;
    }

    @Override
    public void generate(Emenda emenda, OutputStream outputStream) throws IOException {
        final String xml = emendaXmlMarshaller.toXml(emenda);
        final String templateResult =
                templateProcessor.getTemplateResult(emenda);
        new FOPProcessor().processFOP(outputStream, templateResult, xml);
    }

}

