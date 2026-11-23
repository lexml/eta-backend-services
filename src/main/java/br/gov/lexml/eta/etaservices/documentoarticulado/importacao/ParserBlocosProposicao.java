package br.gov.lexml.eta.etaservices.documentoarticulado.importacao;

import java.io.File;
import java.text.Normalizer;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import br.gov.lexml.eta.etaservices.util.EtaBackendException;
import br.gov.lexml.parser.pl.Articulacao$;
import br.gov.lexml.parser.pl.Marcador;
import br.gov.lexml.parser.pl.Marcadores;
import br.gov.lexml.parser.pl.ProjetoLei;
import br.gov.lexml.parser.pl.ProjetoLeiParser;
import br.gov.lexml.parser.pl.ProjetoLeiParser$;
import br.gov.lexml.parser.pl.block.Block;
import br.gov.lexml.parser.pl.block.Block$;
import br.gov.lexml.parser.pl.block.Dispositivo;
import br.gov.lexml.parser.pl.block.Paragraph;
import br.gov.lexml.parser.pl.errors.ParseException;
import br.gov.lexml.parser.pl.errors.ParseProblem;
import br.gov.lexml.parser.pl.metadado.Id;
import br.gov.lexml.parser.pl.metadado.Metadado;
import br.gov.lexml.parser.pl.output.LexmlRenderer$;
import br.gov.lexml.parser.pl.profile.DocumentProfile;
import br.gov.lexml.parser.pl.profile.DocumentProfileOverride;
import br.gov.lexml.parser.pl.profile.DocumentProfileOverride$;
import br.gov.lexml.parser.pl.profile.DocumentProfileRegister$;
import br.gov.lexml.parser.pl.profile.Lei$;
import br.gov.lexml.parser.pl.profile.MedidaProvisoriaNoCongresso$;
import br.gov.lexml.parser.pl.profile.ProjetoDeLeiNaCamara$;
import br.gov.lexml.parser.pl.profile.ProjetoDeLeiNovaNomenclatura$;
import br.gov.lexml.parser.pl.rotulo.Rotulo;
import br.gov.lexml.parser.pl.rotulo.niveis$;
import br.gov.lexml.parser.pl.rotulo.rotuloParser$;
import br.gov.lexml.parser.pl.validation.Validation;
import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.List$;
import scala.collection.immutable.Map$;
import scala.jdk.javaapi.CollectionConverters;
import scala.util.Left;
import scala.util.matching.Regex;
import scala.xml.Elem;
import scala.xml.Node;
import scala.xml.Text;

/**
 * Executa o parseamento estrutural dos blocos da proposição usando a biblioteca
 * {@code lexml-parser-projeto-lei}, suportando os modos {@link ModoParseProposicao#APROVEITAMENTO}
 * (Cenário 2) e {@link ModoParseProposicao#COMPLETO} (Cenário 1), com controle explícito do linker.
 */
final class ParserBlocosProposicao {

    private static final String CAMINHO_LINKERTOOL = "/usr/local/bin/linkertool";

    private static final List<String> PADROES_EPIGRAFE_APROVEITAMENTO = Arrays.asList(
            "^\\s*(red\\d+;+)?(substitutivo (ao|a)\\s+)?(projeto( de)? (lei|decreto legislativo)|(proposta|projeto) de emenda|projeto de resolu)",
            "^projeto de lei de convers",
            "^medida provisoria(\\s+(n[oº°˚]|federal|\\d)|\\s*$)",
            "^emenda constitucional(\\s+(n[oº°˚]|de revis|\\d)|\\s*$)",
            "^lei\\s+(complementar|delegada)\\b",
            "^lei(\\s+(n[oº°˚]|n\\.|\\d)|\\s*$)",
            "^decreto-lei(\\s+(n[oº°˚]|n\\.|\\d)|\\s*$)",
            "^decreto(\\s+legislativo)?(\\s+(n[oº°˚]|n\\.|\\d)|\\s*$)",
            "^resolucao(\\s+(do |da )?(senado|camara|congresso))?(\\s+(n[oº°˚]|n\\.|\\d)|\\s*$)",
            "^substitutivo\\b",
            "^subemenda substitutiva\\b",
            "^constituicao (da republica federativa|federal) do brasil",
            "^ato das disposicoes constitucionais",
            "^regimento *(interno|comum)"
    );

    private static final List<String> PADROES_EPIGRAFE_CONTINUACAO_APROVEITAMENTO = Arrays.asList(
            "^(n[oº°˚]|complementar|delegada)",
            "^\\(?\\s*(ao?|d[oa])\\s+(projeto|medida|proposta|substitutivo|pl[pscnv]?|pd[lsc]|pr[cs]|pec|mpv)\\b",
            "^de\\s+\\d"
    );

    private static final List<String> PADROES_POS_EPIGRAFE_APROVEITAMENTO = Arrays.asList(
            "^\\s*(\\(.*\\)|autora?:.*|autoria d.*|(d[oa] )?(senador|deputad).*|texto compilado.*)\\s*$"
    );

    ValidadorXmlLexml.ResultadoXml parse(
            scala.collection.immutable.List<Block> blocksScala, OpcoesParseProposicao opcoes) {
        List<Block> blocks = CollectionConverters.asJava(blocksScala);
        if (blocks.isEmpty()) {
            throw new EtaBackendException("Não foi possível identificar conteúdo articulado no arquivo.");
        }

        boolean modoAproveitamento = opcoes.getModo() == ModoParseProposicao.APROVEITAMENTO;
        DocumentProfile profile = resolverPerfil(opcoes, modoAproveitamento);
        Metadado metadado = criarMetadado(profile, opcoes);
        boolean usarLinker = opcoes.getUsarLinker() != null
                ? opcoes.getUsarLinker()
                : new File(CAMINHO_LINKERTOOL).isFile();

        SpanEpigrafeResultado epigrafeRes = extrairEpigrafe(blocks, profile, modoAproveitamento);
        SpanPreambuloResultado preambuloRes = reconhecerPreambulo(epigrafeRes.posEpigrafe, profile);

        Option<Block> ementa = extrairEmenta(preambuloRes.ementaRaw, profile, modoAproveitamento);
        List<Paragraph> preambulo = trimEmptyPars(preambuloRes.preambulo);

        List<Block> articulacaoEntrada = isolarArticulacao(preambuloRes.posPreambulo, profile);
        ProjetoLeiParser parser = new ProjetoLeiParser(profile);
        String urnContexto = metadado.urnContextoLinker();

        scala.collection.immutable.List<Block> articulacaoScala;
        try {
            articulacaoScala = parser.parseArticulacao(toScalaList(articulacaoEntrada), usarLinker, urnContexto);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                throw new EtaBackendException(formatarExcecaoParser((ParseException) e), e);
            }
            throw new EtaBackendException("Erro ao analisar a articulação do documento: " + e.getMessage(), e);
        }

        List<Block> articulacaoJava = CollectionConverters.asJava(articulacaoScala);
        if (articulacaoJava.isEmpty() || !contemDispositivo(articulacaoJava)) {
            throw new EtaBackendException(
                    "Não foi possível identificar a articulação (artigos ou dispositivos) no documento.");
        }

        if (usarLinker && ementa.isDefined()) {
            ementa = Option.apply(ProjetoLeiParser$.MODULE$.reconheceLinks(ementa.get(), urnContexto));
        }

        ProjetoLei pl = new ProjetoLei(
                metadado,
                toScalaList(epigrafeRes.preEpigrafe),
                epigrafeRes.epigrafe,
                ementa,
                toScalaList(preambulo),
                articulacaoScala,
                Map$.MODULE$.<String, Object>empty());

        if (!modoAproveitamento && opcoes.getNumero() != null && opcoes.getAno() != null) {
            pl = pl.remakeEpigrafe();
        }

        Elem xmlElem;
        try {
            xmlElem = LexmlRenderer$.MODULE$.render(pl);
        } catch (Exception e) {
            throw new EtaBackendException("Erro durante a renderização do documento em LexML XML: " + e.getMessage(), e);
        }

        Validation validation = new Validation();
        List<AvisoParseProposicao> avisos = new ArrayList<>();
        try {
            for (ParseProblem p : CollectionConverters.asJava(validation.validaEstrutura(articulacaoScala))) {
                avisos.add(AvisoParseProposicao.de(p));
            }
        } catch (Exception e) {
            if (e instanceof ParseException) {
                for (ParseProblem p : CollectionConverters.asJava(((ParseException) e).errors())) {
                    avisos.add(AvisoParseProposicao.de(p));
                }
            } else {
                throw new EtaBackendException("Erro ao validar a estrutura do documento: " + e.getMessage(), e);
            }
        }

        if (opcoes.isValidarSchema()) {
            for (ParseProblem p : CollectionConverters.asJava(validation.validaComSchema(xmlElem))) {
                avisos.add(AvisoParseProposicao.de(p));
            }
        }

        if (opcoes.isFalharComAvisos() && !avisos.isEmpty()) {
            throw new EtaBackendException(formatarErroAvisos(avisos));
        }

        return new ValidadorXmlLexml.ResultadoXml(xmlElem.toString(), Collections.unmodifiableList(avisos));
    }

    private static final Pattern REGEX_DIACRITICOS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static DocumentProfile resolverPerfil(OpcoesParseProposicao opcoes, boolean modoAproveitamento) {
        if (modoAproveitamento) {
            return criarPerfilAproveitamento(opcoes);
        }
        String autoridade = opcoes.getAutoridade();
        String tipoNorma = opcoes.getTipoNorma();
        DocumentProfile base;
        if (autoridade != null && tipoNorma != null) {
            Option<DocumentProfile> perfil = DocumentProfileRegister$.MODULE$
                    .getProfile(autoridade, tipoNorma, Option.apply("br"));
            if (perfil.isEmpty()) {
                throw new EtaBackendException(
                        "Perfil de documento não encontrado para autoridade '" + autoridade
                                + "' e tipo de norma '" + tipoNorma + "'.");
            }
            base = perfil.get();
        } else if ("camara.deputados".equals(autoridade)) {
            base = ProjetoDeLeiNaCamara$.MODULE$;
        } else if ("congresso.nacional".equals(autoridade)) {
            base = MedidaProvisoriaNoCongresso$.MODULE$;
        } else if ("federal".equals(autoridade)) {
            base = Lei$.MODULE$;
        } else {
            base = ProjetoDeLeiNovaNomenclatura$.MODULE$;
        }
        return aplicarRegexesAdicionaisNoPerfil(base, opcoes);
    }

    private static DocumentProfile aplicarRegexesAdicionaisNoPerfil(
            DocumentProfile base, OpcoesParseProposicao opcoes) {
        List<String> epiAdd = opcoes.getRegexEpigrafeAdicionais();
        List<String> contAdd = opcoes.getRegexEpigrafeContinuacaoAdicionais();
        List<String> posAdd = opcoes.getRegexPosEpigrafeAdicionais();
        if (epiAdd.isEmpty() && contAdd.isEmpty() && posAdd.isEmpty()) {
            return base;
        }

        DocumentProfileOverride$ dpo = DocumentProfileOverride$.MODULE$;

        Option<scala.collection.immutable.List<Regex>> overrideEpigrafe1 = epiAdd.isEmpty()
                ? dpo.apply$default$7()
                : Option.apply(concatenarRegex(base.regexEpigrafe1(), compilarRegexNormalizados(epiAdd)));

        Option<scala.collection.immutable.List<Regex>> overrideEpigrafe;
        if (epiAdd.isEmpty() && contAdd.isEmpty()) {
            overrideEpigrafe = dpo.apply$default$8();
        } else {
            List<Regex> extras = new ArrayList<>();
            extras.addAll(compilarRegexNormalizados(epiAdd));
            extras.addAll(compilarRegexNormalizados(contAdd));
            overrideEpigrafe = Option.apply(concatenarRegex(base.regexEpigrafe(), extras));
        }

        Option<scala.collection.immutable.List<Regex>> overridePosEpigrafe = posAdd.isEmpty()
                ? dpo.apply$default$9()
                : Option.apply(concatenarRegex(base.regexPosEpigrafe(), compilarRegexNormalizados(posAdd)));

        return dpo.apply(
                base,
                dpo.apply$default$2(),
                dpo.apply$default$3(),
                dpo.apply$default$4(),
                dpo.apply$default$5(),
                dpo.apply$default$6(),
                overrideEpigrafe1,
                overrideEpigrafe,
                overridePosEpigrafe,
                dpo.apply$default$10(),
                dpo.apply$default$11(),
                dpo.apply$default$12(),
                dpo.apply$default$13(),
                dpo.apply$default$14(),
                dpo.apply$default$15(),
                dpo.apply$default$16(),
                dpo.apply$default$17(),
                dpo.apply$default$18(),
                dpo.apply$default$19());
    }

    private static DocumentProfileOverride criarPerfilAproveitamento(OpcoesParseProposicao opcoes) {
        List<Regex> epigrafe1 = new ArrayList<>(compilarRegex(PADROES_EPIGRAFE_APROVEITAMENTO));
        epigrafe1.addAll(compilarRegexNormalizados(opcoes.getRegexEpigrafeAdicionais()));
        List<Regex> epigrafeCont = new ArrayList<>(epigrafe1);
        epigrafeCont.addAll(compilarRegex(PADROES_EPIGRAFE_CONTINUACAO_APROVEITAMENTO));
        epigrafeCont.addAll(compilarRegexNormalizados(opcoes.getRegexEpigrafeContinuacaoAdicionais()));
        List<Regex> posEpigrafe = new ArrayList<>(compilarRegex(PADROES_POS_EPIGRAFE_APROVEITAMENTO));
        posEpigrafe.addAll(compilarRegexNormalizados(opcoes.getRegexPosEpigrafeAdicionais()));

        DocumentProfileOverride$ dpo = DocumentProfileOverride$.MODULE$;
        return dpo.apply(
                ProjetoDeLeiNovaNomenclatura$.MODULE$,
                dpo.apply$default$2(),
                dpo.apply$default$3(),
                dpo.apply$default$4(),
                dpo.apply$default$5(),
                dpo.apply$default$6(),
                Option.apply(toScalaList(epigrafe1)),
                Option.apply(toScalaList(epigrafeCont)),
                Option.apply(toScalaList(posEpigrafe)),
                Option.apply(Boolean.FALSE),
                Option.apply(Boolean.TRUE),
                dpo.apply$default$12(),
                dpo.apply$default$13(),
                dpo.apply$default$14(),
                dpo.apply$default$15(),
                dpo.apply$default$16(),
                dpo.apply$default$17(),
                dpo.apply$default$18(),
                Option.apply(Boolean.TRUE));
    }

    private static Metadado criarMetadado(DocumentProfile profile, OpcoesParseProposicao opcoes) {
        int ano = opcoes.getAno() != null ? opcoes.getAno() : Year.now().getValue();
        int numero = opcoes.getNumero() != null ? opcoes.getNumero() : 999999;
        Id id = new Id(numero, Option.empty(), new Left<>(ano), Option.empty());
        return new Metadado(
                profile,
                Option.empty(),
                Option.apply(opcoes.getAutoridade()),
                Option.apply(opcoes.getTipoNorma()),
                Option.apply(id),
                Option.empty());
    }

    private static SpanEpigrafeResultado extrairEpigrafe(
            List<Block> blocks, DocumentProfile profile, boolean modoAproveitamento) {
        if (profile.regexEpigrafe().isEmpty()) {
            return new SpanEpigrafeResultado(Collections.emptyList(), paragrafoVazio(), blocks, false);
        }

        List<Regex> re1 = CollectionConverters.asJava(profile.regexEpigrafe1());
        List<Regex> reComb = new ArrayList<>(CollectionConverters.asJava(profile.regexEpigrafe()));
        reComb.addAll(re1);
        List<Regex> rePos = CollectionConverters.asJava(profile.regexPosEpigrafe());

        int inicioEpi = 0;
        while (inicioEpi < blocks.size() && !matchesOneOf(re1, blocks.get(inicioEpi))) {
            inicioEpi++;
        }

        if (inicioEpi >= blocks.size()) {
            if (!modoAproveitamento && profile.epigrafeObrigatoria()) {
                throw new EtaBackendException("Epígrafe ausente ou fora do padrão.");
            }
            return new SpanEpigrafeResultado(Collections.emptyList(), paragrafoVazio(), blocks, false);
        }

        List<Block> pre = new ArrayList<>();
        for (int i = 0; i < inicioEpi; i++) {
            Block b = blocks.get(i);
            if (!isEmptyPar(b)) {
                pre.add(b);
            }
        }
        if (!modoAproveitamento && !profile.preEpigrafePermitida() && !pre.isEmpty()) {
            throw new EtaBackendException("Epígrafe ausente ou fora do padrão.");
        }

        int fimEpi = inicioEpi;
        while (fimEpi < blocks.size() && matchesOneOf(reComb, blocks.get(fimEpi))) {
            fimEpi++;
        }

        List<Paragraph> epiPars = new ArrayList<>();
        for (int i = inicioEpi; i < fimEpi; i++) {
            if (blocks.get(i) instanceof Paragraph) {
                epiPars.add((Paragraph) blocks.get(i));
            }
        }
        if (epiPars.isEmpty()) {
            if (!modoAproveitamento && profile.epigrafeObrigatoria()) {
                throw new EtaBackendException("Epígrafe ausente ou fora do padrão.");
            }
            return new SpanEpigrafeResultado(Collections.emptyList(), paragrafoVazio(), blocks, false);
        }

        int inicioPos = fimEpi;
        while (inicioPos < blocks.size()
                && (matchesOneOf(rePos, blocks.get(inicioPos)) || isEmptyPar(blocks.get(inicioPos)))) {
            inicioPos++;
        }

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < epiPars.size(); i++) {
            if (i > 0) {
                nodes.add(new Text(" "));
            }
            nodes.addAll(CollectionConverters.asJava(epiPars.get(i).nodes()));
        }
        Paragraph epigrafe = new Paragraph(
                toScalaList(nodes), 0.0, false, false, false, Option.empty(), Option.empty());

        return new SpanEpigrafeResultado(pre, epigrafe, blocks.subList(inicioPos, blocks.size()), true);
    }

    private static SpanPreambuloResultado reconhecerPreambulo(List<Block> blocks, DocumentProfile profile) {
        List<Regex> rePreambulo = CollectionConverters.asJava(profile.regexPreambulo());
        List<Regex> rePosEpigrafe = CollectionConverters.asJava(profile.regexPosEpigrafe());

        int i = 0;
        while (i < blocks.size() && !matchesOneOf(rePreambulo, blocks.get(i)) && !isArticulacao(blocks.get(i))) {
            i++;
        }
        List<Block> ementaRaw = blocks.subList(0, i);

        int j = i;
        while (j < blocks.size() && !isArticulacao(blocks.get(j))) {
            j++;
        }

        List<Paragraph> preambulo = new ArrayList<>();
        for (int k = i; k < j; k++) {
            Block b = blocks.get(k);
            if (b instanceof Paragraph && !matchesOneOf(rePosEpigrafe, b)) {
                preambulo.add((Paragraph) b);
            }
        }

        List<Block> posPreambulo = blocks.subList(j, blocks.size());
        return new SpanPreambuloResultado(ementaRaw, preambulo, posPreambulo);
    }

    private static Option<Block> extrairEmenta(
            List<Block> ementaRaw,
            DocumentProfile profile,
            boolean modoAproveitamento) {
        List<Block> trimmed = trimEmptyPars(ementaRaw);
        if (modoAproveitamento) {
            if (trimmed.isEmpty()) {
                return Option.empty();
            }
            List<Block> candidata = ultimoBlocoContiguoDeParagrafos(trimmed);
            if (candidata.isEmpty()) {
                return Option.empty();
            }
            return Block$.MODULE$.joinParagraphs(toScalaList(candidata)).headOption();
        }

        boolean invalida = trimmed.isEmpty()
                || trimmed.stream().anyMatch(b -> isEmptyPar(b) || !(b instanceof Paragraph));
        if (invalida) {
            if (profile.ementaAusente()) {
                return Option.empty();
            }
            throw new EtaBackendException("Ementa ausente ou fora do padrão.");
        }
        return Block$.MODULE$.joinParagraphs(toScalaList(trimmed)).headOption();
    }

    private static List<Block> ultimoBlocoContiguoDeParagrafos(List<Block> blocks) {
        int inicioUltimoBloco = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            if (!(b instanceof Paragraph) || isEmptyPar(b)) {
                inicioUltimoBloco = i + 1;
            }
        }
        return filtrarParagrafosNaoVazios(blocks.subList(inicioUltimoBloco, blocks.size()));
    }

    private static List<Block> filtrarParagrafosNaoVazios(List<Block> blocks) {
        List<Block> result = new ArrayList<>();
        for (Block b : blocks) {
            if (b instanceof Paragraph && !isEmptyPar(b)) {
                result.add(b);
            }
        }
        return result;
    }

    private static List<Block> isolarArticulacao(List<Block> posPreambulo, DocumentProfile profile) {
        if (posPreambulo.isEmpty()) {
            throw new EtaBackendException(
                    "Não foi possível identificar a articulação (artigos ou dispositivos) no documento.");
        }
        Marcadores ms = new Marcadores(profile, false, false, false, false, false);
        java.util.Map<Marcador, scala.collection.immutable.List<Block>> elementos =
                CollectionConverters.asJava(ms.span(toScalaList(posPreambulo)));
        scala.collection.immutable.List<Block> art = elementos.get(Articulacao$.MODULE$);
        if (art == null) {
            throw new EtaBackendException(
                    "Não foi possível identificar a articulação (artigos ou dispositivos) no documento.");
        }
        return CollectionConverters.asJava(art);
    }

    private static boolean contemDispositivo(List<Block> blocks) {
        for (Block b : blocks) {
            if (b instanceof Dispositivo) {
                return true;
            }
        }
        return false;
    }

    private static boolean isArticulacao(Block b) {
        if (!(b instanceof Paragraph)) {
            return false;
        }
        Option<Tuple2<Rotulo, Object>> parsed = rotuloParser$.MODULE$.parseRotulo().apply(((Paragraph) b).text());
        if (parsed.isEmpty()) {
            return false;
        }
        return parsed.get()._1().nivel() <= niveis$.MODULE$.nivel_maximo_aceito_na_raiz();
    }

    private static boolean matchesOneOf(List<Regex> regexes, Block b) {
        if (!(b instanceof Paragraph)) {
            return false;
        }
        String text = ((Paragraph) b).text();
        for (Regex r : regexes) {
            if (r.findFirstIn(text).isDefined()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmptyPar(Block b) {
        return b instanceof Paragraph && ((Paragraph) b).text().isEmpty();
    }

    private static <T extends Block> List<T> trimEmptyPars(List<T> list) {
        int start = 0;
        while (start < list.size() && isEmptyPar(list.get(start))) {
            start++;
        }
        int end = list.size();
        while (end > start && isEmptyPar(list.get(end - 1))) {
            end--;
        }
        return list.subList(start, end);
    }

    private static Paragraph paragrafoVazio() {
        return new Paragraph(List$.MODULE$.empty(), 0.0, false, false, false, Option.empty(), Option.empty());
    }

    /** Remove diacríticos mantendo escapes e habilita comparação case-insensitive contra {@code Paragraph.text}. */
    static String normalizarRegexParaTextoNormalizado(String padrao) {
        String semAcentos = REGEX_DIACRITICOS.matcher(Normalizer.normalize(padrao, Normalizer.Form.NFD)).replaceAll("");
        return "(?iu)" + semAcentos;
    }

    private static scala.collection.immutable.List<Regex> concatenarRegex(
            scala.collection.immutable.List<Regex> base, List<Regex> extras) {
        List<Regex> combinado = new ArrayList<>(CollectionConverters.asJava(base));
        combinado.addAll(extras);
        return toScalaList(combinado);
    }

    private static List<Regex> compilarRegexNormalizados(List<String> padroes) {
        List<Regex> regexes = new ArrayList<>(padroes.size());
        for (String padrao : padroes) {
            regexes.add(new Regex(normalizarRegexParaTextoNormalizado(padrao), List$.MODULE$.empty()));
        }
        return regexes;
    }

    private static List<Regex> compilarRegex(List<String> padroes) {
        List<Regex> regexes = new ArrayList<>(padroes.size());
        for (String padrao : padroes) {
            regexes.add(new Regex(padrao, List$.MODULE$.empty()));
        }
        return regexes;
    }

    private static <T> scala.collection.immutable.List<T> toScalaList(List<T> list) {
        return List$.MODULE$.from(CollectionConverters.asScala(list));
    }

    private static String formatarExcecaoParser(ParseException e) {
        List<String> msgs = new ArrayList<>();
        for (ParseProblem p : CollectionConverters.asJava(e.errors())) {
            msgs.add(AvisoParseProposicao.de(p).toString());
        }
        if (msgs.isEmpty()) {
            return "Não foi possível interpretar a estrutura do documento.";
        }
        return "Não foi possível interpretar o documento: " + String.join("; ", msgs);
    }

    private static String formatarErroAvisos(List<AvisoParseProposicao> avisos) {
        StringBuilder sb = new StringBuilder("O documento possui inconsistências na validação:");
        for (AvisoParseProposicao aviso : avisos) {
            sb.append("\n- ").append(aviso);
        }
        return sb.toString();
    }

    private static final class SpanEpigrafeResultado {
        final List<Block> preEpigrafe;
        final Block epigrafe;
        final List<Block> posEpigrafe;
        final boolean reconhecida;

        SpanEpigrafeResultado(List<Block> preEpigrafe, Block epigrafe, List<Block> posEpigrafe, boolean reconhecida) {
            this.preEpigrafe = preEpigrafe;
            this.epigrafe = epigrafe;
            this.posEpigrafe = posEpigrafe;
            this.reconhecida = reconhecida;
        }
    }

    private static final class SpanPreambuloResultado {
        final List<Block> ementaRaw;
        final List<Paragraph> preambulo;
        final List<Block> posPreambulo;

        SpanPreambuloResultado(List<Block> ementaRaw, List<Paragraph> preambulo, List<Block> posPreambulo) {
            this.ementaRaw = ementaRaw;
            this.preambulo = preambulo;
            this.posPreambulo = posPreambulo;
        }
    }

}
