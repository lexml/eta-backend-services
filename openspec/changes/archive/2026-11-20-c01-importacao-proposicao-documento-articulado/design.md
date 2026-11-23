# Design

## Context

O pacote `br.gov.lexml.eta.etaservices.documentoarticulado` já isola o fluxo do documento articulado do `lexml-eta` (sem compartilhar código com emenda/parecer), contando com `ConversorDocumentoArticulado` (JSON ↔ XML via `jsonix-lexml`) e `DocumentoArticuladoJsonExtractor` (extração de `documento-articulado.xml` em PDFs gerados pela lib).

Por outro lado, a importação de proposições na criação de novos documentos no `lexml-eta` precisa receber arquivos DOCX, TXT, XML (LexML) ou PDF (gerado pela própria biblioteca) e devolver um JSON (Jsonix) válido para `lerDocumentoArticulado`, aproveitando principalmente **ementa** e **articulação** (Cenário 2), enquanto usos de estruturação integral de normas/proposições legítimas demandam **parseamento completo** (Cenário 1).

## Goals / Non-Goals

**Goals:**
- Fornecer uma API Java clara no pacote `documentoarticulado.importacao` para parsear DOCX, TXT, XML e PDF e devolver o Jsonix gerado (`ResultadoParseProposicao` com `jsonix`, `xml` e `avisos`).
- Suportar os dois modos (`APROVEITAMENTO` como padrão e `COMPLETO`), mantendo o módulo isolado (POJO com `ConversorDocumentoArticulado`, sem `@Component`).
- Atualizar `lexml-parser-projeto-lei` para `1.15.8`.
- Garantir compatibilidade do Jsonix gerado com o validador `validarDocumentoArticulado` do `lexml-eta`.

**Non-Goals:**
- Alterar `EditorApiController` (fora do repositório) ou alterar `LexmlParser`/`LexmlParserImpl` (legado de emendas).
- Implementar OCR ou extração heurística de texto em PDFs arbitrários não gerados pela biblioteca (PDFs sem `documento-articulado.xml` devem ser rejeitados).
- Adicionar auto-configuration Spring para `documentoarticulado`.

## Decisions

### 1. Reutilização direta dos blocos de `lexml-parser-projeto-lei` com controle explícito do `Linker`
`ProjetoLeiParser.fromBlocks(metadado, blocks)` chama `parseArticulacao(..., useLinker = true, ...)` e `reconheceLinks` incondicionalmente. Sem `/usr/local/bin/linkertool` (como em Windows ou ambientes sem o binário), o `Linker` inicializa um `ActorSystem` Pekko com 8 atores e registra warnings para cada parágrafo.

**Decisão:** `ParserBlocosProposicao` orquestra as primitivas públicas da biblioteca (`Marcadores`, `rotuloParser`, `niveis`, `Block.joinParagraphs`, `ProjetoLeiParser.parseArticulacao(blocks, usarLinker, urnContexto)`, `ProjetoLeiParser.reconheceLinks`, `Validation`, `LexmlRenderer`), permitindo controlar `usarLinker` via `OpcoesParseProposicao` (padrão: autodetecta `/usr/local/bin/linkertool`, igual ao `LexmlParserImpl`).

### 2. Contorno do bug de inversão de linhas em `text/plain` do `XHTMLProcessor`
No `XHTMLProcessor` de `lexml-parser-projeto-lei`, o handler de `text/plain` acumula linhas consecutivas sem linha em branco em ordem invertida antes de juntar em `<p>`. Para proposições em `.txt`, isso inverteria a ordem dos artigos ou os aglutinaria no mesmo parágrafo.

**Decisão:** `LeitorArquivoProposicao` decodifica os bytes de `.txt` com UTF-8 estrito e fallback para Windows-1252 (evitando perda de `Art. 1º`/`§ 1º` e acentos em arquivos Windows), remove eventual BOM `\uFEFF`, quebra por `\r?\n|\r`, sanitiza com `XMLUtil.sanitize`, escapa `&`, `<` e `>`, envolve cada linha em `<p>...</p>` dentro de `<html><body><div>...</div></body></html>` e passa pelo `XHTMLProcessor` com MIME `application/xhtml+xml` antes de `Block.fromNodes`.

### 3. Leitura segura de DOCX via `DOCXReader` + `XHTMLProcessor`
Para `.docx`, `DOCXReader.readDOCX` extrai `word/document.xml` como XHTML preservando negrito, itálico, sobrescrito e subscrito e ignorando estilos de parágrafo (`1.15.8`). Chamá-lo antes do `XHTMLProcessor` permite capturar `Option.isEmpty()` e exceções de ZIP/XML corrompido e traduzi-los para `EtaBackendException` com mensagem amigável ao usuário.

### 4. Perfil amplo e extração robusta de ementa no modo `APROVEITAMENTO` (Cenário 2)
No Cenário 2, o usuário pode importar minutas de proposições novas, substitutivos, leis vigentes ou arquivos com apenas ementa + articulação (ou só articulação).

**Decisão:**
- Criar um `DocumentProfileOverride` sobre `ProjetoDeLeiNovaNomenclatura` com `epigrafeObrigatoria = false`, `preEpigrafePermitida = true`, `ementaAusente = true` e regex combinando todas as epígrafes conhecidas (proposições de SF/CD/CN, substitutivo/subemenda substitutiva e normas promulgadas como Lei, Lei Complementar, Decreto, Decreto-Lei, Resolução, Emenda Constitucional, Medida Provisória, Constituição, ADCT, Regimento), continuações com nome completo ou siglas (`PL`, `PLP`, `PEC`, `MPV`, etc.) e pós-epígrafes (`(Do Sr. ...)`, `Autoria...`).
- Permitir ainda o fornecimento externo de regexes adicionais de início de epígrafe, continuação e pós-epígrafe em `OpcoesParseProposicao` (por chamada ou configurados como padrão na instanciação de `DocumentoArticuladoParser`), com remoção automática de diacríticos e flag `(?iu)` e validação imediata de sintaxe no `build()`, somados aditivamente aos padrões do perfil tanto em `APROVEITAMENTO` quanto em `COMPLETO`.
- Se não houver epígrafe reconhecida e houver cabeçalhos informais separados por linhas em branco antes da ementa (ex.: `"MINUTA"` + linha vazia + ementa + preâmbulo/articulação), selecionar o último bloco contíguo anterior ao preâmbulo/articulação como ementa.
- `Marcadores.span(...)` descarta automaticamente fecho (`LocalData`), `Justificacao`, `Anexo`, `Legislacao` e `Assinatura` ao final da articulação.
- A ausência de dispositivos reconhecidos na articulação continua sendo erro fatal (`EtaBackendException`).

### 5. Compatibilidade de URN e estrutura com `lerDocumentoArticulado` do `lexml-eta`
O `lexml-eta` valida a URN pela expressão regular `^urn:lex:br:[^:\s]+:[^:\s]+:\d{4}(?:-\d{2}-\d{2})?;\d+(?:@[^!\s]+)?$` e exige `articulacao.lXhier` não vazio.

**Decisão:**
- Para DOCX e TXT, `Metadado` recebe `Id` com ano (informado ou ano corrente) e número (informado ou `999999`), produzindo URN válida.
- Para XML LexML (`ValidadorXmlLexml`), o documento é validado com `SAXReader` protegido contra XXE; no modo `APROVEITAMENTO`, se o atributo `URN` de `<Identificacao>` estiver ausente, vazio ou com o placeholder `LEXML_URN_ID`, apenas esse atributo é normalizado para uma URN válida (XMLs que já tenham URN válida permanecem intocados byte a byte, preservando `lexedit:Metadado`).

### 6. Reutilização de `DocumentoArticuladoJsonExtractor` para PDF
Extrair `extractXmlFromPdf(InputStream)` em `DocumentoArticuladoJsonExtractor` para ler `documento-articulado.xml` em memória via PDFBox sem duplicar a navegação na árvore `EmbeddedFiles`, e fazer `extractJsonFromPdf` delegar a ele.

### 7. Tratamento de avisos não-fatais vs erros fatais
Por padrão (`falharComAvisos = false`), falhas estruturais não-fatais retornadas por `Validation.validaEstrutura` (como descontinuidade de numeração `Art. 1º` → `Art. 3º`) são devolvidas em `ResultadoParseProposicao.getAvisos()` sem impedir a geração do Jsonix; quando `falharComAvisos = true`, lançam `EtaBackendException`. Erros de entrada inválida (formato não suportado, XML malformado/XXE, DOCX inválido, PDF sem anexo, alteração sem fecha-aspas, ausência de articulação/dispositivos) sempre lançam `EtaBackendException`.

## Risks / Trade-offs

- **Dependência de `lexml-parser-projeto-lei` `1.15.8`**: migra de Akka para Apache Pekko (`1.5.0`); em contrapartida, como o `Linker` só sobe quando `/usr/local/bin/linkertool` existe (ou `usarLinker=true`), não há inicialização desnecessária do `ActorSystem` no uso padrão.
- **Linhas quebradas dentro da ementa ou dispositivo no TXT**: cada quebra de linha vira um `<p>`, permitindo que `ProjetoLeiParser.parseArticulacao` una continuações em minúscula/vírgula e `Block.joinParagraphs` una os parágrafos contíguos da ementa sem correr o risco de inverter artigos.
- **Lombok + JDK 21 em ambiente local**: o POM segue usando Java 11 (conforme CI Jenkinsfile); no ambiente local com JDK 21 o build usa `-Dlombok.version=1.18.30`.

