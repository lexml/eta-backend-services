package br.gov.lexml.eta.etaservices.documentoarticulado;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Conversor para testes, sem o executável jsonix-lexml: devolve pares JSON <-> XML registrados.
 * O JSON é comparado pelo conteúdo (ignora ordem de chaves e formatação); o XML, pelo texto exato.
 */
public class ConversorDocumentoArticuladoFake implements ConversorDocumentoArticulado {

    public static final String JSON_EXEMPLO = "/documentoarticulado/documento-articulado-exemplo.json";
    public static final String XML_EXEMPLO = "/documentoarticulado/documento-articulado-exemplo.xml";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<JsonNode, String> xmlPorJson = new LinkedHashMap<>();
    private final Map<String, String> jsonPorXml = new LinkedHashMap<>();
    private String mensagemFalha;

    /** Conversor com o par de exemplo (documento-articulado-exemplo.json/.xml) registrado. */
    public static ConversorDocumentoArticuladoFake comExemplo() {
        ConversorDocumentoArticuladoFake fake = new ConversorDocumentoArticuladoFake();
        fake.registrar(recurso(JSON_EXEMPLO), recurso(XML_EXEMPLO));
        return fake;
    }

    public ConversorDocumentoArticuladoFake registrar(String json, String xml) {
        xmlPorJson.put(lerJson(json), xml);
        jsonPorXml.put(xml, json);
        return this;
    }

    /** A partir daqui, toda conversão falha com a mensagem informada. */
    public ConversorDocumentoArticuladoFake falharCom(String mensagem) {
        this.mensagemFalha = mensagem;
        return this;
    }

    @Override
    public String jsonParaXml(String json) {
        verificarFalha();
        String xml = xmlPorJson.get(lerJson(json));
        if (xml == null) {
            throw new EtaBackendException("JSON não registrado no conversor fake.");
        }
        return xml;
    }

    @Override
    public String xmlParaJson(String xml) {
        verificarFalha();
        String json = jsonPorXml.get(xml);
        if (json == null) {
            throw new EtaBackendException("XML não registrado no conversor fake.");
        }
        return json;
    }

    private void verificarFalha() {
        if (mensagemFalha != null) {
            throw new EtaBackendException(mensagemFalha);
        }
    }

    private static JsonNode lerJson(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new EtaBackendException("JSON inválido no conversor fake.", e);
        }
    }

    public static String recurso(String caminho) {
        try (InputStream in = ConversorDocumentoArticuladoFake.class.getResourceAsStream(caminho)) {
            if (in == null) {
                throw new IllegalArgumentException("Recurso não encontrado: " + caminho);
            }
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
