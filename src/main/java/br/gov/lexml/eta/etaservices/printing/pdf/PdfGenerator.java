package br.gov.lexml.eta.etaservices.printing.pdf;

import java.io.OutputStream;

import br.gov.lexml.eta.etaservices.emenda.Emenda;

public interface PdfGenerator {
    void generate(Emenda emenda, OutputStream outputStream) throws Exception;
}
