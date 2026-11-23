package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.util.Locale;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Formatos de arquivo aceitos na importação de proposições para o documento articulado.
 */
public enum TipoArquivoProposicao {

    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    TXT("text/plain", "txt"),
    XML("application/xml", "xml"),
    PDF("application/pdf", "pdf");

    private final String contentType;
    private final String extensao;

    TipoArquivoProposicao(String contentType, String extensao) {
        this.contentType = contentType;
        this.extensao = extensao;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtensao() {
        return extensao;
    }

    /**
     * Resolve o tipo de arquivo pelo cabeçalho {@code Content-Type} HTTP, ignorando parâmetros como
     * {@code ; charset=utf-8}. Aceita também {@code text/xml} para {@link #XML}.
     *
     * @throws EtaBackendException se o tipo for nulo, vazio ou não suportado
     */
    public static TipoArquivoProposicao porContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new EtaBackendException("Tipo de arquivo (Content-Type) não informado.");
        }
        String mime = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (mime.isEmpty()) {
            throw new EtaBackendException("Tipo de arquivo (Content-Type) não informado.");
        }
        if ("text/xml".equals(mime)) {
            return XML;
        }
        for (TipoArquivoProposicao tipo : values()) {
            if (tipo.contentType.equals(mime)) {
                return tipo;
            }
        }
        throw new EtaBackendException("Tipo de arquivo não suportado: " + mime);
    }

    /**
     * Resolve o tipo de arquivo pelo nome completo do arquivo ou pela extensão (com ou sem ponto).
     *
     * @throws EtaBackendException se o nome/extensão for nulo, vazio ou não suportado
     */
    public static TipoArquivoProposicao porExtensao(String nomeArquivoOuExtensao) {
        if (nomeArquivoOuExtensao == null || nomeArquivoOuExtensao.trim().isEmpty()) {
            throw new EtaBackendException("Extensão de arquivo não informada.");
        }
        String limpo = nomeArquivoOuExtensao.trim();
        int idx = limpo.lastIndexOf('.');
        String ext = (idx >= 0 ? limpo.substring(idx + 1) : limpo).trim().toLowerCase(Locale.ROOT);
        if (ext.isEmpty()) {
            throw new EtaBackendException("Extensão de arquivo não informada.");
        }
        for (TipoArquivoProposicao tipo : values()) {
            if (tipo.extensao.equals(ext)) {
                return tipo;
            }
        }
        throw new EtaBackendException("Extensão de arquivo não suportada: " + ext);
    }

}

