# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (partes 2, 3 e 6):

- Na XSLT `documentoarticulado/documento-articulado-conteudo.xsl`, a articulação é percorrida no modo `dispositivo`.
  - O template nomeado `bloco-dispositivo(rotulo, rotuloNegrito)` imprime o título de dispositivo, o bloco "rótulo + primeiro `p`", os `p` seguintes e os filhos.
  - O `Artigo` repassa o próprio rótulo ao `Caput`.
- `lx:Alteracao` e `lx:Omissis` têm templates vazios.

Estrutura observada nos três documentos de teste da #72:

```
Caput (do artigo que altera)
|-- p "A Lei nº 9.394 ... passa a vigorar acrescida do seguinte art. 12-A:"
'-- Alteracao xml:base="urn:lex:..."
    '-- Artigo abreAspas="s"
        |-- Rotulo "Art. 327."
        |-- Caput textoOmitido="s"                       (sem p)
        |-- Omissis                                      (linha pontilhada)
        '-- Paragrafo fechaAspas="s" notaAlteracao="NR"
            |-- Rotulo "§ 3º"
            '-- p "A pena será aumentada da metade ..."
```

| Atributo ou elemento | Onde aparece nos testes |
|---|---|
| `abreAspas="s"` | no `Artigo` do bloco, nos três documentos |
| `fechaAspas="s"` + `notaAlteracao="NR"` | num `Paragrafo` (dois documentos) ou num `Omissis` (MPV) |
| `textoOmitido="s"` | num `Caput` sem `p` (pena), num `Caput` com incisos e num `Inciso` com omissis e alíneas (MPV) |
| `Omissis` | filho de `Artigo` (pena) ou de `Inciso` (MPV), e último elemento do bloco (MPV) |

Como a emenda formata, observado no código:

- `citacao2html` troca `<Alteracao>` por `<div margin-left="3cm" text-indent="1.5cm">`. O `html2foTextoLivre` (`estilo-norma-alterada`) usa os mesmos valores.
- `xhtml2fo.xsl` desenha o omissis (`span class="omissis"`) como `<fo:leader leader-pattern="dots" leader-length.optimum="100%"/>`.
- Na emenda, as aspas e o "(NR)" vêm no texto montado pelo front. Aqui vêm nos atributos LexML.

## Goals / Non-Goals

**Goals:**
- Imprimir o bloco de alteração recuado, com os dispositivos de dentro pelos templates existentes.
- Imprimir aspas e nota de alteração exatamente conforme os atributos, com o omissis e o texto omitido como linha pontilhada.

**Non-Goals:**
- Links (parte 5), inclusive para a norma alterada (`xml:base`).
- Justificação, local e data e assinaturas.
- Qualquer mudança em Java, no template Velocity ou nos parâmetros.

## Decisions

### D1. Bloco de alteração

`lx:Alteracao` (modo `dispositivo`) gera `<fo:block margin-left="3cm" text-indent="1.5cm">` e aplica o modo `dispositivo` aos filhos. Os blocos internos herdam o recuo inicial (`start-indent`) e o recuo de primeira linha, e o restante (justificação, entrelinha) vem do bloco da articulação. Sem `space-before` nem `space-after`. Os agrupadores dentro do bloco continuam centralizados, agora dentro da área recuada.

### D2. Aspas e nota de alteração pelos atributos

- **`bloco-dispositivo`** ganha o parâmetro `abreAspas`, com padrão `@abreAspas = 's'` do próprio elemento, e passa a olhar `@fechaAspas` e `@notaAlteracao` do próprio elemento.
- **`lx:Artigo`** repassa ao `Caput` o próprio `@abreAspas` (junto do rótulo), porque o rótulo do artigo sai no bloco do caput.
- **Abertura:** “ imediatamente antes do rótulo, dentro do mesmo bloco e fora do `fo:inline` do rótulo, portanto sem negrito, como na emenda.
- **Fechamento:** template nomeado `fecha-aspas`, que imprime ” seguido, quando houver `notaAlteracao`, de um espaço e da nota entre parênteses: `” (NR)`. É impresso no **último bloco de texto** do dispositivo: no bloco do rótulo quando há até um `p`, senão no bloco do último `p`. Nada é impresso depois.
- **Nenhuma aspa sem atributo:** as aspas que a emenda põe em cada linha da citação não existem aqui.
- **Aspas coladas ao texto** (acrescentado na implementação): o texto dos `p` costuma terminar com espaço ("…pública. "), e o fechamento sairia "pública. ”". O template de `text()` do modo `inline` apara os espaços do fim (template recursivo `aparar-fim`, porque XSLT 1.0 não tem regex), mas só no **último texto do último `p`** de um dispositivo com `fechaAspas`. Os demais textos continuam como estão.

*Alternativa descartada:* procurar aspas no texto. Os dados trazem atributos, e o texto não tem as aspas.

### D3. Omissis e texto omitido

- **`lx:Omissis`** gera `fo:block` com, em ordem: “ se tiver `abreAspas`, depois `<fo:leader leader-pattern="dots" leader-length.optimum="100%"/>` (valor da emenda), depois o `fecha-aspas` se tiver `fechaAspas`. O `leader-length.minimum` padrão (0) permite que a linha pontilhada encolha para caber o "” (NR)" no fim da mesma linha.
- **`textoOmitido="s"`** no `bloco-dispositivo`: depois do rótulo e do espaço, entra o mesmo `fo:leader` no lugar do texto do primeiro `p`. Os filhos (incisos, omissis, alíneas) continuam sendo impressos depois, na ordem do documento.

### D4. Ajuste dos testes das partes anteriores

- `DocumentoArticuladoConteudoTransformerTest.alteracaoDeNormaOmitida`: passa a verificar a impressão do bloco.
- `DocumentoArticuladoPdfGeneratorTest.imprimeParteInicialEArticulacaoNaOrdem`: a coluna "ainda não impresso" (trechos das alterações) passa a ser um **trecho da alteração que deve aparecer** depois do primeiro artigo. A verificação do que continua sem impressão (a justificação) vai para o documento de exemplo da #71, que tem justificação ("Esta proposição promove a").

### D5. Formatação e origem

| Elemento | FO | Origem |
|---|---|---|
| Bloco de alteração | `margin-left="3cm" text-indent="1.5cm"` | `citacao2html` (`<Alteracao>`) e `html2foTextoLivre` (`estilo-norma-alterada`) |
| Omissis e texto omitido | `fo:leader leader-pattern="dots" leader-length.optimum="100%"` | `xhtml2fo.xsl` (`span class="omissis"`) |
| Aspas | “ e ”, fora do rótulo em negrito | emenda (aspas fora do `strong`) e #72 |
| Nota de alteração | `” (NR)`, com espaço | decisão da exploração (convenção de publicação das leis e PDF de referência) |

## Risks / Trade-offs

- [`fechaAspas` num `Artigo` com parágrafos] → nos testes, o fechamento está sempre no último dispositivo, não no artigo. O repasse do `Artigo` ao `Caput` cobre só o `abreAspas`. Um `fechaAspas` no próprio `Artigo` fecha no bloco do caput, o que só é correto quando o artigo termina no caput. O risco foi registrado; o lexml-eta marca o último dispositivo.
- [A linha pontilhada numa linha justificada pode variar de comprimento] → o `fo:leader` ocupa o espaço restante da linha, como na emenda. Conferir na inspeção da v5.
- [Blocos de alteração longos atravessando páginas] → comportamento normal de fluxo; sem `keep` especial.
- [Nota de alteração diferente de "NR"] → é impressa com o valor do atributo, entre parênteses.

## Migration Plan

Só a XSLT muda. PDFs gerados a partir desta versão passam a ter os blocos de alteração. Rollback: reverter a change.
