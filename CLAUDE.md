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
  - **conteúdo:** `DocumentoArticuladoConteudoTransformer` aplica `documentoarticulado/documento-articulado-conteudo.xsl` (XSLT 1.0) ao `documento-articulado.xml` e gera um fragmento XSL-FO. Hoje imprime a parte inicial (epígrafe, ementa e preâmbulo) e a articulação básica (artigo, caput, parágrafo, inciso, alínea e item, com a formatação da citação de dispositivos da emenda), percorrida no modo `dispositivo` com o template nomeado `bloco-dispositivo`. Agrupadores são atravessados sem imprimir rótulo e nome. Ainda têm templates vazios, marcados com a parte da issue #72 que os trata: rótulo e nome de agrupadores (parte 3), `Alteracao` e `Omissis` (parte 4), links de remissões (parte 5), `Pena` e `TituloDispositivo` (parte 6). Usa `TransformerFactory.newDefaultInstance()` (XSLT do JDK) de propósito: o Xalan 2.7.2 do classpath trata `fo:*` como XSL e rejeita os atributos em XSLT 1.0.
  - **página:** `documentoarticulado/template-velocity-documento-articulado.xml` (derivado do da emenda) cuida do layout, do XMP e dos blocos da emenda ainda não adaptados (comentados como "NÃO RETIRAR"), e recebe o fragmento em `$conteudoFo`, sem reinterpretá-lo.
  - Tamanhos de fonte e espaçamentos vêm de `ParametrosImpressaoDocumentoArticulado` (hoje só os padrões: 14pt, destaque 16pt, entrelinha 150%, 0.6em; as opções de impressão do documento ainda não são lidas) e são passados ao template e à XSLT.
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

Documento articulado (`src/test/java/.../documentoarticulado/`): os testes não usam o `jsonix-lexml` real — `ConversorDocumentoArticuladoFake` devolve pares de fixtures de `src/test/resources/documentoarticulado/` (`comExemplo()`: `documento-articulado-exemplo.json`/`.xml`; `comDocumentosDeTeste()`: também os três documentos anexados à issue #72, `documento-com-articulacao-e-alteracao-de-norma`, `documento-com-capitulo-e-secao` e `documento-com-pena-e-titulo-de-dispositivo`). Em todos os pares o XML foi gerado pelo `jsonix-lexml` 2.0.0 a partir do JSON, para ser exatamente o que o conversor produz. `ConversorDocumentoArticuladoCliTest` simula o executável com a própria JVM (`JsonixLexmlSimulado`), e `DocumentoArticuladoConteudoTransformerTest` testa a XSLT isoladamente (XML -> fragmento FO).
