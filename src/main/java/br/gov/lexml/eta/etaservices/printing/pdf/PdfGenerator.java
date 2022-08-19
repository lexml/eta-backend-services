package br.gov.lexml.eta.etaservices.printing.pdf;

import br.gov.lexml.eta.etaservices.emenda.Emenda;

import java.io.IOException;
import java.io.OutputStream;

public interface PdfGenerator {
    void generate(Emenda emenda, OutputStream outputStream) throws IOException;
}
