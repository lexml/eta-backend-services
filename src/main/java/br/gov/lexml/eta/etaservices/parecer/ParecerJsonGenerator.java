package br.gov.lexml.eta.etaservices.emenda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.pdfa.PDFAttachmentHelper;

public class ParecerJsonGenerator {

    public void extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws Exception {

        Path attachmentsDirPath = Files.createTempDirectory("extractJsonFromPdf");

        File pdfFile = new File(attachmentsDirPath.toFile(), "parecer.pdf");

        try (OutputStream fos = Files.newOutputStream(pdfFile.toPath())) {
            IOUtils.copy(pdfStream, fos);
        }

		try {			
        PDFAttachmentHelper.extractAttachments(pdfFile.getPath(),
                attachmentsDirPath.toAbsolutePath().toString());
		} 
		catch(Exception e) {
			// Ou não é um PDF ou não foi possível obter os anexos por algum outro motivo.
            throw new EtaBackendException("Não se trata de um arquivo gerado pelo editor de pareceres.", e);
		}

        File jsonFile = new File(attachmentsDirPath.toFile(), "parecer.json");
        if (!jsonFile.isFile()) {
            File[] candidates = attachmentsDirPath.toFile().listFiles((dir, name) -> "parecer.json".equalsIgnoreCase(name));
            if (candidates != null && candidates.length > 0) {
                jsonFile = candidates[0];
            }
        }
		
        if (!jsonFile.isFile()) {
            // Não possui anexo parecer.json
            throw new EtaBackendException("Não se trata de um arquivo gerado pelo editor de pareceres (anexo parecer.json não encontrado).");
        }


        try (Reader in = Files.newBufferedReader(jsonFile.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(in, jsonWriter);
            jsonWriter.flush();
        } finally {
            try {
                deleteRecursively(attachmentsDirPath);
            } catch (Exception cleanup) {
            }
        }

    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root))
            return;
        Files.walk(root).sorted(Comparator.reverseOrder()).forEach(p -> {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
            }
        });
    }

}
