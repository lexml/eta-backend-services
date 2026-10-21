# CLAUDE.md

Este arquivo orienta o Claude Code (claude.ai/code) ao trabalhar neste repositório.

## OpenSpec (`openspec/`)

### Definição

Desde 24/09/2026, o projeto usa o [OpenSpec](https://github.com/Fission-AI/OpenSpec) (`@fission-ai/openspec`, CLI global + skills/comandos `/opsx:*` em `.claude/`) para especificação e rastreamento de mudanças voltado a desenvolvimento colaborativo com IA, seguindo a mesma estrutura do projeto `lexml-eta`. **`openspec/` é versionado no repositório principal** (spec revisada junto com o código, no mesmo diff); `.claude/` não é versionado — cada desenvolvedor gera o seu com `openspec init --tools claude`.

- `openspec/specs/<capability>/spec.md` — comportamento atual, em requisitos testáveis (`### Requirement:` + `#### Scenario:` com `WHEN`/`THEN`). **Não é gerado em massa**: só passa a existir via uma change arquivada.
- `openspec/changes/<slug>/` — toda mudança nova nasce aqui via `/opsx:propose`, com `proposal.md`/`design.md`/`tasks.md`/specs delta (`## ADDED/MODIFIED/REMOVED Requirements`).
- `openspec/changes/archive/<slug>/` — mudanças concluídas, com o delta já mesclado nas specs principais.
- `openspec/config.yaml` — `schema: spec-driven`, `Language: pt-BR` (conteúdo em português; headings estruturais `## Purpose`/`## Requirements`/`### Requirement:` e as palavras `SHALL`/`MUST` ficam em inglês por convenção do OpenSpec).

Fluxo: `/opsx:explore` (opcional) → `/opsx:propose` → `/opsx:apply` → `/opsx:archive`.

### Convenção de nomes para changes do OpenSpec

```
<aaaa>-<mm>-<dd>-c<xx>-<nome-da-change>
```

- `aaaa-mm-dd`: data de criação da change.
- `c<xx>`: contador sequencial de duas casas (`c01`, `c02`, ...), reiniciado a cada dia.
- `<nome-da-change>`: nome descritivo em kebab-case.

Exemplo: `2026-09-24-c01-anexo-parecer-pdf`.

### Regras aplicadas em toda change (enforcement em `openspec/config.yaml`)

- **Testes JUnit**: toda `tasks.md` deve prever explicitamente uma task de testes automatizados (JUnit 5), executáveis por `mvn test` sem rede nem executável `jsonix-lexml`. Se não for viável, registrar a decisão e propor alternativa concreta (ex.: verificação manual via `Json2PDF`/`Json2PDFParecer` com checklist).
- **Pausa por task ao aplicar**: ao concluir todas as subtasks de uma task numerada (ex.: 2.x), pausar e pedir validação antes da próxima.
- **Commits ao pausar**: oferecer que o usuário commite sozinho ou ajudar a organizar — commits semanticamente agrupados, descrição sucinta, plano apresentado antes (nunca commitar direto) e sem coautoria da IA.

## Visão geral do projeto

Biblioteca Java (`br.gov.lexml.eta:eta-backend-services`, empacotada como JAR) com os serviços de back-end dos componentes front-end `lexml-emenda`, `lexml-eta` e `lexml-parecer`. Não é uma aplicação standalone: não há classe `main`/`@SpringBootApplication`; é consumida por outras aplicações Spring Boot. Principais funcionalidades:

- Gerar PDF/A de **emenda** e **parecer** a partir do modelo (JSON) e dos anexos enviados.
- Recuperar o JSON de emenda/parecer a partir do PDF gerado (o modelo vai embutido no PDF como anexo).
- Gerar PDF/A de **proposição** (editor `lexml-eta`) a partir do `documento-articulado.json`, com o `documento-articulado.xml` (LexML) embutido, e recuperar o JSON a partir desse PDF.
- Listar proposições e obter o texto delas em LexML/jsonix (serviços do Senado + executável `jsonix-lexml`).

Stack: Java 11, Maven, Spring Boot 2.7.7 (BOM importado em `dependencyManagement`), Lombok, Jackson, JAXB/dom4j/jaxen, Velocity 1.7, Apache FOP 2.7, PDFBox, `br.gov.lexml:pdfa-helper`, `lexml-parser-projeto-lei`.

## Comandos

```bash
mvn clean install                      # build + testes
mvn test                               # todos os testes
mvn test -Dtest=EmendaXmlMarshallingTest                  # uma classe
mvn test -Dtest=EmendaXmlMarshallingTest#nomeDoMetodo     # um método
mvn gitflow:feature-start              # fluxo gitflow (ver README)
mvn gitflow:hotfix-start -DuseSnapshotInHotfix=true -DpushRemote=true   # = hotfix-start.sh
mvn -Pdeploy deploy                    # publicação no Maven Central (a partir de uma tag)
```

Não há Maven Wrapper; usa o `mvn` instalado. CI no Jenkins do Senado (`Jenkinsfile`, JDK 11, branch de produção `main`). Branches: `develop` (integração), `main` (produção), `feature/*`, `hotfix/*`.

Utilitários manuais (não são testes JUnit, rodam via `main`): `src/test/java/.../printing/xml/Json2PDF.java`, `Json2PDFParecer.java`, `Json2XML.java`, e `src/main/java/.../printing/pdf/TesteGeracaoPDF*.java` — úteis para gerar um PDF real e inspecioná-lo visualmente.

## Arquitetura

Pacote raiz `br.gov.lexml.eta`:

### `etaservices` — emenda e parecer

- **`emenda/`** — modelo de domínio da emenda como **interfaces** (`Emenda`, `DispositivoEmenda`, `Autoria`, `Comentario`, ...). Cada interface tem duas implementações paralelas:
  - `printing/json/*Pojo` — classes Jackson usadas ao **receber JSON** do front-end (entrada para gerar PDF).
  - `parsing/xml/*Record` — implementações montadas ao **ler o XML** extraído do PDF (`EmendaXmlUnmarshaller`).
  - Ao adicionar um campo à emenda, é preciso atualizar a interface, o `Pojo`, o `Record`, o `EmendaXmlMarshaller`/`EmendaXmlUnmarshaller` e o template Velocity.
- **`parecer/`** — modelo do parecer como **classes** Lombok (`Parecer`, `AnexoParecer`, `AnexoPDFA`, `Revisao*`); o JSON do parecer é serializado diretamente com Jackson (sem XML intermediário).
- **`printing/pdf/`** — pipeline de geração de PDF:
  1. `VelocityTemplateProcessor` (obtido de `VelocityTemplateProcessorFactory`) aplica `template-velocity-emenda.xml` ou `template-velocity-parecer.xml` (em `src/main/resources`), com helpers em `VelocityExtension`/`VelocityExtensionUtils`; HTML de texto rico é convertido para XSL-FO por `HTML2FOConverter` (`xhtml2fo.xsl`, `htmlcleaner`).
  2. `FOPProcessor.processFOP(...)` renderiza o XSL-FO com FOP (`fop.xconf`, fontes em `pdfa-fonts/`), gera PDF/A, concatena os anexos imprimíveis e **embute o modelo como anexo do PDF**: `emenda.xml` (emenda, via `EmendaXmlMarshaller`) ou `parecer.json` (parecer). Anexos do parecer também são embutidos como arquivos PDF/A (`AnexoPDFA`), e só os marcados `imprimir` são concatenados às páginas.
  3. `PdfGeneratorBean` (emenda) / `PdfGeneratorParecer` (parecer) substituem o placeholder `<check:hash>000…` no XMP pelo MD5 do PDF.
  - `TipoDocumento` (`EMENDA`, `PARECER`, `SUBSTITUTIVO`, `OUTRO`) direciona comportamentos específicos no `FOPProcessor`.
- **Extração do PDF**: `EmendaJsonGeneratorBean.extractJsonFromPdf` extrai `emenda.xml` com `PDFAttachmentHelper`, faz unmarshal e reescreve como JSON; `ParecerJsonGenerator.extractJsonFromPdf` extrai `parecer.json` diretamente. Um PDF sem esse anexo gera `EtaBackendException`.
- **`parsing/lexml/`** — `LexmlParser` sobre o `lexml-parser-projeto-lei`.
- **`util/`** — `BytesUtil`, `XMLUtil`, `EtaFileUtil`, `EtaBackendException`.

As classes de `etaservices` são POJOs instanciados manualmente (sem `@Component`); a aplicação consumidora decide como expô-las como beans.

### `etaservices.documentoarticulado` — proposição do ETA (documento articulado)

Fluxo **isolado** de emenda e parecer: não reutiliza classes, templates nem configurações deles (`FOPProcessor`, `FOHelper`, `VelocityTemplateProcessor*`, `VelocityExtension`, `fop.xconf`, `lexmljsonix`). Compartilha apenas `pdfa-fonts/`, `static/img/` e `util/` (`EtaBackendException`, `BytesUtil`). Não há classes de domínio: o documento trafega como texto JSON/XML.

- `conversor/` — `ConversorDocumentoArticulado` (JSON ↔ XML) e `ConversorDocumentoArticuladoCli`, que executa o `jsonix-lexml` **2.0.0+** (exigido pelos metadados `lexedit:Metadado`). O executável sai com código 0 mesmo em erro (erro só no stderr), por isso saída vazia é tratada como falha; usa UTF-8, diretório temporário por conversão e tempo limite.
- `pdf/` — `DocumentoArticuladoPdfGenerator.generate(json, out)`: valida o JSON, converte para XML e monta o PDF em duas camadas:
  - **conteúdo:** `DocumentoArticuladoConteudoTransformer` aplica `documentoarticulado/documento-articulado-conteudo.xsl` (XSLT 1.0) ao `documento-articulado.xml` e gera um fragmento XSL-FO. Hoje imprime a parte inicial (epígrafe, ementa e preâmbulo) e a articulação básica. A epígrafe é aparada só nas pontas (`aparar-inicio`/`aparar-fim`), com os espaços internos preservados como `&#160;` (uma linha só). Quando só há espaços entre "Nº" (ou "N°") e a vírgula (projeto não numerado, ex.: "PROJETO DE LEI Nº     , DE"), o vão fica com os espaços gravados pelo editor, com no mínimo 8 (`vao-do-numero`). A articulação básica (artigo, caput, parágrafo, inciso, alínea e item: texto justificado, recuo de 2,5cm e sem espaço entre dispositivos; depois do último, `padding-bottom` com o espaço após o parágrafo — `padding`, e não `margin`, porque no FOP a margem vira `space-after` e se funde com o `space-before` seguinte em vez de somar), percorrida no modo `dispositivo` com o template nomeado `bloco-dispositivo`. O rótulo vai num `fo:inline` com `word-spacing="0pt"`, para ser tratado como uma palavra só: os espaços internos ("Art. 15.", "§ 1º", "I –") não esticam na justificação, e a folga fica no espaço entre o rótulo e o texto. Trocar por `&#160;` não serviria, porque o FOP 2.7 também ajusta o U+00A0 (`CharUtilities.isAdjustableSpace`). Efeito aceito: em alguns parágrafos com rótulo, uma ou duas palavras mudam de linha. Os agrupadores têm rótulo e nome impressos antes do conteúdo pelo template `titulo-agrupador`: centralizados, com `keep-with-next` e sem espaço extra. Parte, Livro, Título e Capítulo saem em maiúsculas (template `maiusculas`, via `translate()`, porque XSLT 1.0 não tem `upper-case()`) com rótulo em negrito; Seção e Subseção saem em negrito. O `Agrupamento` genérico só é atravessado. A `Pena` usa o `bloco-dispositivo` com `rotuloNegrito = false()`. O `TituloDispositivo` sai em bloco próprio, todo em negrito, com `keep-with-next`, antes do dispositivo a que pertence e herdando recuo e justificação. O bloco `Alteracao` (norma vigente) sai recuado (`margin-left` 3cm, `text-indent` 1.5cm), com os dispositivos de dentro pelos mesmos templates. As aspas e a nota de alteração vêm **só dos atributos** `abreAspas`, `fechaAspas` e `notaAlteracao` (`“` antes do rótulo, `” (NR)` depois do texto, nada depois da nota); o espaço final do texto é aparado (`aparar-fim`) para a aspa ficar colada. `Omissis` e `textoOmitido="s"` saem como linha pontilhada (`fo:leader` de pontos). Remissões (`span`/`Remissao` com `xlink:href`, no modo `inline`): URN vira link para `https://normas.leg.br/?urn=<URN>`; id de um dispositivo impresso (chave `dispositivo-impresso`, restrita a `lx:Articulacao//`) vira link interno; alvo ausente sai como texto simples. Links em `#808080`, sem sublinhado; preâmbulo e nome de agrupador ficam sem link. O `id` de destino só vai nos alvos de remissão interna (chave `remissao-interna`): no rótulo do artigo, no bloco dos demais dispositivos e no título dos agrupadores. Com isso a issue #72 está completa. Depois da articulação vem a **justificação** (issue #75, parte A; formato das specs 06, 07 e 09 em `lexml-eta/docs/extensao-formato-lexml/`): título "JUSTIFICAÇÃO" (centralizado, negrito, tamanho de destaque, 26pt antes — 22pt com "reduzir") e corpo (14pt antes — 10pt com "reduzir" —, entrelinha, recuo de 2,5cm, alinhado à esquerda), um bloco por `p` com o espaço após o parágrafo; parágrafo vazio vira linha em branco; sem texto fora de `del`, nada (nem título). As classes do Quill são lidas por token (`align-*` e `ql-align-*` — o lexml-eta grava sem o `ql-` —, `ql-text-indent-0px`, `ql-margin-bottom-0px`, `estilo-ementa`, `estilo-norma-alterada`; desconhecidas ignoradas). O modo `inline` também trata `u`, `sub`/`sup` (0.7em), `a` com `xlink:href` http(s) (link no estilo das remissões), `del` (omitido: o PDF é a versão revisada) e `ins`/`span` de comentário (texto normal). `NotaDeRodape` vira `fo:footnote`: número sobrescrito, corpo "N texto" em 0.7em sobre o `$tamanhoFonte` (quarto `xsl:param`), com as propriedades herdadas do parágrafo zeradas, e numeração `level="any"` que ignora notas em `del`. **Listas, tabelas e imagens** (parte B): `ol`/`ul` viram `fo:list-block` (recuo de 2,5cm, rótulo "N." ou "•" à direita, `margin-bottom`), com lista dentro de item aninhada e `indent-N` ignorado; `table` vira uma tabela externa centralizadora (largura = `width` como %, ou 100%) com a tabela interna (borda 1pt ou nenhuma com `border="0"`, colunas iguais contando `colspan`, células com 80%/140%, `th` em negrito, spans); `img` só com `src` `data:image/` (enviado direto ao FOP, sem arquivo temporário; outros endereços ignorados), em bloco próprio com `text-indent="0"`, largura = `width` como % ou 100%, proporção mantida, e parágrafo que começa com imagem centralizado. Imagem que o FOP não lê (ex.: ICO) é ignorada com erro no log. O corpo da justificação tem `line-height-shift-adjustment="disregard-shifts"` (sobrescrito/subscrito, inclusive o número da nota, não aumentam a altura da linha). Atenção: no XSLT do JDK, o predicado `[self::* or normalize-space(.)]` dá falso para elemento sem texto; usar `[not(self::text()) or normalize-space(.)]`. Local e data e assinaturas não saem da XSLT (o `ParteFinal` textual do LexML é ignorado): ver **página**. Usa `TransformerFactory.newDefaultInstance()` (XSLT do JDK) de propósito: o Xalan 2.7.2 do classpath trata `fo:*` como XSL e rejeita os atributos em XSLT 1.0.
  - **página:** `documentoarticulado/template-velocity-documento-articulado.xml` cuida do layout, do cabeçalho, do XMP, dos alertas e do local e data e assinaturas, e recebe o fragmento em `$conteudoFo`, sem reinterpretá-lo. O `fo:static-content` `xsl-footnote-separator` (traço de 50%, 0.5pt) fica ativo antes do `fo:flow`, para as notas de rodapé. **Cabeçalho** (issue #74, parte A), só na primeira página: `region-before` de 3cm e corpo a 4cm (1cm sem cabeçalho), Brasão da República (`/static/img/brasao.jpg`, JPEG em base64) centralizado, nome da casa em ITC Stone Sans 11pt (só com o brasão) e `textoCabecalho` centralizado. O `DocumentoArticuladoTemplateProcessor` lê as opções de impressão por `OpcoesImpressaoDocumentoArticulado` (ver abaixo) e a casa pela autoridade da URN (`senado.federal`, `camara.deputados`, `congresso.nacional`; outras, como `federal` da MPV, só brasão) e põe no contexto do Velocity `temCabecalho`, `imprimirBrasao`, `brasao`, `casaLegislativa` e `textoCabecalho` (escapado); o JSON não vai inteiro para o template. **Anexo de parecer** (issue #80): com `lexedit:Metadado` `anexoParecer` booleano verdadeiro (`FechoDocumentoArticulado.anexoParecer`; ausente, falso ou de outro tipo vale falso), o PDF não imprime a justificação (`xsl:param` `imprimirJustificacao` = `false`, passado pelo terceiro argumento de `DocumentoArticuladoConteudoTransformer.transformar`; as notas dela somem junto); o local e data e as assinaturas saem normalmente se existirem, logo depois da articulação (regra revista depois da #80: só a justificação é omitida); cabeçalho, alertas e conteúdo da norma não mudam, e o anexo do PDF continua sendo o documento convertido. **Caixa de pendências** (issue #79, revista na avaliação da chefia), antes do `$conteudoFo` (depois do cabeçalho, antes da epígrafe). Uma só caixa, em fundo amarelo, texto vermelho e negrito. O texto tem 2pt a menos que o do documento (`getTamanhoFonteAlerta`: 12, 14 ou 16pt), ao contrário da emenda, que mantém 12pt fixo. O cabeçalho é "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:", seguido da lista de `lexedit:Pendencias` **como registrada**, sem filtro nem reescrita, lida por `AlertasDocumentoArticulado`; pendência em branco é descartada. Revisões e comentários não têm caixa própria (diferente da emenda): o aviso deles é a pendência que o editor registra. Sem pendências, não sai nenhuma caixa. No contexto: `tamanhoFonteAlerta` e `pendencias` (escapadas). **Local e data e assinaturas** (issue #76), depois de todo o `$conteudoFo`, com os dados estruturados do `lexedit:Metadado` (não o `ParteFinal`), lidos por `FechoDocumentoArticulado`: fecho `role="fecho"` só com `local` ("`<local>`, `<data por extenso em pt-BR>`.", recuo de 2,5cm, 34pt antes ou 26pt com "reduzir", `keep-with-next`); sem data válida (vazia, ausente ou fora de `aaaa-mm-dd`), só o local seguido de vírgula ("Sala das Sessões,", issue #81, especificação 03 do lexml-eta), sem espaços para preencher. Assinaturas só com autoria `tipo="Parlamentar"`: tabela `role="Signatários"` em negrito, duas colunas de 50%, células centralizadas, pares (a última sozinha com `number-columns-spanned="2"`), nome com 30pt antes; tratamento pela casa e pelo sexo (`CD` -> Deputado/Deputada, senão Senador/Senadora; `M` masculino, senão feminino), "(PARTIDO - UF)" com `imprimirPartidoUF` (sem parênteses vazios) e cargo quando preenchido. Sem assinaturas adicionais (linhas em branco) nem de comissão. No contexto: `local`, `dataPorExtenso`, `espacoAntesFecho` e `assinaturas` (pares de mapas `nome`/`partidoUF`/`cargo`, escapados: o Velocity 1.7 só chama métodos públicos de classes públicas).
  - **Opções de impressão** (`lexedit:OpcoesImpressao` em `metadadoProprietario[*].any[*].value.opcoesImpressao`): `OpcoesImpressaoDocumentoArticulado.de(json)` lê uma vez, com os padrões do editor quando ausentes ou de tipo inesperado (`imprimirBrasao` true, `textoCabecalho` vazio, `reduzirEspacoEntreLinhas` false, `tamanhoFonte` 14; só 14, 16 e 18 são aceitos, outro valor vale 14). Tamanhos de fonte e espaçamentos vêm de `ParametrosImpressaoDocumentoArticulado.de(opcoes)` (issue #74, parte B): fonte = `tamanhoFonte`; destaque 18pt com fonte 18, senão 16pt; alertas `tamanhoFonte` - 2; entrelinha 150% (120% com "reduzir"); espaço após o parágrafo 0.6em (0.45em); antes do título da justificação 26pt (22pt) e antes do corpo 14pt (10pt). São passados ao template e à XSLT (`xsl:param` `tamanhoFonte`, `maxTamanhoFonte`, `lineHeight`, `pMarginBottom`, `espacoAntesTituloJustificacao`, `espacoAntesCorpoJustificacao`); notas (0.7em) e células (80%) acompanham a fonte. Fixos, sem variar com as opções: 48pt depois da epígrafe e 72pt depois da ementa (#72) e o cabeçalho (casa em 11pt, regiões de 3cm/4cm).
  - Por fim, renderiza com FOP (`documentoarticulado/fop-documento-articulado.xconf`) em PDF/A-3B com o anexo `documento-articulado.xml` e grava o MD5 no `<check:hash>`. Tudo em `pdf/`, exceto o gerador, é package-private.
- `extracao/` — `DocumentoArticuladoJsonExtractor.extractJsonFromPdf(pdf, writer)`: lê o anexo em memória com PDFBox e converte para JSON; PDF sem o anexo gera `EtaBackendException`.

Montagem manual pela aplicação consumidora (sem auto-configuration nem propriedade própria), por exemplo:

```java
ConversorDocumentoArticulado conversor = new ConversorDocumentoArticuladoCli(Paths.get(cli), Duration.ofSeconds(30));
new DocumentoArticuladoPdfGenerator(conversor);
new DocumentoArticuladoJsonExtractor(conversor);
```

### `lexmljsonix` — proposições e conversão LexML ↔ jsonix

Registrado via **auto-configuration** em `META-INF/spring.factories`:
- `conversor/` — `ConversorLexmlJsonix` (+ `Impl`, `AutoConfiguration`, `LexmlJsonixProperties`): converte LexML ↔ jsonix chamando o executável externo `jsonix-lexml` (propriedade `lexml-jsonix.cli`).
- `service/` — `LexmlJsonixService` (+ `Impl`, `AutoConfiguration`): pesquisa proposições e baixa o texto LexML (zip) dos serviços do Senado (`lexml-jsonix.url-proposicoes`, `lexml-jsonix.url-sdleg`).
- `utils/` — execução de CLI, zip e DTOs de entrada/saída.

## Testes

JUnit 5 (via `spring-boot-starter-test`) + AssertJ + XMLUnit (`xmlunit-assertj3`). Fixtures em `src/test/resources` (JSON/XML de emendas, `parecer.json`, PDFs/DOCX de anexo). Principais suítes: `EmendaXmlMarshallingTest` (maior cobertura: modelo → XML), `EmendaXmlUnmarshallerTest`, `VelocityTemplateProcessorTest`/`VelocityTemplateProcessorComentariosTest`, `PdfGeneratorTest`, `EmendaPojoComentariosTest`.

Documento articulado (`src/test/java/.../documentoarticulado/`): os testes não usam o `jsonix-lexml` real — `ConversorDocumentoArticuladoFake` devolve pares de fixtures de `src/test/resources/documentoarticulado/` (`comExemplo()`: `documento-articulado-exemplo.json`/`.xml`; `comDocumentosDeTeste()`: também os três documentos anexados à issue #72, `documento-com-articulacao-e-alteracao-de-norma`, `documento-com-capitulo-e-secao` e `documento-com-pena-e-titulo-de-dispositivo`; o `documento-com-remissoes-internas`, com remissões internas válidas e inválidas; e os da #75, `documento-com-justificacao` — catálogo dos recursos da justificação: formatação inline, estilos do Quill, revisões, comentário, notas de rodapé, inclusive uma dentro de `del`, listas aninhadas, tabelas com spans e imagens PNG/JPEG/externa — e `documento-com-justificacao-longa`, com notas em várias páginas, cabeçalho só com texto (`imprimirBrasao="false"`), `data` vazia (fecho "Sala das Sessões,") e `imprimirPartidoUF="false"`; e o da #80, `documento-anexo-parecer` (`ANEXO_PARECER`), com `anexoParecer="true"`, justificação com nota (não impressa) e local, data e um senador (impressos depois da articulação)); o `documento-articulado-exemplo` tem cabeçalho completo (brasão, casa e `textoCabecalho`), fonte 14 sem "reduzir espaço entre linhas" (como as emendas de comparação) e cinco autores (senador com cargo, senadora, deputado com cargo, deputada e um quinto senador: assinatura ímpar), além de revisões, comentários e as cinco pendências da especificação 13 do lexml-eta (só a caixa de pendências: revisões e comentários não têm caixa própria) (o `DocumentoArticuladoPdfGeneratorTest` gera variações dele com outras opções registrando JSON/XML alterados no `ConversorDocumentoArticuladoFake`) e as fixtures sem `OpcoesImpressao` usam o padrão (com brasão); nesses dois o JSON imita o do editor, com os espaços entre elementos inline que o `tojson` descarta reinseridos). Em todos os pares o XML foi gerado pelo `jsonix-lexml` 2.0.0 a partir do JSON, para ser exatamente o que o conversor produz. `ConversorDocumentoArticuladoCliTest` simula o executável com a própria JVM (`JsonixLexmlSimulado`), e `DocumentoArticuladoConteudoTransformerTest` testa a XSLT isoladamente (XML -> fragmento FO).
