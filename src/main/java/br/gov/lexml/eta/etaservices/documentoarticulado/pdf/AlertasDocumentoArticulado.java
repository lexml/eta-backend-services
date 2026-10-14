package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Dados dos alertas impressos no início do PDF (issue #79), lidos do {@code lexedit:Metadado} do
 * documento-articulado.json (especificações 08, 09, 11 e 13 do lexml-eta), com as condições da emenda
 * ({@code Emenda.isPossuiMarcasRevisao()}, {@code Emenda.isPossuiComentarios()} e as pendências de preenchimento).
 * <p>
 * As pendências são impressas como foram registradas pelo editor, sem filtro nem reescrita, mesmo quando repetem o
 * aviso de revisões ou de comentários.
 */
final class AlertasDocumentoArticulado {

    private final boolean possuiRevisoes;
    private final boolean possuiComentarios;
    private final List<String> pendencias;

    private AlertasDocumentoArticulado(boolean possuiRevisoes, boolean possuiComentarios, List<String> pendencias) {
        this.possuiRevisoes = possuiRevisoes;
        this.possuiComentarios = possuiComentarios;
        this.pendencias = Collections.unmodifiableList(pendencias);
    }

    /** Alertas do documento; nenhum quando o documento não tem os dados. */
    static AlertasDocumentoArticulado de(JsonNode documento) {
        JsonNode metadado = FechoDocumentoArticulado.metadadoLexedit(documento);
        return new AlertasDocumentoArticulado(
                temItens(metadado.path("revisoesTextuais").path("revisaoTextual"))
                        || temItens(metadado.path("revisoesArticulacao").path("revisaoArticulacao")),
                possuiComentarios(metadado.path("comentarios").path("sequenciaComentario")), pendencias(metadado.path("pendencias")));
    }

    private static boolean temItens(JsonNode lista) {
        return lista.isArray() && lista.size() > 0;
    }

    /** Como na emenda: alguma sequência com ao menos um comentário. */
    private static boolean possuiComentarios(JsonNode sequencias) {
        if (sequencias.isArray()) {
            for (JsonNode sequencia : sequencias) {
                if (temItens(sequencia.path("comentario"))) {
                    return true;
                }
            }
        }
        return false;
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

    /** Se há revisões de texto ou da articulação (caixa "marcas de revisão"). */
    boolean isPossuiRevisoes() {
        return possuiRevisoes;
    }

    /** Se há ao menos um comentário (caixa "comentários"). */
    boolean isPossuiComentarios() {
        return possuiComentarios;
    }

    /** Pendências como registradas (caixa "pendências de preenchimento"); vazia sem pendências. */
    List<String> getPendencias() {
        return pendencias;
    }

}
