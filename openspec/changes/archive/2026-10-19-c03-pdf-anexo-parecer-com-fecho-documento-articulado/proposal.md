# Proposal

## Why

A issue [#80](https://github.com/lexml/eta-backend-services/issues/80) (change `2026-10-14-c02`) fez o PDF do documento articulado em anexo de parecer (`lexedit:Metadado anexoParecer="true"`) omitir a justificação, o local e data e as assinaturas. A chefia redefiniu a regra: "O modo anexoParecer vai impedir apenas a impressão da Justificação, pode imprimir local, data e assinaturas se existirem." O ajuste vale só para o documento articulado; a emenda continua como está.

## What Changes

- Em anexo de parecer, o PDF continua sem a justificação (título, corpo e notas de rodapé dela), mas passa a imprimir o local e data e as assinaturas quando o documento os tiver, com as mesmas regras de qualquer documento.
- Sem local nem autoria (como o editor grava hoje no modo anexo de parecer), o PDF termina na articulação, como antes.
- Fixture `documento-anexo-parecer` com local, data e um parlamentar, para o PDF mostrar o caso novo.
- Sem mudança na emenda.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Anexo de parecer" passa a omitir só a justificação.

## Impact

- `FechoDocumentoArticulado` (deixa de esvaziar o fecho em anexo de parecer; a leitura do atributo continua, para a justificação).
- Fixture `documento-anexo-parecer` (JSON-first), testes `FechoDocumentoArticuladoTest` e `DocumentoArticuladoPdfGeneratorTest` e `CLAUDE.md`.
- XSLT, template, API pública e dependências sem mudança; sem impacto em emenda, parecer ou `lexmljsonix`.
