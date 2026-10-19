# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- `FechoDocumentoArticulado.getData()` devolve a data por extenso, ou vazio quando ela falta, está vazia ou é inválida (fora de `aaaa-mm-dd`).
- O template (`template-velocity-documento-articulado.xml`) monta o bloco `role="fecho"` com `#if($dataPorExtenso != "")$local, ${dataPorExtenso}.#{else}$local, <espaços> de <espaços> de <espaços>.#{end}`: o ramo sem data é o da emenda.
- A fixture `documento-com-justificacao-longa` tem `data=""` e `LocalDataFecho` "Sala das Sessões," (texto do editor).

## Goals / Non-Goals

**Goals:**
- Fecho sem data como "`<local>`,", como pede a issue #81 e como a especificação 03 do lexml-eta descreve.

**Non-Goals:**
- Mudar o fecho da emenda.
- Mudar a leitura da data ou o fecho com data válida.

## Decisions

### D1. Só o ramo sem data do template muda

O ramo `#{else}` passa a ser `$local,`; o restante do bloco (atributos, `keep-with-next`, condição de impressão só com `local`) continua igual. Data inválida segue tratada como ausente (decisão do explore), sem mudança no `FechoDocumentoArticulado`.

### D2. Cobertura

Sem fixture nova: a `documento-com-justificacao-longa` (data vazia) já exercita o caso no PDF; as variações de data vazia e ausente continuam no `DocumentoArticuladoTemplateProcessorTest`, mais uma de data inválida.

## Risks / Trade-offs

- [Fecho sem data diferente do da emenda] -> Pedido explícito da #81, em linha com a especificação do editor.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
