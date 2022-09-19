package br.gov.lexml.eta.etaservices.parsing.xml;

import static br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda.EMENDA;

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import br.gov.lexml.eta.etaservices.emenda.Autoria;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoApreciador;
import br.gov.lexml.eta.etaservices.emenda.ColegiadoAutor;
import br.gov.lexml.eta.etaservices.emenda.ComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ComponenteEmendado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaAdicionado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaModificado;
import br.gov.lexml.eta.etaservices.emenda.DispositivoEmendaSuprimido;
import br.gov.lexml.eta.etaservices.emenda.Emenda;
import br.gov.lexml.eta.etaservices.emenda.Epigrafe;
import br.gov.lexml.eta.etaservices.emenda.ItemComandoEmenda;
import br.gov.lexml.eta.etaservices.emenda.ModoEdicaoEmenda;
import br.gov.lexml.eta.etaservices.emenda.NotaAlteracao;
import br.gov.lexml.eta.etaservices.emenda.OpcoesImpressao;
import br.gov.lexml.eta.etaservices.emenda.Parlamentar;
import br.gov.lexml.eta.etaservices.emenda.RefProposicaoEmendada;
import br.gov.lexml.eta.etaservices.emenda.Sexo;
import br.gov.lexml.eta.etaservices.emenda.SiglaCasaLegislativa;
import br.gov.lexml.eta.etaservices.emenda.TipoAutoria;
import br.gov.lexml.eta.etaservices.emenda.TipoColegiado;

public class EmendaXmlUnmarshaller {
    public Emenda fromXml(final String xml) throws DocumentException {

        final SAXReader reader = new SAXReader();
        final Document doc = reader.read(new StringReader(xml));

        return parseEmenda(doc.getRootElement());
    }

    private Emenda parseEmenda(final Element rootElement) {

        final AtributosEmenda atributosEmenda = parseAtributosEmenda(rootElement);
        final Metadados metadados = parseMetadados(rootElement);
        final RefProposicaoEmendada proposicao = parseProposicao(rootElement);
        final ColegiadoApreciador colegiadoApreciador = parseColegiado(rootElement);
        final Epigrafe epigrafe = parseEpigrafe(rootElement);
        final List<? extends ComponenteEmendado> componentes = parseComponentes(rootElement);
        final ComandoEmenda comandoEmenda = parseComandoEmenda(rootElement);
        final String justificativa = parseJustificativa(rootElement);
        final Autoria autoria = parseAutoria(rootElement);
        final OpcoesImpressao opcoesImpressao = parseOpcoesImpressao(rootElement);

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
                comandoEmenda,
                justificativa,
                atributosEmenda.getLocal(),
                atributosEmenda.getData(),
                autoria,
                opcoesImpressao);
    }

    private List<? extends ComponenteEmendado> parseComponentes(Element rootElement) {
        final List<Node> nodes = rootElement.selectNodes("Componente");

        return nodes.stream().map(this::parseComponente).collect(Collectors.toList());
    }

    private AtributosEmenda parseAtributosEmenda(final Element rootElement) {
        final String local = rootElement.attributeValue("local");
        final String dataAttribute = rootElement.attributeValue("data");
        final LocalDate data = dataAttribute != null? LocalDate.parse(dataAttribute): null;
        return new AtributosEmenda(local, data);
    }

    private Metadados parseMetadados(final Element rootElement) {
        final  List<Node> metadados = rootElement.selectNodes("Metadados/*");
        Instant dataUltimaModificacao = Instant.now();
        String aplicacao = "";
        String versaoAplicacao = "";
        ModoEdicaoEmenda modoEdicao = EMENDA;
        final Map<String, Object> meta = new LinkedHashMap<>();
        for (Node n : metadados) {
            switch (n.getName()) {
                case "DataUltimaModificacao":
                    String dt = n.getStringValue().trim();
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
        final Element proposicao = (Element) rootElement.selectSingleNode("Proposicao");
        final String urn = proposicao.attributeValue("urn");
        final String sigla = proposicao.attributeValue("sigla");
        final String numero = proposicao.attributeValue("numero");
        final String ano = proposicao.attributeValue("ano");
        final String ementa = proposicao.attributeValue("ementa");
        final String identificacaoTexto = proposicao.attributeValue("identificacaoTexto");


        return new RefProposicaoEmendadaRecord(
                urn,
                sigla,
                numero,
                ano,
                ementa,
                identificacaoTexto);
    }

    private ColegiadoApreciador parseColegiado(final Element rootElement) {
        final Element colegiado =
                (Element) rootElement.selectSingleNode("ColegiadoApreciador");
        final SiglaCasaLegislativa sigla =
                SiglaCasaLegislativa.parse(colegiado.attributeValue("siglaCasaLegislativa"));
        final TipoColegiado tipoColegiado =
                TipoColegiado.parse(colegiado.attributeValue("tipoColegiado"));
        final String siglaComissao = colegiado.attributeValue("siglaComissao");

        return new ColegiadoApreciadorRecord(sigla, tipoColegiado, siglaComissao);
    }

    private Epigrafe parseEpigrafe(final Element rootElement) {
        final Element epigrafe =
                (Element) rootElement.selectSingleNode("Epigrafe");
        final String texto = epigrafe.attributeValue("texto");
        final String complemento = epigrafe.attributeValue("complemento");
        
        return new EpigrafeRecord(
                texto,
                complemento);
    }

    private ComponenteEmendado parseComponente(final Node nodeComponente) {
    	
    	final Element componente = (Element)nodeComponente;

        final String urn = componente.attributeValue("urn");
        final boolean articulado =
                booleanAttributeValue(componente.attributeValue("articulado"));
        final String tituloAnexo =
                componente.attributeValue("tituloAnexo");
        final String rotuloAnexo =
                componente.attributeValue("rotuloAnexo");

        final DispositivosEmendaRecord dispositivos = parseDispositivos(componente);

        return new ComponenteEmendadoRecord(
                urn,
                articulado,
                rotuloAnexo,
                tituloAnexo,
                dispositivos);
    }

    private DispositivosEmendaRecord parseDispositivos(final Node componente) {
        final Node dispositivos = componente.selectSingleNode("Dispositivos");

        final List<? extends DispositivoEmendaSuprimido> suprimidos = parseSuprimidos(dispositivos);
        final List<? extends DispositivoEmendaModificado> modificados = parseModificados(dispositivos);
        final List<? extends DispositivoEmendaAdicionado> adicionados = parseAdicionados(dispositivos);

        return new DispositivosEmendaRecord(
                suprimidos,
                modificados,
                adicionados);
    }

    private List<? extends DispositivoEmendaSuprimido> parseSuprimidos(final Node dispositivos) {
        return dispositivos.selectNodes("DispositivoSuprimido").stream()
                .map(this::parseSuprimido)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaSuprimido parseSuprimido(final Node nodeSuprimido) {
    	final Element suprimido = (Element)nodeSuprimido; 
        final String tipo = suprimido.attributeValue("tipo");
        final String id = suprimido.attributeValue("id");
        final String rotulo = suprimido.attributeValue("rotulo");
        return new DispositivoEmendaSuprimidoRecord(tipo, id, rotulo);
    }

    private List<? extends DispositivoEmendaModificado> parseModificados(final Node dispositivos) {
        return dispositivos.selectNodes("DispositivoModificado").stream()
                .map(this::parseModificado)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaModificado parseModificado(final Node nodeModificado) {
    	final Element modificado = (Element)nodeModificado;
        final String tipo = modificado.attributeValue("tipo");
        final String id = modificado.attributeValue("id");
        final String rotulo = modificado.attributeValue("rotulo");
        final Boolean textoOmitido = booleanAttributeValue(modificado.attributeValue("textoOmitido"));
        final Boolean abreAspas = booleanAttributeValue(modificado.attributeValue("abreAspas"));
        final Boolean fechaAspas = booleanAttributeValue(modificado.attributeValue("fechaAspas"));
        final NotaAlteracao nota = NotaAlteracao.parse(modificado.attributeValue("notaAlteracao"));
        final String texto = nodeContentWithTags(modificado.selectSingleNode("Texto"));

        return new DispositivoEmendaModificadoRecord(
                tipo,
                id,
                rotulo,
                texto,
                textoOmitido,
                abreAspas,
                fechaAspas,
                nota);
    }


    private List<? extends DispositivoEmendaAdicionado> parseAdicionados(final Node dispositivos) {
        return dispositivos.selectNodes("DispositivoAdicionado").stream()
                .map(this::parseAdicionado)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaAdicionadoRecord parseAdicionado(final Node nodeAdicionado) {
    	
    	Element adicionado = (Element) nodeAdicionado;

        boolean ondeCouber = booleanAttributeValue(adicionado.attributeValue("ondeCouber"));
        String idPai = adicionado.attributeValue("idPai");
        String idIrmaoAnterior = adicionado.attributeValue("idIrmaoAnterior");  
        
        Node filho = nodeAdicionado.selectSingleNode("*");
        return parseAdicionadoLexml(filho, ondeCouber, idPai, idIrmaoAnterior);

    }
    
    private DispositivoEmendaAdicionadoRecord parseAdicionadoLexml(final Node nodeAdicionado,
    		boolean ondeCouber, String idPai, String idIrmaoAnterior) {
    	
    	Element adicionado = (Element) nodeAdicionado;
    	
        String tipo = adicionado.getName();
        String id = adicionado.attributeValue("id");
        Boolean textoOmitido = booleanAttributeValue(adicionado.attributeValue("textoOmitido"));
        Boolean abreAspas = booleanAttributeValue(adicionado.attributeValue("abreAspas"));
        Boolean fechaAspas = booleanAttributeValue(adicionado.attributeValue("fechaAspas"));
        NotaAlteracao notaAlteracao = NotaAlteracao.parse(adicionado.attributeValue("notaAlteracao"));
        String urnNormaAlterada = adicionado.attributeValue("base");
        Boolean existeNaNormaAlterada = booleanAttributeValue(adicionado.attributeValue("existeNaNormaAlterada"));
        String rotulo = nodeStringValue(adicionado.selectSingleNode("Rotulo"));
        String texto = nodeContentWithTags(adicionado.selectSingleNode("p"));
        
        List<Node> nodesFilhos = adicionado.selectNodes("*[not(self::Rotulo or self::p)]");
        List<DispositivoEmendaAdicionadoRecord> filhos = nodesFilhos.stream() 
                .map(this::parseAdicionadoLexml)
                .collect(Collectors.toList());
        
        return new DispositivoEmendaAdicionadoRecord(
        		tipo, id, rotulo, texto, 
        		textoOmitido, abreAspas, fechaAspas, notaAlteracao, ondeCouber, 
        		idPai, idIrmaoAnterior, 
        		urnNormaAlterada, existeNaNormaAlterada, filhos);
    }

    private DispositivoEmendaAdicionadoRecord parseAdicionadoLexml(final Node nodeAdicionado) {
    	return parseAdicionadoLexml(nodeAdicionado, false, null, null);
    }
    
	private ComandoEmenda parseComandoEmenda(final Element rootElement) {
        final Node comandoEmenda = rootElement.selectSingleNode("ComandoEmenda");
        final Node cabecalhoComumNode = comandoEmenda.selectSingleNode("CabecalhoComum");
        final String cabecalhoComum = nodeContentWithTags(cabecalhoComumNode);
        final List<ItemComandoEmenda> itensComandoEmenda = parseItensComandoEmenda(comandoEmenda);

        return new ComandoEmendaRecord(cabecalhoComum, itensComandoEmenda);
    }

    private List<ItemComandoEmenda> parseItensComandoEmenda(Node comandoEmenda) {
        return comandoEmenda.selectNodes("ItemComandoEmenda").stream()
                .map(this::parseItem).collect(Collectors.toList());
    }

    private ItemComandoEmenda parseItem(Node itemComandoEmenda) {

        final String rotulo =
                nodeStringValue(itemComandoEmenda.selectSingleNode("Rotulo"));

        final String cabecalho =
        		nodeContentWithTags(itemComandoEmenda.selectSingleNode("Cabecalho"));

        final String citacao =
        		nodeContentWithTags(itemComandoEmenda.selectSingleNode("Citacao"));

        final String complemento =
        		nodeContentWithTags(itemComandoEmenda.selectSingleNode("Complemento"));

        return new ItemComandoEmendaRecord(
                cabecalho,
                citacao,
                rotulo,
                complemento);
    }

    private String parseJustificativa(final Element rootElement) {
        return nodeStringValue(rootElement.selectSingleNode("Justificativa"));
    }

    private Autoria parseAutoria(final Element rootElement) {
        final Element autoria = (Element) rootElement.selectSingleNode("Autoria");

        final TipoAutoria tipo = TipoAutoria.parse(autoria.attributeValue("tipo"));
        final boolean imprimirPartidoUF = booleanAttributeValue(autoria.attributeValue("imprimirPartidoUF"));
        final int quantidadeAssinaturasAdicionaisDeputados =
                Integer.parseInt(autoria.attributeValue("quantidadeAssinaturasAdicionaisDeputados"));
        final int quantidadeAssinaturasAdicionaisSenadores =
                Integer.parseInt(autoria.attributeValue("quantidadeAssinaturasAdicionaisSenadores"));

        final List<Parlamentar> parlamentares = parseParlamentares(autoria);
        final ColegiadoAutor colegiado = parseColegiadoAutor(autoria);


        return new AutoriaRecord(
                tipo,
                imprimirPartidoUF,
                quantidadeAssinaturasAdicionaisSenadores,
                quantidadeAssinaturasAdicionaisDeputados,
                parlamentares,
                colegiado);
    }

    private List<Parlamentar> parseParlamentares(final Node autoria) {
        return autoria.selectNodes("Parlamentar").stream().map(this::parseParlamentar).collect(Collectors.toList());
    }

    private Parlamentar parseParlamentar(Node nodeParlamentar) {
    	final Element parlamentar = (Element) nodeParlamentar;
        final String identificacao = parlamentar.attributeValue("identificacao");
        final String nome = parlamentar.attributeValue("nome");
        final Sexo sexo = Sexo.parse(parlamentar.attributeValue("sexo"));
        final String partido = parlamentar.attributeValue("siglaPartido");
        final String uf = parlamentar.attributeValue("siglaUF");
        final SiglaCasaLegislativa casa =
                SiglaCasaLegislativa.parse(parlamentar.attributeValue("siglaCasaLegislativa"));
        final String cargo = parlamentar.attributeValue("cargo");

        return new ParlamentarRecord(
                identificacao,
                nome,
                sexo,
                partido,
                uf,
                casa,
                cargo);
    }

    private ColegiadoAutor parseColegiadoAutor(Node autoria) {

        final Element colegiado = (Element) autoria.selectSingleNode("Colegiado");
        if (colegiado == null) {
            return null;
        }
        final String identificacao = colegiado.attributeValue("identificacao");
        final String nome = colegiado.attributeValue("nome");
        final String sigla = colegiado.attributeValue("sigla");


        return new ColegiadoAutorRecord(
                identificacao,
                nome,
                sigla);
    }

    private OpcoesImpressao parseOpcoesImpressao(final Element rootElement) {
        Element opcoes = (Element) rootElement.selectSingleNode("OpcoesImpressao");

        boolean imprimirBrasao = booleanAttributeValue(opcoes.attributeValue("imprimirBrasao"));
        String textoCabecalho = opcoes.attributeValue("textoCabecalho");
        boolean reduzirEspacoEntreLinhas = booleanAttributeValue(opcoes.attributeValue("reduzirEspacoEntreLinhas"));

        return new OpcoesImpressaoRecord(
                imprimirBrasao,
                textoCabecalho,
                reduzirEspacoEntreLinhas);
    }

    private boolean booleanAttributeValue(String attributeValue) {
		return attributeValue != null && (attributeValue.equals("true") || attributeValue.equalsIgnoreCase("s"));
	}
    
    private String nodeStringValue(Node node) {
    	return node == null ? null : node.getStringValue(); 
    }

    private String nodeContentWithTags(Node node) {
    	return node == null ? null : node.asXML().trim().replaceAll("^<[^>]+>", "").replaceAll("</[^>]+>$", ""); 
    }

}
