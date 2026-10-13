package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Opções de impressão registradas no documento ({@code lexedit:OpcoesImpressao}, especificação 02 do lexml-eta),
 * lidas uma única vez do documento-articulado.json para o cabeçalho e para os parâmetros de impressão.
 * <p>
 * Opção ausente, ou com tipo inesperado, vale o padrão do editor (classe {@code OpcoesImpressao} do lexml-eta):
 * com brasão, sem texto de cabeçalho, sem reduzir o espaço entre linhas e fonte 14. O tamanho da fonte só aceita
 * os valores oferecidos pelo editor (14, 16 e 18); outro valor vale 14.
 */
final class OpcoesImpressaoDocumentoArticulado {

    static final int TAMANHO_FONTE_PADRAO = 14;

    private static final OpcoesImpressaoDocumentoArticulado PADRAO =
            new OpcoesImpressaoDocumentoArticulado(true, "", false, TAMANHO_FONTE_PADRAO);

    private final boolean imprimirBrasao;
    private final String textoCabecalho;
    private final boolean reduzirEspacoEntreLinhas;
    private final int tamanhoFonte;

    private OpcoesImpressaoDocumentoArticulado(boolean imprimirBrasao, String textoCabecalho,
            boolean reduzirEspacoEntreLinhas, int tamanhoFonte) {
        this.imprimirBrasao = imprimirBrasao;
        this.textoCabecalho = textoCabecalho;
        this.reduzirEspacoEntreLinhas = reduzirEspacoEntreLinhas;
        this.tamanhoFonte = tamanhoFonte;
    }

    /** Opções padrão do editor. */
    static OpcoesImpressaoDocumentoArticulado padrao() {
        return PADRAO;
    }

    /** Opções do documento (documento-articulado.json); padrões do editor para o que faltar. */
    static OpcoesImpressaoDocumentoArticulado de(JsonNode documento) {
        JsonNode opcoes = no(documento);
        JsonNode brasao = opcoes.path("imprimirBrasao");
        JsonNode texto = opcoes.path("textoCabecalho");
        JsonNode reduzir = opcoes.path("reduzirEspacoEntreLinhas");
        JsonNode fonte = opcoes.path("tamanhoFonte");
        return new OpcoesImpressaoDocumentoArticulado(
                brasao.isBoolean() ? brasao.booleanValue() : PADRAO.imprimirBrasao,
                texto.isTextual() ? texto.textValue().trim() : PADRAO.textoCabecalho,
                reduzir.isBoolean() ? reduzir.booleanValue() : PADRAO.reduzirEspacoEntreLinhas,
                fonte.isIntegralNumber() && tamanhoDoEditor(fonte.intValue()) ? fonte.intValue() : PADRAO.tamanhoFonte);
    }

    private static boolean tamanhoDoEditor(int tamanho) {
        return tamanho == 14 || tamanho == 16 || tamanho == 18;
    }

    /** Nó {@code opcoesImpressao} do {@code lexedit:Metadado}; nó ausente quando o documento não o tem. */
    private static JsonNode no(JsonNode documento) {
        for (JsonNode proprietario : documento.path("value").path("metadado").path("metadadoProprietario")) {
            for (JsonNode metadado : proprietario.path("any")) {
                JsonNode opcoes = metadado.path("value").path("opcoesImpressao");
                if (opcoes.isObject()) {
                    return opcoes;
                }
            }
        }
        return documento.path("ausente");
    }

    boolean isImprimirBrasao() {
        return imprimirBrasao;
    }

    /** Texto do cabeçalho sem espaços nas pontas; vazio quando não informado. */
    String getTextoCabecalho() {
        return textoCabecalho;
    }

    boolean isReduzirEspacoEntreLinhas() {
        return reduzirEspacoEntreLinhas;
    }

    /** 14, 16 ou 18. */
    int getTamanhoFonte() {
        return tamanhoFonte;
    }

}
