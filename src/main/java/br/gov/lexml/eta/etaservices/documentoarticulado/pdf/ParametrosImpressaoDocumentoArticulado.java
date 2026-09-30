package br.gov.lexml.eta.etaservices.documentoarticulado.pdf;

/**
 * Tamanhos de fonte e espaçamentos usados na impressão do documento articulado, passados ao template
 * Velocity e à XSLT do conteúdo.
 * <p>
 * Regras de cálculo copiadas do template da emenda ({@code template-velocity-emenda.xml}):
 * <ul>
 * <li>{@code maxTamanhoFonte}: 18pt se {@code tamanhoFonte} = 18; senão 16pt;</li>
 * <li>{@code lineHeight}: 120% se "reduzir espaço entre linhas"; senão 150%;</li>
 * <li>{@code pMarginBottom}: 0.45em se "reduzir espaço entre linhas"; senão 0.6em.</li>
 * </ul>
 * Por enquanto só há os valores padrão; a leitura das opções de impressão do documento
 * ({@code lexedit:OpcoesImpressao}) fica para uma etapa posterior.
 */
final class ParametrosImpressaoDocumentoArticulado {

    private static final int TAMANHO_FONTE_PADRAO = 14;

    private static final ParametrosImpressaoDocumentoArticulado PADRAO =
            new ParametrosImpressaoDocumentoArticulado(TAMANHO_FONTE_PADRAO, false);

    private final int tamanhoFonte;
    private final String maxTamanhoFonte;
    private final String lineHeight;
    private final String pMarginBottom;

    private ParametrosImpressaoDocumentoArticulado(int tamanhoFonte, boolean reduzirEspacoEntreLinhas) {
        this.tamanhoFonte = tamanhoFonte;
        this.maxTamanhoFonte = tamanhoFonte == 18 ? "18pt" : "16pt";
        this.lineHeight = reduzirEspacoEntreLinhas ? "120%" : "150%";
        this.pMarginBottom = reduzirEspacoEntreLinhas ? "0.45em" : "0.6em";
    }

    /** Valores padrão: fonte 14, destaque 16pt, entrelinha 150% e espaço entre parágrafos 0.6em. */
    static ParametrosImpressaoDocumentoArticulado padrao() {
        return PADRAO;
    }

    /** Tamanho da fonte do texto, em pontos. */
    int getTamanhoFonte() {
        return tamanhoFonte;
    }

    /** Tamanho da fonte de destaque (epígrafe), com unidade. */
    String getMaxTamanhoFonte() {
        return maxTamanhoFonte;
    }

    /** Entrelinha, com unidade. */
    String getLineHeight() {
        return lineHeight;
    }

    /** Espaço após cada parágrafo, com unidade. */
    String getPMarginBottom() {
        return pMarginBottom;
    }

}
