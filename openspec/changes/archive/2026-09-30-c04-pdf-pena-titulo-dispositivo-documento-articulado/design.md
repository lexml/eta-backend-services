# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (partes 2 e 3):

- Na XSLT `documentoarticulado/documento-articulado-conteudo.xsl`, a articulação é percorrida no modo `dispositivo`.
  - O template nomeado `bloco-dispositivo(rotulo)` gera o bloco com o rótulo em negrito, um espaço e o primeiro `p` (modo `inline`), os `p` seguintes e os filhos exceto `Rotulo` e `p`.
  - `lx:Artigo` passa o próprio rótulo ao `Caput` e processa os demais filhos, **excluindo `TituloDispositivo`**.
- `lx:Pena` e `lx:TituloDispositivo` têm templates vazios no modo `dispositivo`.

No documento de teste `documento-com-pena-e-titulo-de-dispositivo`:

- **3 `TituloDispositivo`**, sempre como **primeiro filho do `Artigo`**, antes do `Rotulo`, com texto direto (ex.: "Desvio ou apropriação de recursos e insumos da saúde ").
- **5 `Pena`**, todas no **fim** de um `Caput` ou `Paragrafo`, com `Rotulo` ("Pena –") e um `p`, a mesma estrutura de um inciso.

A imagem `05-exemplo-titulo-dispositivo-e-pena.png` da #72 mostra o título com o mesmo recuo do "Art. 2º" (não centralizado), em negrito e sem rótulo, e "Pena –" com o recuo dos dispositivos e sem negrito.

## Goals / Non-Goals

**Goals:**
- Imprimir a pena e o título de dispositivo conforme a #72, reaproveitando o `bloco-dispositivo`.
- Manter o título de dispositivo na mesma página do dispositivo seguinte.

**Non-Goals:**
- Alteração de norma e omissis (parte 4), links (parte 5).
- Qualquer mudança em Java, no template Velocity ou nos parâmetros.

## Decisions

### D1. Pena pelo `bloco-dispositivo` com rótulo regular

O template nomeado ganha o parâmetro `rotuloNegrito`, com valor padrão `true()`:

```
bloco-dispositivo(rotulo, rotuloNegrito = true())
  rótulo em fo:inline font-weight="bold" só quando rotuloNegrito; senão, fo:inline sem negrito
```

`lx:Pena` (modo `dispositivo`) chama `bloco-dispositivo` com `rotulo = lx:Rotulo` e `rotuloNegrito = false()`. Como a pena já está na posição certa entre os filhos do caput ou do parágrafo, a ordem sai correta sem tratamento extra.

*Alternativa descartada:* um template próprio para a pena. Duplicaria o corpo do `bloco-dispositivo`, que é exatamente o mesmo exceto pelo negrito.

### D2. Título de dispositivo antes do dispositivo, de forma genérica

- **Novo template `lx:TituloDispositivo` (modo `dispositivo`):** gera `<fo:block font-weight="bold" keep-with-next.within-page="always">` com o conteúdo no modo `inline`. Não gera nada quando o texto normalizado é vazio.
- **Posição:**
  - `lx:Artigo` aplica os templates a `lx:TituloDispositivo` **antes** do `Caput` (que traz o rótulo do artigo);
  - `bloco-dispositivo` aplica os templates ao `lx:TituloDispositivo` do próprio dispositivo **antes** do bloco dele e o exclui da lista de filhos. Isso cobre título em parágrafo, inciso etc., mesmo sem ocorrência nas fixtures.
- **Formatação:** o bloco não define recuo nem alinhamento próprios. Herda do bloco da articulação o recuo de primeira linha de 2,5cm, a justificação e a entrelinha, como qualquer dispositivo (a #72 diz "mesma formatação dos dispositivos de artigo"). Não é centralizado, diferente dos agrupadores.
- **`keep-with-next`:** evita o título sozinho no fim da página, pela mesma razão do título dos agrupadores (parte 3). Como é uma linha curta, não precisa de `keep-together`.

### D3. Formatação e origem

| Elemento | FO | Origem |
|---|---|---|
| Pena | bloco de dispositivo, rótulo em `fo:inline` sem negrito | #72 ("mesma formatação dos dispositivos de artigo, sem negrito no rótulo") e imagem 05 |
| Título de dispositivo | `fo:block font-weight="bold" keep-with-next.within-page="always"`, herdando recuo, justificação e entrelinha | #72 ("mesma formatação dos dispositivos de artigo, sem rótulo e todo em negrito") e imagem 05; `keep-with-next` pela decisão da parte 3 |

### D4. Ajuste dos testes das partes 2 e 3

- `DocumentoArticuladoConteudoTransformerTest.penaETituloDeDispositivoOmitidos`: passa a verificar a impressão.
- `DocumentoArticuladoPdfGeneratorTest.imprimeParteInicialEArticulacaoNaOrdem`: na linha do documento da pena, o elemento "ainda não impresso" deixa de ser "Pena –". Passa a ser um dispositivo do bloco de alteração desse documento (parte 4), escolhido na implementação a partir da fixture.

## Risks / Trade-offs

- [Título de dispositivo com elemento inline não previsto] → o modo `inline` imprime o texto de qualquer elemento; só `b`/`i` recebem formatação, o que dentro do bloco em negrito é inofensivo.
- [Vários títulos e agrupadores seguidos com `keep-with-next`] → o FOP encadeia os blocos e leva o conjunto para a página seguinte quando necessário, que é o comportamento desejado.
- [Pena fora do fim do caput ou do parágrafo] → é impressa onde estiver, na ordem do documento, o que é o correto para qualquer posição.

## Migration Plan

Só a XSLT muda. PDFs gerados a partir desta versão passam a ter penas e títulos de dispositivo. Rollback: reverter a change.
