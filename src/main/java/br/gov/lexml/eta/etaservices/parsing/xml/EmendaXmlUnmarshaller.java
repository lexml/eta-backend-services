package br.gov.lexml.eta.etaservices.parsing.xml;

import br.gov.lexml.eta.etaservices.emenda.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda.EMENDA;

public class EmendaXmlUnmarshaller {
    public Emenda fromXml(final String xml) throws DocumentException {

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new StringReader(xml));

        return parseEmenda(doc.getRootElement());
    }

    private Emenda parseEmenda(final Element rootElement) {

        final AtributosEmenda atributosEmenda = parseAtributosEmenda(rootElement);
        final Metadados metadados = parseMetadados(rootElement);
        final RefProposicaoEmendada proposicao = parseProposicao(rootElement);
        final ColegiadoApreciador colegiadoApreciador = parseColegiado(rootElement);
        final Epigrafe epigrafe = parseEpigrafe(rootElement);
        final List<? extends ComponenteEmendado> componentes = parseComponentes(rootElement);

        return new EmendaRecord(
                metadados.getDataUltimaModificacao(),
                metadados.getAplicacao(),
                metadados.getVersaoAplicacao(),
                metadados.getModoEdicao(),
                metadados.getMeta(),
                proposicao,
                colegiadoApreciador,
                epigrafe,
                componentes,
                null,
                "",
                atributosEmenda.getLocal(),
                atributosEmenda.getData(),
                null,
                null
        );
    }

    private List<? extends ComponenteEmendado> parseComponentes(Element rootElement) {
        List<Node> nodes = rootElement.selectNodes("/Componente");

        return nodes.stream().map(this::parseComponente).collect(Collectors.toList());
    }

    private AtributosEmenda parseAtributosEmenda(final Element rootElement) {
        final String local = rootElement.attributeValue("local");
        final String dataAttribute = rootElement.attributeValue("data");
        final LocalDate data = LocalDate.parse(dataAttribute);
        return new AtributosEmenda(local, data);
    }

    private Metadados parseMetadados(final Element rootElement) {
        final Node metadadosNode = rootElement.selectSingleNode("/Metadados");
        List<Node> metadados = metadadosNode.selectNodes("");
        Instant dataUltimaModificacao = Instant.now();
        String aplicacao = "";
        String versaoAplicacao = "";
        ModoEdicaoEmenda modoEdicao = EMENDA;
        final Map<String, Object> meta = new LinkedHashMap<>();
        for (Node n : metadados) {
            switch (n.getName()) {
                case "DataUltimaModificacao":
                    String dt = n.getStringValue();
                    dataUltimaModificacao = Instant.parse(dt);
                    break;
                case "Aplicacao":
                    aplicacao = n.getStringValue();
                    break;
                case "VersaoAplicacao":
                    versaoAplicacao = n.getStringValue();
                    break;
                case "ModoEdicao":
                    String me = n.getStringValue();
                    modoEdicao = ModoEdicaoEmenda.parse(me);
                    break;
                default:
                    meta.put(n.getName(), n.getStringValue());
                    break;
            }

        }
        return new Metadados(dataUltimaModificacao, aplicacao, versaoAplicacao, modoEdicao, meta);
    }

    private RefProposicaoEmendada parseProposicao(final Element rootElement) {
        final Node proposicao = rootElement.selectSingleNode("/Proposicao");
        final String urn = proposicao.valueOf("/@urn");
        final String sigla = proposicao.valueOf("/@sigla");
        final String numero = proposicao.valueOf("/@numero");
        final String ano = proposicao.valueOf("/@ano");
        final String ementa = proposicao.valueOf("/@ementa");
        final String identificacaoTexto = proposicao.valueOf("/@identificacaoTexto");


        return new RefProposicaoEmendadaRecord(
                urn,
                sigla,
                numero,
                ano,
                ementa,
                identificacaoTexto);
    }

    private ColegiadoApreciador parseColegiado(final Element rootElement) {
        final Node colegiado =
                rootElement.selectSingleNode("/ColegiadoApreciador");
        final SiglaCasaLegislativa sigla =
                SiglaCasaLegislativa.parse(colegiado.valueOf("/@siglaCasaLegislativa"));
        final TipoColegiado tipoColegiado =
                TipoColegiado.parse(colegiado.valueOf("/@tipoColegiado"));
        final String siglaComissao = colegiado.valueOf("/@siglaComissao");

        return new ColegiadoApreciadorRecord(sigla, tipoColegiado, siglaComissao);
    }

    private Epigrafe parseEpigrafe(final Element rootElement) {
        final Node epigrafe =
                rootElement.selectSingleNode("/Epigrafe");
        final String texto = epigrafe.valueOf("/@texto");
        final String complemento = epigrafe.valueOf("/@complemento");
        
        return new EpigrafeRecord(
                texto,
                complemento);
    }

    private ComponenteEmendado parseComponente(final Node componente) {

        final String urn = componente.valueOf("/@urn");
        final boolean articulado =
                componente.valueOf("/@articulado").equals("true");
        final String tituloAnexo =
                componente.valueOf("/@tituloAnexo");
        final String rotuloAnexo =
                componente.valueOf("/@rotuloAnexo");

        final DispositivosEmendaRecord dispositivos = parseDispositivos(componente);

        return new ComponenteEmendadoRecord(
                urn,
                articulado,
                rotuloAnexo,
                tituloAnexo,
                dispositivos);
    }

    private DispositivosEmendaRecord parseDispositivos(final Node componente) {
        final Node dispositivos = componente.selectSingleNode("/Dispositivos");

        return new DispositivosEmendaRecord(
                null,
                null,
                null);
    }

}
