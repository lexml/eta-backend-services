# Como usar a API de Documento Articulado (Proposição) — referência

O pacote `br.gov.lexml.eta.etaservices.documentoarticulado` reúne os serviços de back-end para proposições editadas no componente `<lexml-eta>`: conversão entre LexML JSON (Jsonix) e LexML XML, geração de PDF/A-3B com `documento-articulado.xml` embutido e anexos, extração de XML/JSON a partir do PDF e importação/parseamento de arquivos `DOCX`, `TXT`, `XML` e `PDF` para o formato Jsonix.

Este documento descreve a API pública desses serviços (versão do projeto: `1.3.2-SNAPSHOT`): pré-requisitos, instanciação, classes, opções de parseamento, receitas de integração com o `<lexml-eta>`, tratamento de erros e armadilhas conhecidas.

> **Pacotes distintos:** o módulo `etaservices.documentoarticulado` é um conjunto de POJOs instanciados manualmente (sem auto-configuration do Spring). Para pesquisa de proposições existentes no Senado e conversão via auto-configuration Spring (`lexml-jsonix.*`), veja o [README principal](../README.md).

## 1. Instalação e pré-requisitos

### 1.1 Dependência Maven

Adicione `br.gov.lexml.eta:eta-backend-services` ao `pom.xml` da aplicação Java (requisito mínimo: **Java 11**):

```xml
<dependency>
    <groupId>br.gov.lexml.eta</groupId>
    <artifactId>eta-backend-services</artifactId>
    <version>1.3.2-SNAPSHOT</version>
</dependency>
```

### 1.2 Dependências externas em runtime

- **Executável `jsonix-lexml` (obrigatório na conversão JSON ↔ XML):** utilize a versão **2.0.0 ou superior** (exigida para preservar os metadados `lexedit:Metadado`), ou **2.4.0+** quando houver epígrafes em múltiplas linhas com `<br/>`. Os binários para Linux, macOS e Windows ficam nos [releases do jsonix-lexml](https://github.com/lexml/jsonix-lexml/releases/).
- **Linker externo `/usr/local/bin/linkertool` (opcional):** quando presente e executável, o `DocumentoArticuladoParser` o utiliza por padrão para resolver remissões legislativas no texto importado. Quando ausente (padrão em ambientes sem o binário), o linker é desabilitado automaticamente para evitar inicializar o `ActorSystem` sem necessidade.

## 2. Exemplo mínimo

Todos os serviços do pacote `documentoarticulado` dependem apenas da interface `ConversorDocumentoArticulado` e são montados manualmente:

```java
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticulado;
import br.gov.lexml.eta.etaservices.documentoarticulado.conversor.ConversorDocumentoArticuladoCli;
import br.gov.lexml.eta.etaservices.documentoarticulado.extracao.DocumentoArticuladoJsonExtractor;
import br.gov.lexml.eta.etaservices.documentoarticulado.importacao.DocumentoArticuladoParser;
import br.gov.lexml.eta.etaservices.documentoarticulado.importacao.ResultadoParseProposicao;
import br.gov.lexml.eta.etaservices.documentoarticulado.pdf.DocumentoArticuladoPdfGenerator;

// 1. Instanciar o conversor e os serviços
ConversorDocumentoArticulado conversor = new ConversorDocumentoArticuladoCli(
        Paths.get("/opt/lexml/jsonix-lexml"), Duration.ofSeconds(30));

DocumentoArticuladoParser parser = new DocumentoArticuladoParser(conversor);
DocumentoArticuladoPdfGenerator pdfGenerator = new DocumentoArticuladoPdfGenerator(conversor);
DocumentoArticuladoJsonExtractor pdfExtractor = new DocumentoArticuladoJsonExtractor(conversor);

// 2. Importar um DOCX para gerar Jsonix pronto para o <lexml-eta>
String jsonix;
try (InputStream docx = Files.newInputStream(Path.of("minuta.docx"))) {
    ResultadoParseProposicao resultado = parser.parseDocx(docx);
    jsonix = resultado.getJsonix();
    if (resultado.temAvisos()) {
        System.out.println("Avisos de técnica legislativa: " + resultado.getMensagensAvisos());
    }
}

// 3. Gerar PDF/A-3B da proposição (com documento-articulado.xml embutido)
ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
pdfGenerator.generate(jsonix, pdfOut);

// 4. Recuperar o Jsonix a partir do PDF gerado
StringWriter jsonRecuperado = new StringWriter();
try (InputStream pdfIn = new java.io.ByteArrayInputStream(pdfOut.toByteArray())) {
    pdfExtractor.extractJsonFromPdf(pdfIn, jsonRecuperado);
}
```

## 3. Conversão JSON ↔ XML (`conversor`)

Pacote: [`br.gov.lexml.eta.etaservices.documentoarticulado.conversor`](../src/main/java/br/gov/lexml/eta/etaservices/documentoarticulado/conversor)

| Tipo | Método | Descrição |
|---|---|---|
| `ConversorDocumentoArticulado` | `String jsonParaXml(String json)` | Converte `documento-articulado.json` (Jsonix) para LexML XML (`documento-articulado.xml`). |
| `ConversorDocumentoArticulado` | `String xmlParaJson(String xml)` | Converte LexML XML para Jsonix usado pelo `<lexml-eta>`. |
| `ConversorDocumentoArticuladoCli` | `new ConversorDocumentoArticuladoCli(Path executavel, Duration timeout)` | Executa o binário `jsonix-lexml` (`toxml` / `tojson`) via arquivo temporário isolado em UTF-8. |

Comportamento do `ConversorDocumentoArticuladoCli`:

- Cria um diretório temporário por conversão e o remove ao final (com até 20 tentativas de intervalo curto no Windows caso o sistema demore a liberar o arquivo).
- Como o `jsonix-lexml` pode sair com código `0` mesmo quando falha no `stderr`, saída ausente ou vazia sempre lança `EtaBackendException` com o conteúdo do `stderr` (limitado a 2000 caracteres).
- Tempo limite excedido ou falha ao iniciar o processo também lançam `EtaBackendException`; duração nula, zero ou negativa lança `IllegalArgumentException`.
- Para ambientes com conversor remoto (ex.: serviço HTTP), basta implementar a interface `ConversorDocumentoArticulado` e passá-la aos demais serviços.

## 4. Geração de PDF/A da proposição (`pdf`)

Pacote: [`br.gov.lexml.eta.etaservices.documentoarticulado.pdf`](../src/main/java/br/gov/lexml/eta/etaservices/documentoarticulado/pdf)

### 4.1 Métodos de `DocumentoArticuladoPdfGenerator`

Construtor: `new DocumentoArticuladoPdfGenerator(ConversorDocumentoArticulado conversor)`.

| Método | Descrição |
|---|---|
| `void generate(String json, OutputStream outputStream) throws IOException` | Valida o JSON, converte para XML, renderiza o PDF/A-3B em memória com `documento-articulado.xml` embutido, grava o hash MD5 no XMP e escreve no `outputStream`. |
| `void generate(String json, List<AnexoPDFA> anexos, OutputStream outputStream) throws IOException` | Mesmo comportamento, acrescentando os anexos ao PDF final e concatenando as páginas daqueles marcados com `imprimir = true`. |

### 4.2 Anexos (`AnexoPDFA`)

Reutiliza `br.gov.lexml.eta.etaservices.parecer.AnexoPDFA`:

| Campo | Tipo | Descrição |
|---|---|---|
| `nomeArquivo` | `String` | Nome do anexo gravado no PDF. Se `null`, assume `"anexo.pdf"`. O nome `"documento-articulado.xml"` (sem diferenciar maiúsculas) é reservado e rejeitado com `EtaBackendException`. |
| `file` | `InputStream` | Conteúdo binário do anexo. Lido por inteiro **uma única vez** antes da escrita no destino. Itens nulos na lista ou com `file == null` são ignorados. |
| `imprimir` | `boolean` | Se `true`, o anexo precisa ser um PDF válido e tem suas páginas concatenadas ao final do documento na ordem recebida (`PDFMergerUtility`). Todos os anexos (inclusive com `imprimir = false`) são embutidos no PDF com relação `Data`. |

Mapeamento de MIME dos anexos embutidos: `.pdf` → `application/pdf`; `.docx` → `application/vnd.openxmlformats-officedocument.wordprocessingml.document`; demais extensões → `application/octet-stream`. O `documento-articulado.xml` é sempre embutido com relação `Source` (`text/xml`).

> **Escrita atômica no destino:** validação do JSON, rejeição de nome reservado, leitura dos anexos e validação de PDF dos anexos imprimíveis ocorrem antes de gravar no `outputStream`. Em caso de erro (`EtaBackendException`), nada é escrito no stream de saída.

### 4.3 O que é renderizado no PDF

A partir de `documento-articulado.json` + XML convertido e dos metadados `lexedit:Metadado`:

- **Cabeçalho na 1ª página** e **opções de impressão** (`imprimirBrasao`, `textoCabecalho`, `reduzirEspacoEntreLinhas`, `tamanhoFonte` `14`/`16`/`18`), com nome da casa inferido pela URN (`senado.federal`, `camara.deputados`, `congresso.nacional`).
- **Caixa de alertas** com os itens de `lexedit:Pendencias` quando presentes.
- **Parte inicial** (`Epigrafe` inclusive em várias linhas com `<br/>`, `Ementa`, `Preambulo`) e **articulação** completa (agrupadores, artigos, parágrafos, incisos, alíneas, itens, `TituloDispositivo`, `Pena`, blocos `Alteracao` com aspas/notas e `Omissis`, remissões externas e internas clicáveis).
- **Justificação** (classes de alinhamento e recuo do Quill, notas de rodapé `NotaDeRodape`, listas ordenadas/não ordenadas, tabelas com `colspan`/`rowspan`, imagens `data:image/`, omissão de `<del>`), omitida automaticamente quando `anexoParecer = true` em `lexedit:Metadado`.
- **Fecho e assinaturas** (`local`, `data` em pt-BR ou apenas `"<local>,"` quando sem data válida, e tabela de signatários para `autoria` do tipo `"Parlamentar"`).
- **XMP com hash MD5** inserido sobre o placeholder `<check:hash>000...` após a junção dos anexos impressos.

### 4.4 Exemplos

```java
// PDF simples (sem anexos)
try (OutputStream out = Files.newOutputStream(Path.of("proposicao.pdf"))) {
    pdfGenerator.generate(jsonix, out);
}

// PDF com anexos (anexo1 concatenado nas páginas finais; anexo2 apenas embutido)
try (InputStream anexo1 = Files.newInputStream(Path.of("quadro.pdf"));
     InputStream anexo2 = Files.newInputStream(Path.of("dados.docx"));
     OutputStream out = Files.newOutputStream(Path.of("proposicao-com-anexos.pdf"))) {

    List<AnexoPDFA> anexos = Arrays.asList(
            AnexoPDFA.builder().nomeArquivo("quadro.pdf").file(anexo1).imprimir(true).build(),
            AnexoPDFA.builder().nomeArquivo("dados.docx").file(anexo2).imprimir(false).build()
    );
    pdfGenerator.generate(jsonix, anexos, out);
}
```

## 5. Extração a partir de PDF (`extracao`)

Pacote: [`br.gov.lexml.eta.etaservices.documentoarticulado.extracao`](../src/main/java/br/gov/lexml/eta/etaservices/documentoarticulado/extracao)

Construtor: `new DocumentoArticuladoJsonExtractor(ConversorDocumentoArticulado conversor)`.

| Método | Retorno | Descrição |
|---|---|---|
| `extractXmlFromPdf(InputStream pdfStream) throws IOException` | `String` | Lê em memória (sem arquivos temporários) o conteúdo UTF-8 do anexo `documento-articulado.xml` embutido no PDF. |
| `extractJsonFromPdf(InputStream pdfStream, Writer jsonWriter) throws IOException` | `void` | Extrai `documento-articulado.xml`, converte para JSON via `ConversorDocumentoArticulado.xmlParaJson` e grava no `jsonWriter` (com `flush()`). |

**Múltiplos anexos no PDF:** o extrator percorre recursivamente toda a árvore `EmbeddedFiles` do catálogo PDF (incluindo nós `Kids`) procurando especificamente pela chave `"documento-articulado.xml"`, ignorando quaisquer outros arquivos anexados (`.pdf`, `.docx`, etc.). Se o arquivo não for um PDF válido ou não contiver `"documento-articulado.xml"`, lança `EtaBackendException("Não se trata de um arquivo gerado pelo editor de proposições.")`.

```java
StringWriter jsonWriter = new StringWriter();
try (InputStream pdf = Files.newInputStream(Path.of("proposicao-com-anexos.pdf"))) {
    pdfExtractor.extractJsonFromPdf(pdf, jsonWriter);
}
String jsonRecuperado = jsonWriter.toString();

// Ou apenas o XML bruto embutido no PDF:
try (InputStream pdf = Files.newInputStream(Path.of("proposicao-com-anexos.pdf"))) {
    String xmlEmbutido = pdfExtractor.extractXmlFromPdf(pdf);
}
```

## 6. Importação e parseamento de proposições para Jsonix (`importacao`)

Pacote: [`br.gov.lexml.eta.etaservices.documentoarticulado.importacao`](../src/main/java/br/gov/lexml/eta/etaservices/documentoarticulado/importacao)

### Construtores de `DocumentoArticuladoParser`

- `new DocumentoArticuladoParser(ConversorDocumentoArticulado conversor)` — usa `OpcoesParseProposicao.aproveitamento()` como padrão quando `opcoes` não for passado na chamada.
- `new DocumentoArticuladoParser(ConversorDocumentoArticulado conversor, OpcoesParseProposicao opcoesPadrao)` — define opções padrão reutilizadas por todas as chamadas que não informem `opcoes` (útil para configurar uma única vez listas customizadas de regex de epígrafe no bean da aplicação).

### Métodos públicos de `DocumentoArticuladoParser`

| Método | Retorno | Descrição |
|---|---|---|
| `parse(InputStream arquivo, TipoArquivoProposicao tipo)` | `ResultadoParseProposicao` | Despacha pelo tipo explícito usando as opções padrão. |
| `parse(InputStream arquivo, TipoArquivoProposicao tipo, OpcoesParseProposicao opcoes)` | `ResultadoParseProposicao` | Despacha pelo tipo explícito com `opcoes`. |
| `parsePorContentType(InputStream arquivo, String contentType)` | `ResultadoParseProposicao` | Resolve `TipoArquivoProposicao` via `Content-Type` HTTP e executa o parse com as opções padrão. |
| `parsePorContentType(InputStream arquivo, String contentType, OpcoesParseProposicao opcoes)` | `ResultadoParseProposicao` | Mesma resolução de `Content-Type` com `opcoes` explícitas. |
| `parseDocx(InputStream docx [, OpcoesParseProposicao opcoes])` | `ResultadoParseProposicao` | Lê arquivo Word `.docx` e converte para XML LexML + Jsonix. |
| `parseTxt(InputStream txt [, OpcoesParseProposicao opcoes])` | `ResultadoParseProposicao` | Lê arquivo texto `.txt` (UTF-8 com fallback Windows-1252) e converte para XML LexML + Jsonix. |
| `parseTxt(String texto [, OpcoesParseProposicao opcoes])` | `ResultadoParseProposicao` | Recebe texto já em memória (equivalente à entrada `.txt`) e converte para XML LexML + Jsonix. |
| `parseXml(InputStream xml [, OpcoesParseProposicao opcoes])` | `ResultadoParseProposicao` | Valida e normaliza XML LexML de stream e converte para Jsonix. |
| `parseXml(String xml [, OpcoesParseProposicao opcoes])` | `ResultadoParseProposicao` | Valida e normaliza XML LexML em memória e converte para Jsonix. |
| `parsePdf(InputStream pdf)` | `ResultadoParseProposicao` | Extrai `documento-articulado.xml` embutido em PDF gerado pela lib e converte para Jsonix (`avisos` vazio). |
| `parseParaJson(InputStream arquivo, TipoArquivoProposicao tipo [, OpcoesParseProposicao opcoes])` | `String` | Atalho para `parse(...).getJsonix()`. |
| `parsePorContentTypeParaJson(InputStream arquivo, String contentType [, OpcoesParseProposicao opcoes])` | `String` | Atalho para `parsePorContentType(...).getJsonix()`. |

### 6.1 Formatos aceitos (`TipoArquivoProposicao`)

| Constante | `getExtensao()` | `getContentType()` | Regras e comportamento |
|---|---|---|---|
| `DOCX` | `"docx"` | `"application/vnd.openxmlformats-officedocument.wordprocessingml.document"` | Extraído com `DOCXReader` preservando `<b>`, `<i>`, `<sup>`, `<sub>`. ZIP sem `word/document.xml` ou corrompido lança `EtaBackendException`. |
| `TXT` | `"txt"` | `"text/plain"` | Decodifica UTF-8 estrito com fallback `windows-1252`, remove BOM `\uFEFF`, preserva a ordem exata das linhas (contornando bug de inversão de `text/plain` do `XHTMLProcessor`) e normaliza caracteres inválidos para XML 1.0. |
| `XML` | `"xml"` | `"application/xml"` | Aceita também `text/xml` em `porContentType`. Leitor anti-XXE (`DOCTYPE` proibido), exige elemento raiz `<LexML>` (`http://www.lexml.gov.br/1.0`) e `<ProjetoNorma>` com `<Articulacao>` não vazia, preservando `lexedit:Metadado`. No modo `APROVEITAMENTO`, cria/normaliza `URN` ausente, vazia, inválida ou com `LEXML_URN_ID`. |
| `PDF` | `"pdf"` | `"application/pdf"` | Apenas PDFs gerados pela biblioteca que contenham o anexo `documento-articulado.xml` (ignora demais anexos do PDF). PDF comum sem o anexo lança `EtaBackendException`. |

Métodos utilitários de `TipoArquivoProposicao`:

- `TipoArquivoProposicao.porContentType(String contentType)` — ignora parâmetros após `;` (ex.: `"text/plain; charset=utf-8"` → `TXT`) e aceita `"text/xml"`. Nulo, vazio ou não suportado lança `EtaBackendException`.
- `TipoArquivoProposicao.porExtensao(String nomeArquivoOuExtensao)` — aceita nome completo (`"minuta.docx"`) ou extensão com/sem ponto (`".txt"`, `"XML"`). Nulo, vazio ou não suportado lança `EtaBackendException`.

### 6.2 Modos de parseamento (`ModoParseProposicao`)

| Aspecto | `APROVEITAMENTO` (Cenário 2 — padrão) | `COMPLETO` (Cenário 1) |
|---|---|---|
| **Objetivo** | Aproveitar **ementa** e **articulação** de minutas ou normas existentes ao iniciar uma nova proposição no `<lexml-eta>` (que gera sua própria epígrafe e preâmbulo). | Parsear integralmente uma proposição legítima existente (`Epigrafe`, `Ementa`, `Preambulo`, `Articulacao`) segundo um perfil oficial do LexML. |
| **Epígrafes reconhecidas por padrão** | Proposições de SF/CD/CN (`PROJETO DE LEI`, `PLP`, `PDL`, `PEC`, `PRC`, `PLV`), `SUBSTITUTIVO`, `SUBEMENDA SUBSTITUTIVA`, normas promulgadas (`LEI`, `LEI COMPLEMENTAR`, `LEI DELEGADA`, `DECRETO`, `DECRETO-LEI`, `MEDIDA PROVISÓRIA`, `EMENDA CONSTITUCIONAL`, `RESOLUÇÃO`, `CONSTITUIÇÃO`, `ADCT`, `REGIMENTO`) e continuações em duas linhas inclusive com siglas (`PL`, `PLP`, `PEC`, `MPV`, `PDL`, `PRC`, etc.). | Definidas pelo `DocumentProfile` resolvido (`autoridade` + `tipoNorma`), somadas a eventuais regexes adicionais configurados em `OpcoesParseProposicao`. |
| **Ausência de epígrafe / preâmbulo / ementa** | Sempre aceita documentos sem epígrafe, sem preâmbulo ou com cabeçalho informal seguido de linha em branco e ementa (aproveita o último bloco contíguo antes do preâmbulo/articulação), bem como documentos só com articulação. | Segue `epigrafeObrigatoria`, `preEpigrafePermitida` e `ementaAusente` do `DocumentProfile` escolhido; falha se uma parte obrigatória estiver ausente. |
| **Seções finais (fecho, assinatura, justificação)** | Descartadas automaticamente (`LocalData`, `Justificacao`, `Anexo`, `Legislacao`, `Assinatura`). | Descartadas automaticamente (`LocalData`, `Justificacao`, `Anexo`, `Legislacao`, `Assinatura`). |
| **URN gerada** | Em DOCX/TXT usa ano/número informados ou `<anoAtual>;999999`. Em XML, normaliza URN ausente, vazia, inválida ou com `LEXML_URN_ID` para `^urn:lex:br:[^:\s]+:[^:\s]+:\d{4}(?:-\d{2}-\d{2})?;\d+(?:@[^!\s]+)?$`, aceita por `lerDocumentoArticulado`. | Usa os metadados do perfil/`OpcoesParseProposicao`; em XML preserva a URN original intacta. Pode refazer a epígrafe (`remakeEpigrafe`) se `numero` e `ano` forem informados. |
| **Validação de esquema XSD (`validarSchema`)** | Desabilitada por padrão (`false`); pode ser habilitada no builder. | Desabilitada por padrão (`false`); pode ser habilitada no builder. |
| **Dispositivos ausentes ou erro léxico (`Alteracao` sem fechar aspas)** | Lança `EtaBackendException`. | Lança `EtaBackendException`. |

### 6.3 Opções e regexes customizados (`OpcoesParseProposicao`)

Fábricas estáticas:

- `OpcoesParseProposicao.aproveitamento()` — Cenário 2 com todos os padrões padrão.
- `OpcoesParseProposicao.completo()` — Cenário 1 usando perfil padrão `ProjetoDeLeiNovaNomenclatura` (com `validarSchema = false`; habilite pelo builder).
- `OpcoesParseProposicao.completo(String autoridade, String tipoNorma)` — Cenário 1 buscando perfil registrado em `DocumentProfileRegister` (ex.: `"senado.federal"`, `"projeto.lei;pls"`; `"camara.deputados"`, `"projeto.lei;pl"`; `"congresso.nacional"`, `"medida.provisoria;mpv"`). Perfil inexistente lança `EtaBackendException`.

Métodos de `OpcoesParseProposicao.builder()`:

| Método do Builder | Padrão | Descrição |
|---|---|---|
| `modo(ModoParseProposicao)` | `APROVEITAMENTO` | Define `APROVEITAMENTO` ou `COMPLETO`. |
| `autoridade(String)` | `null` (`senado.federal` fallback) | Fragmento de autoridade da URN (ex.: `"senado.federal"`, `"camara.deputados"`, `"congresso.nacional"`, `"federal"`). |
| `tipoNorma(String)` | `null` (`projeto.lei;pl` fallback) | Fragmento do tipo na sintaxe URN (ex.: `"projeto.lei;pls"`, `"projeto.lei.complementar;plp"`). |
| `numero(Integer)` / `ano(Integer)` | `999999` / ano corrente | Número e ano usados na URN (e no `remakeEpigrafe` em `COMPLETO` quando ambos forem informados). |
| `usarLinker(Boolean)` | `null` (autodetecta) | `null` usa linker somente se `/usr/local/bin/linkertool` existir; `true`/`false` força o comportamento explicitamente. |
| `validarSchema(boolean)` | `false` (em ambos os modos) | Executa `Validation.validaComSchema` ao final do parse. |
| `falharComAvisos(boolean)` | `false` | Se `true`, converte qualquer aviso não-fatal em `EtaBackendException`. |
| `regexEpigrafeAdicionais(Collection<String>)` / `adicionarRegexEpigrafe(String...)` | vazio | Expressões regulares adicionais de início de epígrafe (somadas às padrão e também incluídas na detecção de continuação). `regex*Adicionais` substitui a lista no builder; `adicionarRegex*` acrescenta. |
| `regexEpigrafeContinuacaoAdicionais(Collection<String>)` / `adicionarRegexEpigrafeContinuacao(String...)` | vazio | Expressões regulares adicionais para linhas seguintes em epígrafes de múltiplas linhas (somadas às padrão). |
| `regexPosEpigrafeAdicionais(Collection<String>)` / `adicionarRegexPosEpigrafe(String...)` | vazio | Expressões regulares adicionais para linhas pós-epígrafe descartadas antes da ementa (somadas às padrão). |

Regras importantes dos regexes customizados:

- **Semântica aditiva:** somam-se às regras embutidas do modo `APROVEITAMENTO` ou do `DocumentProfile` em `COMPLETO`, sem remover nenhum padrão existente.
- **Normalização automática:** como o parser casa contra `Paragraph.text` (minúsculas e sem diacríticos), o builder e o parser removem acentos via `Normalizer.Form.NFD` (preservando `º`, `°` e escapes como `\S`, `\D`, `\W`) e aplicam a flag `(?iu)`. Assim, `"^INDICAÇÃO\\b"` casa naturalmente com `"INDICAÇÃO Nº 42, DE 2026"`.
- **Validação imediata:** entradas `null` ou em branco são ignoradas; qualquer expressão regular sintaticamente inválida falha imediatamente no `.build()` lançando `EtaBackendException`.

### 6.4 Resultado e avisos (`ResultadoParseProposicao` e `AvisoParseProposicao`)

`ResultadoParseProposicao`:

| Método | Retorno | Descrição |
|---|---|---|
| `getJsonix()` | `String` | JSON Jsonix resultante, pronto para envio ao `<lexml-eta>` (`documentoImportado` ou `abrirDocumentoArticulado`). |
| `getXml()` | `String` | XML LexML intermediário (ou extraído/normalizado). |
| `getAvisos()` | `List<AvisoParseProposicao>` | Lista imutável de avisos não-fatais emitidos por `Validation.validaEstrutura` e/ou `validaComSchema`. |
| `temAvisos()` | `boolean` | Indica se houve ao menos um aviso. |
| `getMensagensAvisos()` | `List<String>` | Representação textual formatada (`tipo: mensagem (posições)`). |

Cada `AvisoParseProposicao` expõe: `getCodigo()` (`int`), `getTipo()` (`String`), `getCategoria()` (`String`), `getMensagem()` (`String`) e `getPosicoes()` (`List<String>`). Por padrão (`falharComAvisos = false`), descontinuidades não-fatais como `Art. 1º` → `Art. 3º` produzem o Jsonix normalmente e retornam os avisos em `getAvisos()`.

## 7. Receitas de integração

### 7.1 Configuração manual como beans Spring Boot (`@Configuration`)

```java
@Configuration
public class DocumentoArticuladoConfig {

    @Bean
    public ConversorDocumentoArticulado conversorDocumentoArticulado(
            @Value("${lexml-jsonix.cli}") Path jsonixCli,
            @Value("${lexml-jsonix.timeout:60s}") Duration timeout) {
        return new ConversorDocumentoArticuladoCli(jsonixCli, timeout);
    }

    @Bean
    public DocumentoArticuladoPdfGenerator documentoArticuladoPdfGenerator(
            ConversorDocumentoArticulado conversor) {
        return new DocumentoArticuladoPdfGenerator(conversor);
    }

    @Bean
    public DocumentoArticuladoJsonExtractor documentoArticuladoJsonExtractor(
            ConversorDocumentoArticulado conversor) {
        return new DocumentoArticuladoJsonExtractor(conversor);
    }

    @Bean
    public DocumentoArticuladoParser documentoArticuladoParser(
            ConversorDocumentoArticulado conversor,
            @Value("${eta.importacao.epigrafes-extras:}") List<String> epigrafesExtras) {
        OpcoesParseProposicao opcoesPadrao = OpcoesParseProposicao.builder()
                .modo(ModoParseProposicao.APROVEITAMENTO)
                .regexEpigrafeAdicionais(epigrafesExtras)
                .build();
        return new DocumentoArticuladoParser(conversor, opcoesPadrao);
    }
}
```

### 7.2 Endpoint `POST` para `urlImportacaoArquivo` do `<lexml-nova-proposicao>`

O componente `<lexml-nova-proposicao>` (veja a seção 11 de [api-componente-lexml-eta.md](../../lexml-eta/docs/api-componente-lexml-eta.md)) envia o binário direto no corpo do `POST` (não `multipart`) com o header `Content-Type`, esperando `200` com o Jsonix no corpo e `400` em texto puro quando o arquivo é recusado:

```java
@RestController
@RequestMapping("/api/documento-articulado")
public class DocumentoArticuladoController {

    private final DocumentoArticuladoParser parser;
    private final DocumentoArticuladoPdfGenerator pdfGenerator;
    private final DocumentoArticuladoJsonExtractor jsonExtractor;

    public DocumentoArticuladoController(
            DocumentoArticuladoParser parser,
            DocumentoArticuladoPdfGenerator pdfGenerator,
            DocumentoArticuladoJsonExtractor jsonExtractor) {
        this.parser = parser;
        this.pdfGenerator = pdfGenerator;
        this.jsonExtractor = jsonExtractor;
    }

    @PostMapping(value = "/importacao", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> importarArquivo(
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            InputStream corpoArquivo) throws IOException {
        try {
            String jsonix = parser.parsePorContentTypeParaJson(corpoArquivo, contentType);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonix);
        } catch (EtaBackendException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        }
    }

    @PostMapping(value = "/pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarPdf(@RequestBody String jsonix) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdfGenerator.generate(jsonix, out);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    @PostMapping(value = "/pdf/json", consumes = MediaType.APPLICATION_PDF_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> extrairJsonDoPdf(InputStream pdf) throws IOException {
        StringWriter writer = new StringWriter();
        jsonExtractor.extractJsonFromPdf(pdf, writer);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(writer.toString());
    }
}
```

> **Endpoint de avisos no front-end:** caso a aplicação host use o callback `onImportarArquivo` (ou um endpoint próprio fora do `<lexml-nova-proposicao>`) e queira exibir avisos não-fatais de técnica legislativa ao usuário, chame `parser.parsePorContentType(corpoArquivo, contentType)` e devolva um DTO contendo `resultado.getJsonix()` e `resultado.getMensagensAvisos()`.

### 7.3 Parseamento completo de proposição existente (Cenário 1)

```java
OpcoesParseProposicao opcoesCompletas = OpcoesParseProposicao.builder()
        .modo(ModoParseProposicao.COMPLETO)
        .autoridade("senado.federal")
        .tipoNorma("projeto.lei;pls")
        .numero(123)
        .ano(2025)
        .validarSchema(true)
        .falharComAvisos(true) // falha se houver salto de numeração ou erro de schema
        .build();

try (InputStream docx = Files.newInputStream(Path.of("pls-123-2025.docx"))) {
    ResultadoParseProposicao resultado = parser.parseDocx(docx, opcoesCompletas);
    String xmlEstruturado = resultado.getXml();
    String jsonix = resultado.getJsonix();
}
```

### 7.4 Suportar padrão de epígrafe incomum sem alterar a biblioteca

```java
OpcoesParseProposicao opcoesCustom = OpcoesParseProposicao.builder()
        .modo(ModoParseProposicao.APROVEITAMENTO)
        .adicionarRegexEpigrafe("^INDICAÇÃO\\b", "^REQUERIMENTO\\s+Nº")
        .adicionarRegexEpigrafeContinuacao("^\\(?\\s*REFERENTE\\s+AO\\s+PROCESSO\\b")
        .adicionarRegexPosEpigrafe("^Relatoria:.*")
        .build();

ResultadoParseProposicao resultado = parser.parseTxt(textoComIndicacao, opcoesCustom);
// A epígrafe é isolada em <Epigrafe>, sem contaminar <Ementa>
```

## 8. Tratamento de erros

| Exceção | Quando ocorre | Tratamento sugerido em API REST |
|---|---|---|
| `EtaBackendException` | Entrada de negócio ou formato inválida: `Content-Type`/extensão não suportada, DOCX corrompido, TXT vazio, XML malformado/`DOCTYPE`/não-LexML/sem articulação, ausência de dispositivos reconhecidos, `Alteracao` sem fechamento de aspas, regex inválido no `Builder`, perfil desconhecido em `COMPLETO`, PDF sem `documento-articulado.xml`, anexo com nome reservado ou não-PDF imprimível, falha/timeout do `jsonix-lexml` ou avisos quando `falharComAvisos = true`. | Retornar `400 Bad Request` (`text/plain` ou JSON de erro com `e.getMessage()`, nunca devolvendo stacktrace). Para falhas do processo `jsonix-lexml`, avaliar log detalhado e `400`/`500` conforme política do sistema. |
| `IOException` | Falha de leitura ou escrita nos streams (`InputStream`, `OutputStream`, `Writer`). | `500 Internal Server Error` (ou `400` se causada por desconexão do cliente na leitura do upload). |
| `NullPointerException` / `IllegalArgumentException` | Dependência obrigatória nula no construtor, argumento obrigatório `null` (`arquivo`, `tipo`, `jsonWriter`, etc.) ou comando/timeout inválidos no CLI. | Erro de programação do chamador (`500`). |

## 9. Armadilhas conhecidas

- **Versão do `jsonix-lexml`:** versões `< 2.0.0` não conhecem o namespace `lexedit:Metadado` e falham na conversão de documentos salvos pelo `<lexml-eta>`. Para imprimir/converter epígrafes com quebra de linha (`<br/>`), use `2.4.0+`.
- **Fechamento de streams:** nenhum método de `DocumentoArticuladoPdfGenerator`, `DocumentoArticuladoJsonExtractor` ou `DocumentoArticuladoParser` fecha o `InputStream`, `OutputStream` ou `Writer` recebido. Feche-os no chamador com `try`-with-resources.
- **Streams de `AnexoPDFA` lidos uma vez só e PDF/A estrito:** cada `AnexoPDFA.file` é consumido integralmente em memória durante `generate`. Ao combinar anexos de terceiros ao PDF gerado, o arquivo resultante pode deixar de atender à conformidade estrita PDF/A-3B.
- **Anexo imprimível que também foi gerado pelo editor:** embora anexos com o nome `documento-articulado.xml` sejam bloqueados na entrada, se um PDF anexado com `imprimir = true` for *outro PDF já gerado pelo próprio `eta-backend-services`*, o merge de páginas é iniciado a partir do PDF principal como primeira fonte. Na prática, anexe apenas arquivos auxiliares às proposições.
- **Travessões em regexes customizados:** `lexml-parser-projeto-lei` converte travessões (`–`, `—`) para hífen (`-`) durante a normalização interna de `Paragraph.text`. Em expressões customizadas passadas em `OpcoesParseProposicao`, prefira `-` em vez de `–` ou `—`.
- **Sem estado Spring nem `application.properties` próprio:** diferentemente de `br.gov.lexml.eta.lexmljsonix`, nada em `documentoarticulado` é registrado automaticamente no contexto Spring; as configurações de timeout, caminho do executável e regexes padrão precisam ser ligadas pela aplicação consumidora.

## 10. Referências

- Código-fonte do módulo: [`src/main/java/.../documentoarticulado/`](../src/main/java/br/gov/lexml/eta/etaservices/documentoarticulado)
- Testes unitários e de integração CLI: [`src/test/java/.../documentoarticulado/`](../src/test/java/br/gov/lexml/eta/etaservices/documentoarticulado)
- Guia geral e serviços `lexmljsonix`: [`README.md`](../README.md) e [`CLAUDE.md`](../CLAUDE.md)
- Especificações OpenSpec: [`openspec/specs/`](../openspec/specs) e [`openspec/changes/2026-11-20-c01-importacao-proposicao-documento-articulado/`](../openspec/changes/2026-11-20-c01-importacao-proposicao-documento-articulado)
- Componente front-end `<lexml-eta>`: [`lexml-eta/docs/api-componente-lexml-eta.md`](../../lexml-eta/docs/api-componente-lexml-eta.md)
- Conversor JSON ↔ XML: [github.com/lexml/jsonix-lexml](https://github.com/lexml/jsonix-lexml)

