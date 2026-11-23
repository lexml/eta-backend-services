package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoFoGerador.FoGerado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Gera, sob demanda, o RTF editável de uma proposição a partir do documento-articulado.json, com o mesmo conteúdo
 * textual do PDF e sem garantia de fidelidade visual.
 * <p>
 * O RTF só tem o texto: não embute o documento-articulado.xml nem anexos (as páginas dos anexos do PDF não são
 * concatenadas) e não permite recuperar o JSON.
 */
public class DocumentoArticuladoRtfGenerator {

    private final DocumentoArticuladoFoGerador foGerador;
    private final DocumentoArticuladoFopProcessor fopProcessor = new DocumentoArticuladoFopProcessor();

    public DocumentoArticuladoRtfGenerator(ConversorDocumentoArticulado conversor) {
        this.foGerador = new DocumentoArticuladoFoGerador(Objects.requireNonNull(conversor, "conversor"));
    }

    /**
     * @param json conteúdo do documento-articulado.json
     * @param outputStream destino do RTF; nada é escrito se a geração falhar
     * @throws EtaBackendException se o JSON não for um documento LexML ou se a conversão/geração falhar
     * @throws IOException se houver erro ao escrever no destino
     */
    public void generate(String json, OutputStream outputStream) throws IOException {
        JsonNode documento = foGerador.validar(json);
        FoGerado fo = foGerador.gerar(json, documento);
        byte[] rtf = fopProcessor.gerarRtf(fo.xslFo, fo.parametros);

        outputStream.write(rtf);
        outputStream.flush();
    }

}
