# Tasks

## 1. Estrutura e fixtures

- [x] 1.1 Criar o pacote `br.gov.lexml.eta.etaservices.documentoarticulado` com os subpacotes `conversor`, `pdf` e `extracao` e o diretório `src/main/resources/documentoarticulado/`; verificar que `mvn -q compile` passa sem alterar nenhum arquivo existente (`git status` mostra apenas arquivos novos)
- [x] 1.2 Adicionar em `src/test/resources/documentoarticulado/` o `documento-articulado-exemplo.json` (copiado de `lexml-eta/docs/extensao-formato-lexml/`) e o `documento-articulado-exemplo.xml` gerado a partir dele com `jsonix-lexml` 2.0.0 (`toxml`); verificar que `tojson` do XML devolve JSON semanticamente igual ao original e que ambos estão em UTF-8

## 2. Conversor JSON <-> XML (jsonix-lexml)

- [x] 2.1 Criar a interface `ConversorDocumentoArticulado` (`jsonParaXml`, `xmlParaJson`, erros como `EtaBackendException`) com Javadoc do contrato; verificar pela compilação
- [x] 2.2 Implementar `ConversorDocumentoArticuladoCli` (construtor público `Path executavel, Duration timeout` e construtor package-private com prefixo de comando) conforme design D3: diretório temporário, UTF-8, `ProcessBuilder` com lista de argumentos, stdout/stderr para arquivo, tempo limite com `destroyForcibly`, falha em código != 0 ou saída ausente/vazia com stderr na mensagem, remoção do diretório no `finally`; verificar pelos testes de 2.3
- [x] 2.3 Escrever `ConversorDocumentoArticuladoCliTest` (JUnit 5, sem o executável real, usando o `java` corrente executando uma classe auxiliar de teste que simula o CLI) cobrindo: sucesso com acentos preservados, executável inexistente, código 0 + saída vazia + stderr (mensagem contém o stderr), código != 0, tempo limite excedido, caminho com espaços e ausência de diretórios temporários residuais; verificar com `mvn test -Dtest=ConversorDocumentoArticuladoCliTest`
- [x] 2.4 Criar em `src/test` o `ConversorDocumentoArticuladoFake` baseado nas fixtures de 1.2 (e com modo de falha configurável) para uso nos testes dos grupos 3 e 4; verificar com um teste simples do fake

## 3. Geração do PDF com documento-articulado.xml embutido

- [x] 3.1 Criar `documentoarticulado/fop-documento-articulado.xconf` (cópia do `fop.xconf`) e `DocumentoArticuladoFoHelper` (derivado do `FOHelper`); verificar com teste unitário que extrai `pdfaid:part`, `conformance`, `CreateDate` e destaca o `x:xmpmeta` de um XSL-FO mínimo
- [x] 3.2 Criar `template-velocity-documento-articulado.xml` a partir do `template-velocity-emenda.xml` conforme design D5 (layout e XMP ativos com `$titulo`/`$dataIso`/`$aplicacao`; toda a impressão do texto em `#* ... *#` com nota "NÃO RETIRAR"; `<fo:block/>` vazio no `fo:flow`) e `DocumentoArticuladoTemplateProcessor` (Velocity próprio, contexto `$documento` como `Map`, título pela epígrafe ou URN); verificar com `DocumentoArticuladoTemplateProcessorTest` que o resultado é XML bem formado, contém o título esperado nos dois casos (com e sem epígrafe), escapa `&`/`<` no título e não contém texto de dispositivos
- [x] 3.3 Criar `DocumentoArticuladoFopProcessor` (FopFactory e resolver de fontes próprios servindo `/pdfa-fonts/`, PDF/A-3B, versão 1.7, anexo único `documento-articulado.xml` `text/xml` `SOURCE`); verificar pelos testes de 3.5
- [x] 3.4 Criar `DocumentoArticuladoPdfGenerator.generate(String json, OutputStream)` com validação mínima (D4), conversão `jsonParaXml`, template, FOP, MD5 no `<check:hash>` e escrita no destino só ao final; verificar pelos testes de 3.5
- [x] 3.5 Escrever `DocumentoArticuladoPdfGeneratorTest` (JUnit 5 + PDFBox, com o conversor fake) cobrindo os cenários da spec de geração: anexo `documento-articulado.xml` com conteúdo igual ao XML convertido e acentos em UTF-8, PDF com ao menos uma página e sem o texto dos dispositivos (`PDFTextStripper`), título nos metadados, hash diferente do placeholder, JSON inválido / não LexML / falha do conversor lançando `EtaBackendException` sem bytes escritos; verificar com `mvn test -Dtest=DocumentoArticuladoPdfGeneratorTest`

## 4. Recuperação do JSON a partir do PDF

- [x] 4.1 Implementar `DocumentoArticuladoJsonExtractor.extractJsonFromPdf(InputStream, Writer)` conforme design D7 (PDFBox em memória, busca em `EmbeddedFiles` incluindo `Kids`, UTF-8, `xmlParaJson`, mensagens de erro da spec); verificar pelos testes de 4.2
- [x] 4.2 Escrever `DocumentoArticuladoJsonExtractorTest` (JUnit 5, conversor fake) cobrindo: ida e volta JSON -> PDF -> JSON semanticamente igual (comparação por `JsonNode`, incluindo `lexedit:Metadado`), PDF de emenda sem o anexo (`src/test/resources/test1.pdf` ou PDF gerado pelo `PdfGeneratorBean` em memória) e bytes que não são PDF lançando `EtaBackendException`; verificar com `mvn test -Dtest=DocumentoArticuladoJsonExtractorTest`
- [x] 4.3 Documentar o novo fluxo no `CLAUDE.md` (seção Arquitetura: pacote `documentoarticulado`, montagem manual dos POJOs, dependência do `jsonix-lexml` 2.0.0 e isolamento em relação a emenda/parecer); verificar pela revisão do diff

## 5. Verificação integrada

- [x] 5.1 Criar o teste de integração opcional `DocumentoArticuladoIntegracaoCliTest` (JUnit 5, `@EnabledIfSystemProperty(named = "jsonix-lexml.cli", matches = ".+")`) fazendo JSON -> PDF -> JSON com o executável real; verificar que é ignorado em `mvn test` e passa com `mvn test -Dtest=DocumentoArticuladoIntegracaoCliTest -Djsonix-lexml.cli=<caminho do jsonix-lexml 2.0.0>`
- [x] 5.2 Criar o utilitário manual `DocumentoArticuladoJson2PDF` (main em `src/test`, no estilo de `Json2PDF`) que recebe o caminho do executável e de um JSON e grava o PDF; gerar o PDF do exemplo e verificar manualmente: abre no leitor, exibe o anexo `documento-articulado.xml`, passa em um validador PDF/A-3B (ex.: veraPDF) — registrar o resultado na conclusão da task
  - Resultado (`jsonix-lexml-win.exe` 2.0.0): utilitário criado; `target/documento-articulado-exemplo.pdf` (14 KB) gerado em 28/09/2026 e JSON recuperado igual ao original. Inspeção estrutural com PDFBox: 1 página sem texto, catálogo 1.7 (cabeçalho `%PDF-1.4`, como na emenda), OutputIntent sRGB, sem criptografia, XMP `pdfaid` 3/B, `check:hash` preenchido, `/AF` com `documento-articulado.xml` (`AFRelationship=Source`, `text/xml`). **Validado no veraPDF como PDF/A-3B conforme** (28/09/2026, pelo desenvolvedor).
- [x] 5.3 Rodar `mvn clean install` e verificar que todos os testes (novos e existentes de emenda/parecer) passam e que nenhum arquivo existente fora do `CLAUDE.md` foi alterado
