package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFoGerador.FoGerado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Gera, sob demanda, o arquivo Word ({@code .docx} / OOXML) editável de uma proposição a partir do
 * {@code documento-articulado.json}, traduzindo diretamente o XSL-FO usado na geração do PDF.
 * <p>
 * O DOCX contém apenas o documento editável: não embute o {@code documento-articulado.xml} nem anexos (as páginas dos
 * anexos do PDF não são concatenadas) e não permite recuperar o JSON original.
 */
public class DocumentoArticuladoDocxGenerator {

    private final DocumentoArticuladoFoGerador foGerador;

    public DocumentoArticuladoDocxGenerator(ConversorDocumentoArticulado conversor) {
        this.foGerador = new DocumentoArticuladoFoGerador(Objects.requireNonNull(conversor, "conversor"));
    }

    /**
     * @param json conteúdo do {@code documento-articulado.json}
     * @param outputStream destino do DOCX; nada é escrito se a geração falhar, e o stream não é fechado
     * @throws EtaBackendException se o JSON não for um documento LexML ou se a conversão/geração falhar
     * @throws IOException se houver erro ao escrever no destino
     */
    public void generate(String json, OutputStream outputStream) throws IOException {
        JsonNode documento = foGerador.validar(json);
        FoGerado fo = foGerador.gerar(json, documento);
        byte[] docx = DocumentoArticuladoFoDocx.gerar(fo.xslFo, fo.parametros);

        outputStream.write(docx);
        outputStream.flush();
    }

}
