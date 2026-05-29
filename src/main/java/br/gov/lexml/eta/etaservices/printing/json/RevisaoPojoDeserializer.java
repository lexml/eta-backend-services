package br.gov.lexml.eta.etaservices.printing.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import br.gov.lexml.eta.etaservices.parecer.RevisaoAnalise;
import br.gov.lexml.eta.etaservices.parecer.RevisaoEmenta;
import br.gov.lexml.eta.etaservices.parecer.RevisaoRelatorio;
import br.gov.lexml.eta.etaservices.parecer.RevisaoVoto;

public class RevisaoPojoDeserializer extends JsonDeserializer<RevisaoPojo> {

    private static final String REVISAO_ELEMENTO = "RevisaoElemento";
    private static final String REVISAO_JUSTIFICATIVA = "RevisaoJustificativa";
    private static final String REVISAO_TEXTO_LIVRE = "RevisaoTextoLivre";
    private static final String REVISAO_EMENTA = "RevisaoEmenta";
    private static final String REVISAO_RELATORIO = "RevisaoRelatorio";
    private static final String REVISAO_ANALISE = "RevisaoAnalise";
    private static final String REVISAO_VOTO = "RevisaoVoto";

    @Override
    public RevisaoPojo deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);
        return codec.treeToValue(node, getClasseRevisao(node));
    }

    private Class<? extends RevisaoPojo> getClasseRevisao(JsonNode node) {
        String type = textValue(node.get("type"));

        switch(type) {
            case REVISAO_ELEMENTO:
                return RevisaoElementoPojo.class;
            case REVISAO_TEXTO_LIVRE:
                return RevisaoTextoLivrePojo.class;
            case REVISAO_EMENTA:
                return RevisaoEmenta.class;
            case REVISAO_RELATORIO:
                return RevisaoRelatorio.class;
            case REVISAO_ANALISE:
                return RevisaoAnalise.class;
            case REVISAO_VOTO:
                return RevisaoVoto.class;
            case REVISAO_JUSTIFICATIVA:
            default:
                return getClasseRevisaoSemType(node);
        }
    }

    private Class<? extends RevisaoPojo> getClasseRevisaoSemType(JsonNode node) {
        if(isRevisaoElemento(node)) {
            return RevisaoElementoPojo.class;
        }
        if(REVISAO_TEXTO_LIVRE.equals(getTipoPorDescricao(node))) {
            return RevisaoTextoLivrePojo.class;
        }
        return RevisaoJustificativaPojo.class;
    }

    private boolean isRevisaoElemento(JsonNode node) {
        return node.has("actionType")
                || node.has("stateType")
                || node.has("elementoAntesRevisao")
                || node.has("elementoAposRevisao");
    }

    private String getTipoPorDescricao(JsonNode node) {
        String descricao = textValue(node.get("descricao"));
        return "Texto Livre Alterado".equals(descricao) ? REVISAO_TEXTO_LIVRE : "";
    }

    private String textValue(JsonNode node) {
        return node == null || node.isNull() ? "" : node.asText("");
    }
}
