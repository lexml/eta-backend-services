package br.gov.lexml.eta.etaservices.emenda;

import java.io.InputStream;
import java.io.Writer;

public interface EmendaJsonGenerator {

	void extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws Exception;

	void writeJson(Emenda e, Writer writer) throws Exception;

}