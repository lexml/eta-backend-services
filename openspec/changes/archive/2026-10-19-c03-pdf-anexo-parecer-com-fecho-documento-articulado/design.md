# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual (change `2026-10-14-c02`, issue #80):
- `FechoDocumentoArticulado.anexoParecer(documento)` lê o atributo (só o booleano `true` conta).
- `FechoDocumentoArticulado.de(documento)` devolve fecho vazio (sem local, data e assinaturas) em anexo de parecer; o template não imprime o bloco de fecho e assinaturas quando está vazio.
- `DocumentoArticuladoPdfGenerator` passa `!anexoParecer` como `imprimirJustificacao` à XSLT, que então não gera a justificação.
- O editor (lexml-eta), no modo anexo de parecer, hoje não grava local, data nem autoria (`getDocumentoArticulado`); a fixture `documento-anexo-parecer` reproduz isso.

## Goals / Non-Goals

**Goals:**
- Em anexo de parecer, omitir só a justificação.

**Non-Goals:**
- Mudar a emenda (decisão do usuário: só o documento articulado).
- Mudar o editor para gravar local, data e autoria no modo anexo de parecer (fora deste repositório).

## Decisions

### D1. Fecho independente do atributo

`FechoDocumentoArticulado.de` deixa de testar `anexoParecer`: o fecho sai com os dados que o documento tiver, como em qualquer documento. `anexoParecer(documento)` continua existindo, usado só pelo gerador para a justificação. XSLT e template não mudam: sem justificação, o fecho vem logo depois da articulação (com o espaço após a articulação e os 34pt antes do fecho).

### D2. Fixture com o caso novo

`documento-anexo-parecer` ganha `local="Sala das Comissões"`, uma `data` e a autoria de um senador (e o `ParteFinal` textual coerente), regenerada JSON-first: o PDF mostra a articulação seguida do fecho e da assinatura, sem a justificação. O caso sem local nem autoria (como o editor grava hoje) continua coberto por variação em memória nos testes.

## Risks / Trade-offs

- [Editor ainda não grava local, data e autoria no modo anexo de parecer] -> Na prática o PDF de um anexo de parecer real continua sem fecho até o editor mudar; o back-end fica pronto.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
