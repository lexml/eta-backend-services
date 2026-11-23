package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utilitário de teste para gerar arquivos {@code .docx} mínimos válidos em memória.
 */
final class DocxTesteBuilder {

    private DocxTesteBuilder() {
    }

    static byte[] comParagrafos(String... paragrafos) throws IOException {
        return comParagrafos(Arrays.asList(paragrafos));
    }

    static byte[] comParagrafos(Iterable<String> paragrafos) throws IOException {
        StringBuilder body = new StringBuilder();
        for (String paragrafo : paragrafos) {
            body.append("<w:p><w:r><w:t>");
            if (paragrafo != null) {
                body.append(escaparXml(paragrafo));
            }
            body.append("</w:t></w:r></w:p>");
        }
        return comCorpoXml(body.toString());
    }

    static byte[] comCorpoXml(String corpoXml) throws IOException {
        String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:body>"
                + corpoXml
                + "</w:body>"
                + "</w:document>";
        return zipComEntrada("word/document.xml", documentXml.getBytes(StandardCharsets.UTF_8));
    }

    static byte[] zipComEntrada(String nomeEntrada, byte[] conteudo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(nomeEntrada));
            zos.write(conteudo);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static String escaparXml(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}

