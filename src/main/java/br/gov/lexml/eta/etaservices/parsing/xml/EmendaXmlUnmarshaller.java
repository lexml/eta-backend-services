package br.gov.lexml.eta.etaservices.parsing.xml;

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
        final List<Node> nodes = rootElement.selectNodes("/Componente");

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
        final  List<Node> metadados = metadadosNode.selectNodes("");
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

        final List<? extends DispositivoEmendaSuprimido> suprimidos = parseSuprimidos(dispositivos);
        final List<? extends DispositivoEmendaModificado> modificados = parseModificados(dispositivos);
        final List<? extends DispositivoEmendaAdicionado> adicionados = parseAdicionados(dispositivos);

        return new DispositivosEmendaRecord(
                suprimidos,
                modificados,
                adicionados);
    }

    private List<? extends DispositivoEmendaSuprimido> parseSuprimidos(final Node dispositivos) {
        return dispositivos.selectNodes("/DispositivoSuprimido").stream()
                .map(this::parseSuprimido)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaSuprimido parseSuprimido(final Node suprimido) {
        final String tipo = suprimido.valueOf("/@tipo");
        final String id = suprimido.valueOf("/@id");
        final String rotulo = suprimido.valueOf("/@rotulo");
        return new DispositivoEmendaSuprimidoRecord(tipo, id, rotulo);
    }

    private List<? extends DispositivoEmendaModificado> parseModificados(final Node dispositivos) {
        return dispositivos.selectNodes("/DispositivoModificado").stream()
                .map(this::parseModificado)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaModificado parseModificado(final Node modificado) {
        final String tipo = modificado.valueOf("/@tipo");
        final String id = modificado.valueOf("/@id");
        final String rotulo = modificado.valueOf("/@rotulo");
        final String omitido = modificado.valueOf("/@textoOmitido");
        final Boolean textoOmitido = omitido == null ? null : omitido.equals("true");
        final String aAspas = modificado.valueOf("/@abreAspas");
        final Boolean abreAspas = aAspas == null ? null : aAspas.equals("true");
        final String fAspas = modificado.valueOf("/@abreAspas");
        final Boolean fechaAspas = fAspas == null ? null : fAspas.equals("true");
        final NotaAlteracao nota = NotaAlteracao.parse(modificado.valueOf("/@notaAlteracao"));
        final Node txt = modificado.selectSingleNode("/Texto");
        final String texto = txt.getStringValue();

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
        return dispositivos.selectNodes("/DispositivoAdicionado").stream()
                .map(this::parseAdicionado)
                .collect(Collectors.toList());
    }

    private DispositivoEmendaAdicionado parseAdicionado(final Node adicionado) {

        boolean ondeCouber = adicionado.valueOf("/@ondeCouber").equals("true");
        String idPai = adicionado.valueOf("/@idPai");
        String idIrmao = adicionado.valueOf("/@idIrmaoAnterior");

        

        String tipo = adicionado.valueOf("/@tipo");
        String id = adicionado.valueOf("/@id");
        String rotulo = adicionado.valueOf("/@rotulo");
        String omitido = adicionado.valueOf("/@textoOmitido");
        Boolean textoOmitido = omitido == null ? null : omitido.equals("true");
        String aAspas = adicionado.valueOf("/@abreAspas");
        Boolean abreAspas = aAspas == null ? null : aAspas.equals("true");
        String fAspas = adicionado.valueOf("/@abreAspas");
        Boolean fechaAspas = fAspas == null ? null : fAspas.equals("true");
        NotaAlteracao nota = NotaAlteracao.parse(adicionado.valueOf("/@notaAlteracao"));
        return null;
    }

    private ComandoEmenda parseComandoEmenda(final Element rootElement) {
        final Node comandoEmenda = rootElement.selectSingleNode("/ComandoEmenda");
        final Node cabecalhoComumNode = comandoEmenda.selectSingleNode("/CabecalhoComum");
        final String cabecalhoComum = cabecalhoComumNode == null ? null : cabecalhoComumNode.getStringValue();
        final List<ItemComandoEmenda> itensComandoEmenda = parseItensComandoEmenda(comandoEmenda);

        return new ComandoEmendaRecord(cabecalhoComum, itensComandoEmenda);
    }

    private List<ItemComandoEmenda> parseItensComandoEmenda(Node comandoEmenda) {
        return comandoEmenda.selectNodes("/ItemComandoEmenda").stream()
                .map(this::parseItem).collect(Collectors.toList());
    }

    private ItemComandoEmenda parseItem(Node itemComandoEmenda) {

        final String rotulo =
                itemComandoEmenda.selectSingleNode("/Rotulo").getStringValue();

        final String cabecalho =
                itemComandoEmenda.selectSingleNode("/Cabecalho").getStringValue();

        final String citacao =
                itemComandoEmenda.selectSingleNode("/Citacao").getStringValue();

        final String complemento =
                itemComandoEmenda.selectSingleNode("/Complemento").getStringValue();

        return new ItemComandoEmendaRecord(
                cabecalho,
                citacao,
                rotulo,
                complemento);
    }

    private String parseJustificativa(final Element rootElement) {
        return rootElement.selectSingleNode("/Justificativa").getStringValue();
    }

    private Autoria parseAutoria(final Element rootElement) {
        final Node autoria = rootElement.selectSingleNode("/Autoria");

        final TipoAutoria tipo = TipoAutoria.parse(autoria.valueOf("/@tipo"));
        final boolean imprimirPartidoUF = autoria.valueOf("/@imprimirPartidoUF").equals("true");
        final int quantidadeAssinaturasAdicionaisDeputados =
                Integer.parseInt(autoria.valueOf("/@quantidadeAssinaturasAdicionaisDeputados"));
        final int quantidadeAssinaturasAdicionaisSenadores =
                Integer.parseInt(autoria.valueOf("/@quantidadeAssinaturasAdicionaisSenadores"));

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
        return autoria.selectNodes("/Parlamentar").stream().map(this::parseParlamentar).collect(Collectors.toList());
    }

    private Parlamentar parseParlamentar(Node parlamentar) {
        final String identificacao = parlamentar.valueOf("/@identificacao");
        final String nome = parlamentar.valueOf("/@nome");
        final Sexo sexo = Sexo.parse(parlamentar.valueOf("/@sexo"));
        final String partido = parlamentar.valueOf("/@siglaPartido");
        final String uf = parlamentar.valueOf("/@siglaUF");
        final SiglaCasaLegislativa casa =
                SiglaCasaLegislativa.parse(parlamentar.valueOf("/@siglaCasaLegislativa"));
        final String cargo = parlamentar.valueOf("/@cargo");

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

        final Node colegiado = autoria.selectSingleNode("/Colegiado");
        if (colegiado == null) {
            return null;
        }
        final String identificacao = colegiado.valueOf("/@identificacao");
        final String nome = colegiado.valueOf("/@nome");
        final String sigla = colegiado.valueOf("/@sigla");


        return new ColegiadoAutorRecord(
                identificacao,
                nome,
                sigla);
    }

    private OpcoesImpressao parseOpcoesImpressao(final Element rootElement) {
        Node opcoes = rootElement.selectSingleNode("/OpcoesImpressao");

        boolean imprimirBrasao = opcoes.valueOf("/@imprimirBrasao").equals("true");
        String textoCabecalho = opcoes.valueOf("/@textoCabecalho");
        boolean reduzirEspacoEntreLinhas = opcoes.valueOf("/@reduzirEspacoEntreLinhas").equals("true");

        return new OpcoesImpressaoRecord(
                imprimirBrasao,
                textoCabecalho,
                reduzirEspacoEntreLinhas);
    }

}
