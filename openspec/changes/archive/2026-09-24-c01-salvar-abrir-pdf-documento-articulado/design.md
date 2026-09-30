# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual relevante:

- Emenda e parecer seguem o pipeline `VelocityTemplateProcessor` -> `FOPProcessor.processFOP` -> `PDFA` (pdfa-helper) -> substituição do `<check:hash>` pelo MD5. O `FOPProcessor` só embute o modelo para `TipoDocumento.EMENDA`/`PARECER` e mantém `fopFactory`/fontes em `private static`. O `VelocityTemplateProcessor` está acoplado a `Emenda`/`Parecer`.
- O `ConversorLexmlJsonixImpl` + `CliUtils` já chamam o `jsonix-lexml`, mas: engolem erros (só log), não verificam código de saída nem leem stderr, no Linux usam `inheritIO()` (o modo stdin/stdout devolve vazio — por isso o lexeditweb usa `usar-entrada-saida-padrao-durante-conversao=false`), usam o charset padrão da plataforma (quebra acentos no Windows/cp1252) e montam o comando como string para `cmd /c`/`bash -c`.
- Comportamento observado do `jsonix-lexml` 2.0.0 (win), em 24/09/2026, com `lexml-eta/docs/extensao-formato-lexml/documento-articulado-exemplo.json`: `toxml` seguido de `tojson` preserva o documento sem diferenças semânticas (só a ordem das chaves muda); o XML sai em UTF-8, sem declaração `<?xml?>`; com entrada inválida o processo **sai com código 0**, não gera saída e escreve o erro no stderr (`UnhandledPromiseRejectionWarning: Error: Element [...] is not known in this context`); com arquivo de entrada inexistente, idem.
- Restrição do projeto: os testes devem rodar em `mvn test` sem rede e sem o executável `jsonix-lexml`.
- Decisão do usuário: nada usado por emenda/parecer é reutilizado; compartilham-se apenas `pdfa-fonts/`, `static/img/` e `util/` (`EtaBackendException`, `BytesUtil`). Bibliotecas de terceiros (FOP, PDFBox, pdfa-helper, Velocity, Jackson, commons-io) são usadas normalmente.

## Goals / Non-Goals

**Goals:**
- Pipeline JSON -> (jsonix-lexml) XML -> PDF/A-3B com anexo e o inverso PDF -> XML -> (jsonix-lexml) JSON, em pacote próprio.
- Conversor robusto: erro detectado mesmo com código de saída 0, UTF-8, tempo limite, sem resíduos temporários.
- Template derivado do da emenda, com o código de impressão do texto preservado em comentários Velocity.
- Pipeline inteiro testável sem o executável.

**Non-Goals:**
- Formatação do texto da proposição no PDF (brasão, cabeçalho, articulação, assinaturas).
- Classes de domínio para o documento articulado; validação completa de esquema do LexML (fica a cargo do próprio `jsonix-lexml`).
- Anexos do documento (`lexedit:Anexos`) embutidos/concatenados ao PDF.
- Endpoints/beans no lexeditweb, distribuição dos executáveis e integração no front `lexml-eta`.
- Refatorar o `ConversorLexmlJsonix`/`CliUtils` existentes.

## Decisions

### D1. Pacote e componentes

```
br.gov.lexml.eta.etaservices.documentoarticulado
  conversor/
    ConversorDocumentoArticulado          (interface: jsonParaXml / xmlParaJson)
    ConversorDocumentoArticuladoCli       (impl. via executável jsonix-lexml)
  pdf/
    DocumentoArticuladoPdfGenerator       (API: generate(String json, OutputStream))
    DocumentoArticuladoTemplateProcessor  (Velocity próprio -> XSL-FO)
    DocumentoArticuladoFopProcessor       (FOP + PDFA + anexo; fábrica FOP e fontes próprias)
    DocumentoArticuladoFoHelper           (extração do XMP/pdfaid do XSL-FO)
  extracao/
    DocumentoArticuladoJsonExtractor      (API: extractJsonFromPdf(InputStream, Writer))
src/main/resources/documentoarticulado/
    template-velocity-documento-articulado.xml
    fop-documento-articulado.xconf
```

Nome `documentoarticulado` segue o termo do front (`DocumentoArticulado`, `documento-articulado.json`); `...etaservices.eta` seria ambíguo com a raiz `br.gov.lexml.eta`. As classes `pdf/*` são cópias adaptadas das equivalentes de emenda/parecer (isolamento pedido), reduzidas ao necessário: o processador FOP embute sempre um único anexo `documento-articulado.xml` e não trata `TipoDocumento`, `AnexoPDFA` nem concatenação.

*Alternativa descartada:* sobrecarga no `FOPProcessor` aceitando uma lista de `PDFAttachmentFile` — evitaria ~80 linhas de duplicação, mas contraria a decisão de não tocar/reutilizar o que emenda e parecer usam.

### D2. Montagem: POJOs manuais, sem auto-configuration

As três classes públicas são POJOs com dependências no construtor, como as demais de `etaservices`:

```java
new ConversorDocumentoArticuladoCli(Path executavel, Duration timeout)
new DocumentoArticuladoPdfGenerator(ConversorDocumentoArticulado conversor)
new DocumentoArticuladoJsonExtractor(ConversorDocumentoArticulado conversor)
```

A aplicação consumidora decide de onde vem o caminho (o lexeditweb pode reaproveitar sua `lexml-jsonix.cli`). Não se usa `LexmlJsonixProperties` nem se cria prefixo novo.

*Alternativa descartada:* auto-configuration com prefixo próprio — mais código e acoplamento a Spring sem ganho agora; pode ser acrescentada depois sem quebrar a API.

### D3. Conversor via arquivos temporários e `ProcessBuilder` com lista de argumentos

Para cada conversão: cria um diretório temporário, grava a entrada em UTF-8 (`entrada.json`/`entrada.xml`), executa `[executavel, "toxml"|"tojson", <entrada>, "-o", <saida>]` com `redirectError` para `stderr.txt` e `redirectOutput` para `stdout.txt` (descarta, mas evita bloqueio de pipe), aguarda com `waitFor(timeout)` e, no `finally`, remove o diretório. É falha (`EtaBackendException`) quando: o processo não inicia (executável inexistente/sem permissão), excede o tempo limite (`destroyForcibly`), termina com código != 0, ou o arquivo de saída não existe ou está vazio; a mensagem inclui o conteúdo do stderr (truncado a ~2000 caracteres).

*Por que arquivos e não stdin/stdout:* redirecionar stdout/stderr para arquivo elimina o risco de deadlock por buffer de pipe em documentos grandes sem precisar de threads de leitura, e o modo arquivo é o que o lexeditweb já usa em produção. *Alternativa:* stdin/stdout com threads de consumo — funciona (testado no Windows), mas é mais código para o mesmo resultado.

### D4. Validação mínima da entrada

O gerador faz `ObjectMapper.readTree` e verifica `name.localPart == "LexML"`, `name.namespaceURI == "http://www.lexml.gov.br/1.0"` e `value.metadado.identificacao.urn` não vazio; caso contrário, `EtaBackendException`. A validação estrutural fica a cargo do `jsonix-lexml` (erro propagado por D3). Toda a geração é feita em memória e só no fim os bytes são escritos no `OutputStream`, garantindo "nada escrito" em erro.

### D5. Contexto do template sem classes de domínio

O Velocity recebe:
- `$documento`: o JSON convertido para `Map<String,Object>` (Jackson), para navegação natural no template (`$documento.value.metadado.identificacao.urn`) e para o código comentado aproveitado depois;
- `$titulo`: epígrafe (`value.projetoNorma.norma.parteInicial.epigrafe.content`, strings concatenadas, espaços normalizados) ou, se ausente/vazia, a URN — já escapado para XML;
- `$dataIso`: data/hora de geração em ISO-8601 com fuso (usada em `xmp:CreateDate`, `ModifyDate`, `MetadataDate`);
- `$aplicacao`: valor fixo `"LexEdit"` quando `lexedit:Metadado.aplicacao` não estiver presente.

Sem `VelocityExtension`: os poucos helpers necessários (escape XML, data ISO) ficam em métodos do próprio `DocumentoArticuladoTemplateProcessor` ou são pré-calculados no contexto. O template é uma cópia do `template-velocity-emenda.xml` com: layout de página e bloco `<x:xmpmeta>` ativos (adaptados a `$titulo`/`$dataIso`/`$aplicacao`, descrição "Proposição legislativa", `pdfaid:part=3`/`conformance=B`, `check:hash` placeholder); todo o conteúdo impresso (cabeçalho/brasão, alertas, epígrafe, comandos, justificação, local/data, assinaturas) dentro de `#* ... *#` com a nota `NÃO RETIRAR — impressão do texto a adaptar ao documento articulado`; e um `<fo:block/>` vazio mantido no `fo:flow` (o FOP exige ao menos um bloco).

### D6. FOP e fontes próprios, arquivos de fonte compartilhados

`DocumentoArticuladoFopProcessor` monta sua própria `FopFactory` a partir de `documentoarticulado/fop-documento-articulado.xconf` (cópia do `fop.xconf`) e de um `ResourceResolver` próprio que serve as fontes de `/pdfa-fonts/` (recurso compartilhado). PDF/A-3B, versão 1.7, anexo `documento-articulado.xml` / `text/xml` / `AFRelationShip.SOURCE` via `PDFA.addAttachments`. O MD5 no `<check:hash>` é aplicado pelo `DocumentoArticuladoPdfGenerator` (lógica copiada de `PdfGeneratorBean`, usando `BytesUtil.lastIndexOf`).

### D7. Extração em memória com PDFBox

`DocumentoArticuladoJsonExtractor` carrega o PDF com PDFBox (`PDDocument.load`), percorre a árvore `EmbeddedFiles` do `PDDocumentNameDictionary` (incluindo `Kids`) procurando `documento-articulado.xml`, lê os bytes como UTF-8 e chama `xmlParaJson`. Falha de leitura do PDF ou anexo ausente -> `EtaBackendException("Não se trata de um arquivo gerado pelo editor de proposições.")`. O JSON devolvido pelo conversor é escrito no `Writer` sem reserialização (já é o formato do front).

*Alternativa descartada:* `PDFAttachmentHelper.extractAttachments` (usado por emenda/parecer) — exige gravar o PDF e os anexos em disco e hoje deixa o diretório temporário para trás.

### D8. Estratégia de testes sem o executável

- `ConversorDocumentoArticuladoFake` (em `src/test`) implementa a interface devolvendo pares fixos de fixtures: `documento-articulado-exemplo.json` <-> `documento-articulado-exemplo.xml`, copiados de `lexml-eta/docs/extensao-formato-lexml/` sendo o XML **gerado pelo `jsonix-lexml` 2.0.0** a partir do JSON (par consistente). O teste de ida e volta compara os JSON como árvores Jackson (`JsonNode.equals`), independente da ordem das chaves.
- `ConversorDocumentoArticuladoCli` é testado sem o executável real em: executável inexistente e caminho com espaços. Para simular "código 0 + saída vazia + stderr" e tempo limite sem depender de shell, o construtor aceita um `List<String>` de prefixo de comando em um construtor package-private, permitindo usar o próprio `java` (`ProcessHandle.current().info().command()`) executando uma classe de teste que reproduz esses comportamentos.
- Teste de integração opcional com o executável real, habilitado por `@EnabledIfSystemProperty(named = "jsonix-lexml.cli", matches = ".+")`, fazendo JSON -> PDF -> JSON com o conversor real.
- Utilitário manual `DocumentoArticuladoJson2PDF` (main em `src/test`, no estilo de `Json2PDF`) para gerar um PDF real e inspecionar em um validador PDF/A.

## Risks / Trade-offs

- [Duplicação de ~300 linhas de emenda/parecer (FOP, FO helper, hash)] -> aceita por decisão de isolamento; divergências futuras são intencionais. Registrar nos Javadocs de onde cada classe foi derivada.
- [Executável `jsonix-lexml` diferente de 2.0.0 no host] -> metadados `lexedit` no formato 2.0.0 falham na conversão; o erro é propagado com a mensagem do stderr. No lexeditweb os três executáveis já estão na 2.0.0 (SHA-256 conferidos em 24/09/2026), pendentes apenas de commit.
- [A 2.0.0 muda o JSON de alguns elementos para o fluxo existente de proposições do Senado (`AutorProjeto`, `Cargo`, `NomeAgente`, `NomeGrupo`, `NomePessoa`, `OrgaoJulgador`, `Tratamento` passam a `stringComIdType`; `DataJulgamento` a `dateComIdType`; `Rotulo` foi mantido como `xsd:string`)] -> o modelo do `lexml-eta` não lê esses campos, então o risco é baixo; confirmar com um teste manual de carga de proposição no lexml-emenda antes de publicar o lexeditweb. Não afeta esta change (o documento articulado já é gerado no formato 2.0.0).
- [Custo de iniciar o executável (~Node empacotado) a cada conversão] -> aceitável para salvar/abrir sob demanda; o tempo limite protege o host.
- [PDF sem texto parece "vazio" para o usuário] -> esperado nesta etapa; o template mantém o código para a formatação futura.
- [PDF/A-3B inválido sem nenhum glifo/fonte] -> o `fo:block` vazio não usa fonte; validar manualmente o PDF gerado (utilitário do D8) em um validador PDF/A (ex.: veraPDF) como parte das tarefas.
- [Fixture XML gerado com o executável de Windows] -> é o mesmo 2.0.0 das demais plataformas; o teste de integração opcional detecta divergência.

## Migration Plan

Mudança puramente aditiva na biblioteca; nenhum consumidor é afetado até registrar os novos beans. Rollback = não usar as novas classes / reverter o commit.
