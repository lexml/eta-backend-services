package br.gov.lexml.eta.etaservices.documentoarticulado.conversor;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;

/**
 * Converte o documento articulado entre LexML JSON (formato jsonix usado pelo lexml-eta, o
 * documento-articulado.json) e LexML XML (documento-articulado.xml).
 * <p>
 * Os dois sentidos devem preservar o documento: {@code xmlParaJson(jsonParaXml(json))} é
 * semanticamente igual a {@code json}, podendo diferir apenas na ordem das chaves.
 */
public interface ConversorDocumentoArticulado {

    /**
     * @param json conteúdo do documento-articulado.json
     * @return o LexML XML equivalente
     * @throws EtaBackendException se a conversão falhar, com a mensagem de erro do conversor
     */
    String jsonParaXml(String json);

    /**
     * @param xml conteúdo do documento-articulado.xml
     * @return o LexML JSON (jsonix) equivalente
     * @throws EtaBackendException se a conversão falhar, com a mensagem de erro do conversor
     */
    String xmlParaJson(String xml);

}
