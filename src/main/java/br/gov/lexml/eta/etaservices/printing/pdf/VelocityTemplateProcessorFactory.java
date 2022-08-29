package br.gov.lexml.eta.etaservices.printing.pdf;

public class VelocityTemplateProcessorFactory {

    public final VelocityTemplateProcessor get() {
        return new VelocityTemplateProcessor(new TemplateLoaderBean());
    }

}
