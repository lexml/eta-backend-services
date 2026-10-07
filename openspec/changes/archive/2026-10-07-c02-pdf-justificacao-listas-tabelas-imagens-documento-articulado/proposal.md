# Proposal

## Why

A parte A da issue [#75](https://github.com/lexml/eta-backend-services/issues/75) (change `2026-10-07-c01`) passou a imprimir a justificação, mas deixou de fora as listas, as tabelas e as imagens, que o editor do lexml-eta também oferece. Esta é a **parte B**, que completa a #75.

No explore desta parte apareceu ainda um ajuste na parte A: o lexml-eta, ao salvar o HTML da justificação (`ajustaHtmlFromEditor`), renomeia as classes de alinhamento do Quill (`ql-align-center` -> `align-center`, idem `right` e `justify`; `ql-indent-N` -> `indent-N`). A parte A só reconhece `ql-align-*`, então os alinhamentos do documento real seriam ignorados.

## What Changes

- **Listas** (`ol`, `ul`) da justificação impressas como na emenda: recuo de 2,5cm, rótulo "1." alinhado à direita (`ol`) ou "•" (`ul`), espaço após a lista dos parâmetros de impressão. Lista dentro de item (`li` com `ol`/`ul`) sai aninhada; o recuo plano do editor (`li class="indent-N"`) é ignorado, como na emenda.
- **Tabelas** (`table`, `tr`, `td`, `th`) impressas como na emenda: centralizadas, na largura da tabela (atributo `width` em porcentagem, senão 100%), com borda externa, colunas de largura igual (o LexML não guarda a largura das células), células em fonte menor, `th` em negrito, mesclagem de células (`colspan`/`rowspan`).
- **Imagens** (`img` com `src` em data URI) impressas em bloco próprio, na largura do atributo `width` (porcentagem da largura do texto) ou na largura total, mantendo a proporção; parágrafo que começa com imagem sai centralizado. Endereços que não são data URI de imagem não são carregados.
- **Alinhamento (correção da parte A)**: os parágrafos aceitam também as classes `align-center`, `align-right` e `align-justify`, gravadas pelo editor.
- Notas de rodapé, formatação inline e remissões continuam valendo dentro de itens de lista e de células.
- **Exemplos**: o `documento-articulado-exemplo` e o `documento-com-justificacao` passam a cobrir todas as variações de lista, tabela e imagem, e as classes como o editor as grava.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: listas, tabelas e imagens deixam de constar entre os elementos não impressos ("Conteúdo impresso"); classes `align-*` em "Estilos de parágrafo da justificação"; novos requisitos para listas, tabelas e imagens da justificação.

## Impact

- `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`: templates de lista, tabela e imagem; classes `align-*`.
- Testes (`DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoPdfGeneratorTest`) e fixtures `documento-articulado-exemplo` e `documento-com-justificacao` em `src/test/resources/documentoarticulado/`.
- `CLAUDE.md` (seção `etaservices.documentoarticulado`).
- Sem mudança de API, de dependências, do template Velocity nem do Java; sem impacto em emenda, parecer ou `lexmljsonix`.
