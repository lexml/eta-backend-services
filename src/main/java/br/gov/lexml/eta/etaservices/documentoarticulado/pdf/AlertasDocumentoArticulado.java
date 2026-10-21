package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Dados da caixa de pendências impressa no início do PDF (issue #79), lidos do {@code lexedit:Metadado} do
 * documento-articulado.json (especificação 13 do lexml-eta).
 * <p>
 * As pendências são impressas como foram registradas pelo editor, sem filtro nem reescrita. Revisões e comentários
 * não têm caixa própria: o aviso deles é a pendência registrada pelo editor (avaliação da chefia).
 */
final class AlertasDocumentoArticulado {

    private final List<String> pendencias;

    private AlertasDocumentoArticulado(List<String> pendencias) {
        this.pendencias = Collections.unmodifiableList(pendencias);
    }

    /** Alertas do documento; nenhum quando o documento não tem os dados. */
    static AlertasDocumentoArticulado de(JsonNode documento) {
        return new AlertasDocumentoArticulado(pendencias(FechoDocumentoArticulado.metadadoLexedit(documento).path("pendencias")));
    }

    /** Textos das pendências, na ordem e sem alteração; só os itens sem texto são descartados. */
    private static List<String> pendencias(JsonNode pendencias) {
        List<String> textos = new ArrayList<>();
        JsonNode itens = pendencias.path("pendencia");
        if (itens.isArray()) {
            for (JsonNode item : itens) {
                if (item.isTextual() && !item.textValue().trim().isEmpty()) {
                    textos.add(item.textValue());
                }
            }
        }
        return textos;
    }

    /** Pendências como registradas (caixa de pendências); vazia sem pendências. */
    List<String> getPendencias() {
        return pendencias;
    }

}
