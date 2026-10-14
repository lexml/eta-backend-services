# Proposal

## Why

A issue [#80](https://github.com/lexml/eta-backend-services/issues/80) pede que, no caso de `<lexedit:Metadado anexoParecer='true'>`, o PDF da proposição não imprima a justificação, o local, a data e a autoria; a ausência do atributo indica `anexoParecer=false`. É o comportamento da emenda (`template-velocity-emenda.xml`: justificação, fecho e assinaturas condicionados a `!$emenda.anexoParecer`). Hoje o PDF do documento articulado ignora o atributo. O editor (lexml-eta), no modo anexo de parecer, já não grava local, data e autoria, mas não retira a justificação do documento articulado: cabe ao PDF não imprimi-la.

## What Changes

- Com `anexoParecer` verdadeiro no `lexedit:Metadado`, o PDF **não imprime**: a justificação (título "JUSTIFICAÇÃO", corpo e as notas de rodapé dela), o local e data e as assinaturas, mesmo que o documento os tenha.
- Com o atributo ausente, falso ou de outro tipo, nada muda.
- Continuam impressos, como na emenda: cabeçalho, alertas e pendências, parte inicial e articulação.
- **Fixture nova** `documento-anexo-parecer` (procedimento JSON-first; o `jsonix-lexml` 2.0.0 preserva o atributo, verificado): documento do editor em modo anexo de parecer, com articulação e justificação (com nota de rodapé) e sem local, data e autoria, como o editor grava.
- Variações nos testes: `anexoParecer="false"` explícito, atributo ausente e `true` com local e autoria preenchidos.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: novo requisito "Anexo de parecer".

## Impact

- Leitura do atributo a partir do JSON (junto das demais leituras do `lexedit:Metadado`), passada à XSLT e ao template.
- `documento-articulado-conteudo.xsl` (novo `xsl:param`), `DocumentoArticuladoConteudoTransformer`, `DocumentoArticuladoPdfGenerator`, `DocumentoArticuladoTemplateProcessor` e `template-velocity-documento-articulado.xml`.
- Fixture nova, `ConversorDocumentoArticuladoFake`, testes (`DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoPdfGeneratorTest` e o da leitura) e `CLAUDE.md`.
- Sem mudança de API pública nem de dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
