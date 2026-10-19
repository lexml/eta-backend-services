# Proposal

## Why

A issue [#81](https://github.com/lexml/eta-backend-services/issues/81) pede que, no PDF da proposição sem data, o fecho mostre só o local, mantendo a vírgula ("Sala das Sessões,"), e não os espaços para preenchimento manual do dia, do mês e do ano. Hoje, seguindo a emenda (decisão da change `2026-10-13-c03`, issue #76), o fecho sai como "Sala das Sessões, ___ de ___ de ___.". O pedido coincide com a especificação 03 do lexml-eta ("Caso a data não seja informada, o texto apresentará apenas o local, seguido por vírgula") e com o texto que o editor grava no `LocalDataFecho`.

## What Changes

- Com `data` vazia, ausente ou inválida (fora de `aaaa-mm-dd`), o fecho passa a ser "`<local>`," (o local seguido de vírgula, sem espaços para preencher e sem ponto final).
- Com data válida, nada muda ("`<local>`, `<data por extenso>`.").
- Sem impacto na emenda: só o PDF do documento articulado muda.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Local e data" troca os espaços para preencher pelo local seguido de vírgula quando não há data.

## Impact

- `template-velocity-documento-articulado.xml` (ramo sem data do fecho).
- Testes `DocumentoArticuladoTemplateProcessorTest` e `DocumentoArticuladoPdfGeneratorTest` (casos sem data; a fixture `documento-com-justificacao-longa`, com `data=""`, passa a mostrar "Sala das Sessões,") e `CLAUDE.md`.
- Sem mudança de classes Java, fixtures, API pública ou dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
