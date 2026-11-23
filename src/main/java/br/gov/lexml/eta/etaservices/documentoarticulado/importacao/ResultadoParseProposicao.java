package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Resultado do parseamento ou extração de uma proposição para o formato Jsonix do {@code lexml-eta}.
 */
public final class ResultadoParseProposicao {

    private final String jsonix;
    private final String xml;
    private final List<AvisoParseProposicao> avisos;

    public ResultadoParseProposicao(String jsonix, String xml, List<AvisoParseProposicao> avisos) {
        this.jsonix = Objects.requireNonNull(jsonix, "jsonix");
        this.xml = Objects.requireNonNull(xml, "xml");
        this.avisos = avisos == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(avisos));
    }

    public String getJsonix() {
        return jsonix;
    }

    public String getXml() {
        return xml;
    }

    public List<AvisoParseProposicao> getAvisos() {
        return avisos;
    }

    public boolean temAvisos() {
        return !avisos.isEmpty();
    }

    public List<String> getMensagensAvisos() {
        return avisos.stream()
                .map(AvisoParseProposicao::toString)
                .collect(Collectors.toUnmodifiableList());
    }

}

