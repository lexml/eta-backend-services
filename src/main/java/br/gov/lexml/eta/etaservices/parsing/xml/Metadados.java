package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda;

import java.time.Instant;
import java.util.Map;

class Metadados {
    private final Instant dataUltimaModificacao;
    private final String aplicacao;
    private final String versaoAplicacao;
    private final ModoEdicaoEmenda modoEdicao;
    private final Map<String, Object> meta;

    public Metadados(Instant dataUltimaModificacao, String aplicacao, String versaoAplicacao, ModoEdicaoEmenda modoEdicao, Map<String, Object> meta) {
        this.dataUltimaModificacao = dataUltimaModificacao;
        this.aplicacao = aplicacao;
        this.versaoAplicacao = versaoAplicacao;
        this.modoEdicao = modoEdicao;
        this.meta = meta;
    }

    public Instant getDataUltimaModificacao() {
        return dataUltimaModificacao;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public String getVersaoAplicacao() {
        return versaoAplicacao;
    }

    public ModoEdicaoEmenda getModoEdicao() {
        return modoEdicao;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
