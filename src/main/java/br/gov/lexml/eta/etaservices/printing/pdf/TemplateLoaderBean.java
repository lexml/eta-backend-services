package br.gov.lexml.eta.etaservices.printing.pdf;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateLoaderBean implements TemplateLoader {

    @Override
    public String loadTemplate(final String templateAddress) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(templateAddress);

        assert resourceAsStream != null;

        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
    }
}
