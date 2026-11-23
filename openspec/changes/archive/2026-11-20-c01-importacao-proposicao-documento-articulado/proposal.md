# Proposal

## Why

O editor de proposições `lexml-eta` permite criar uma nova proposição aproveitando o texto existente de um arquivo nos formatos **DOCX**, **TXT**, **XML (LexML)** ou **PDF** (gerado pela própria biblioteca com o `documento-articulado.xml` embutido), aguardando como resposta um documento articulado no formato JSON (Jsonix).

Até o momento, o `eta-backend-services` possuía apenas:
- `LexmlParserImpl` (pacote `parsing.lexml`), que executa apenas `ArticulacaoParser` sobre linhas de texto puro e preenche um template com epígrafe/ementa fixas (`"TEXTO PARSEADO"`), sem leitura de DOCX, sem reconhecimento de ementa, sem diferenciação entre parseamento completo e aproveitamento de texto, sem extração de PDF e desconectado do fluxo `documentoarticulado`;
- `DocumentoArticuladoJsonExtractor`, que já extrai o anexo XML de PDFs do documento articulado e converte para Jsonix, mas não expunha a extração do XML nem tratava DOCX, TXT ou XML diretamente.

Além disso, a dependência `lexml-parser-projeto-lei` estava na versão `1.14.1`, enquanto a versão `1.15.8` traz melhorias no `DOCXReader` (StAX, ignorando estilo de texto definido em estilo de parágrafo), suporte a `Pena` e novos padrões de epígrafe e preâmbulo.

## What Changes

- Atualização de `br.gov.lexml.parser.pl:lexml-parser-projeto-lei` no `pom.xml` de `1.14.1` para `1.15.8`.
- Exposição de `extractXmlFromPdf(InputStream)` em `DocumentoArticuladoJsonExtractor` para reaproveitar a leitura em memória do anexo `documento-articulado.xml`.
- Criação do novo subpacote `br.gov.lexml.eta.etaservices.documentoarticulado.importacao` com `DocumentoArticuladoParser` (POJO instanciado manualmente com `ConversorDocumentoArticulado`):
  - Recebe arquivos em **DOCX**, **TXT**, **XML (LexML)** ou **PDF** (por `TipoArquivoProposicao`, por `Content-Type` ou por método dedicado) e devolve o Jsonix gerado, o XML LexML e a lista de avisos de validação (`ResultadoParseProposicao`), além de conveniências para retornar diretamente a string Jsonix;
  - Suporta dois modos de parseamento (`ModoParseProposicao`):
    - **`APROVEITAMENTO`** (padrão, Cenário 2): focado em aproveitar **ementa** e **articulação** na criação de nova proposição no `lexml-eta` (descartando epígrafes de proposições/normas existentes e seções finais como justificação/assinatura, aceitando ausência de epígrafe/preâmbulo/ementa, garantindo URN válida para o editor e tolerando avisos não-fatais de técnica legislativa);
    - **`COMPLETO`** (Cenário 1): parseamento estrutural completo de proposição existente (epígrafe, ementa, preâmbulo e articulação) com perfil `DocumentProfile` específico e opção de validação por esquema XSD.

## Capabilities

### New Capabilities

- `importacao-proposicao-documento-articulado`: parseamento e importação de proposições em DOCX, TXT, XML (LexML) e PDF para Jsonix do documento articulado, contemplando os cenários de aproveitamento (ementa + articulação) e parseamento completo.

### Modified Capabilities

Nenhuma. O `LexmlParser` legado (`parsing.lexml`) e o `pdf-documento-articulado` existentes mantêm seus contratos inalterados.

## Impact

- **Dependências (`pom.xml`)**: `lexml-parser-projeto-lei` atualizado de `1.14.1` para `1.15.8`.
- **Código existente**:
  - `documentoarticulado/extracao/DocumentoArticuladoJsonExtractor.java`: novo método `extractXmlFromPdf(InputStream)`.
- **Código novo**:
  - `documentoarticulado/importacao/`: `DocumentoArticuladoParser`, `TipoArquivoProposicao`, `ModoParseProposicao`, `OpcoesParseProposicao`, `ResultadoParseProposicao`, `AvisoParseProposicao`, `LeitorArquivoProposicao`, `ParserBlocosProposicao`, `ValidadorXmlLexml`.
- **Testes**: novas suítes JUnit 5 em `documentoarticulado/importacao/` executáveis sem rede nem executável `jsonix-lexml`, além de teste de integração opt-in com o CLI real.
- **Hosts consumidores**: podem montar `new DocumentoArticuladoParser(conversor)` no padrão manual do módulo `documentoarticulado` e expor endpoints HTTP compatíveis com o componente de importação do `lexml-eta`, mantendo `EtaBackendException` como erro de negócio (HTTP 400).

