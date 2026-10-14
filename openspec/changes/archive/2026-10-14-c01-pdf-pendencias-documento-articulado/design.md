# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual:
- O `template-velocity-documento-articulado.xml` guarda, comentado como "NÃO RETIRAR", o trecho da emenda com "Alertas" (revisões e comentários) e "Pendências de Preenchimento", logo antes da epígrafe. O conteúdo vem pronto da XSLT em `$conteudoFo` (começa pela epígrafe), e o fecho e as assinaturas vêm depois dele (change `2026-10-13-c03`).
- O `DocumentoArticuladoTemplateProcessor` põe no contexto do Velocity só valores simples e escapados, lidos do JSON por classes package-private (`OpcoesImpressaoDocumentoArticulado`, `FechoDocumentoArticulado`).
- No JSON (jsonix), no valor do `lexedit:Metadado`: `comentarios.sequenciaComentario[*].comentario[*]`, `revisoesTextuais.revisaoTextual[*]`, `revisoesArticulacao.revisaoArticulacao[*]` e `pendencias.pendencia[*]` (strings).

Referência (emenda, `template-velocity-emenda.xml` e `Emenda`):

| Bloco | Condição na emenda | Estilo |
|---|---|---|
| Marcas de revisão | `isPossuiMarcasRevisao()`: `revisoes` não vazia | `background-color="yellow" color="red" font-size="12pt" font-weight="bold" text-align="center" padding=".1cm" space-after=".3cm"` |
| Comentários | `isPossuiComentarios()`: alguma sequência com comentário | idem |
| Pendências de preenchimento | `pendenciasPreenchimento` não vazia | idem, mas `text-align="left"`, `padding-left/right=".25cm"`; `fo:list-block` com `font-weight="normal"`, marcador `&#8226;`, `margin-left=".5cm"`, `space-before=".3em"` por item; cada item impresso como chegou |

## Goals / Non-Goals

**Goals:**
- Os três blocos da emenda, com os mesmos textos, estilo e ordem, a partir dos dados do `lexedit:Metadado`.

**Non-Goals:**
- Filtrar ou reescrever as pendências (decisão do explore: cada item como chegou, mesmo que repita uma caixa; revisto depois se necessário).
- Validar a coerência entre pendências e o restante do documento.
- Variar o tamanho das caixas com `tamanhoFonte` (12pt fixo, como na emenda).

## Decisions

### D1. `AlertasDocumentoArticulado`: leitura dos dados

Classe package-private em `pdf/`, imutável, `static de(JsonNode documento)`, que localiza o valor do `lexedit:Metadado` como o `FechoDocumentoArticulado` (pelo nome e namespace do elemento):
- `isPossuiRevisoes()`: `revisaoTextual` ou `revisaoArticulacao` com ao menos um item. A emenda tem um só tipo de revisão; aqui os dois tipos levam à mesma caixa.
- `isPossuiComentarios()`: alguma `sequenciaComentario` com ao menos um `comentario` (regra do `Emenda.isPossuiComentarios()`).
- `getPendencias()`: os textos de `pendencia`, na ordem, sem alteração; só itens não textuais ou em branco são descartados (não haveria o que imprimir).

A localização do `lexedit:Metadado` passa a ser compartilhada (método package-private reaproveitado pelo `FechoDocumentoArticulado` e pela nova classe), sem mudar o comportamento do fecho.

Alternativa considerada: ler na XSLT a partir do XML (`lexedit:Pendencias` também está no `documento-articulado.xml`). Rejeitada para seguir o padrão do fecho e do cabeçalho (dados do metadado lidos em Java, apresentação no template, como na emenda).

### D2. Template

O trecho "Alertas"/"Pendências de Preenchimento" sai do comentário "NÃO RETIRAR" e passa a ser ativo logo **antes** de `$conteudoFo`, com as adaptações: `$emenda.revisoes.size() > 0` -> `$possuiRevisoes`; `$emenda.possuiComentarios` -> `$possuiComentarios`; `$emenda.pendenciasPreenchimento` -> `$pendencias` (lista de strings escapadas para XML). Textos, atributos e ordem iguais aos da emenda. O restante do trecho comentado continua como está.

### D3. Fixture e cobertura

- `documento-articulado-exemplo`: `lexedit:Pendencias` com as cinco pendências da especificação 13 do lexml-eta, na ordem dela (justificação, remissões internas inválidas, comentários, revisão de texto, revisão na articulação), substituindo as três atuais; o exemplo já tem revisões de texto e da articulação e comentários, então mostra as três caixas. Regenerado pelo procedimento JSON-first.
- Demais fixtures sem mudança: `documento-com-justificacao` (revisões e comentários; duas pendências), `documento-com-justificacao-longa` (revisões; uma pendência), documentos da #72 (nada).
- Variações em memória nos testes: só pendências (sem revisões nem comentários), só comentários, pendência em branco, fonte 18 (caixas em 12pt).

## Risks / Trade-offs

- [Item da lista repetindo uma caixa] -> Decisão do usuário; registrado como possível ajuste futuro.
- [Caixas empurram o conteúdo da primeira página] -> Esperado (como na emenda); os testes que medem a posição da epígrafe no exemplo comparam variações que têm as mesmas caixas, e as fixtures da #72 não têm alertas.
- [Texto das pendências com caracteres especiais] -> Escapado para XML antes do Velocity.

## Migration Plan

Não se aplica: biblioteca sem estado; vale para os próximos PDFs.
