package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.IOException;

public interface TemplateLoader {
    String loadTemplate(String templateAddress) throws IOException;
}
