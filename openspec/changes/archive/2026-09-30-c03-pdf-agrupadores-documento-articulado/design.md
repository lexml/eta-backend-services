# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (parte 2, change `2026-09-30-c02-pdf-articulacao-basica-documento-articulado`):

- Na XSLT `documentoarticulado/documento-articulado-conteudo.xsl`, a articulação é um `fo:block` com `text-indent="2.5cm"`, justificado e com a entrelinha dos parâmetros. Os dispositivos são percorridos no modo `dispositivo`.
- O template `lx:Parte | lx:Livro | lx:Titulo | lx:Capitulo | lx:Secao | lx:Subsecao | lx:Agrupamento` (modo `dispositivo`) só atravessa: aplica os templates aos filhos, exceto `Rotulo` e `NomeAgrupador`.

Agrupadores nas fixtures: só a MPV (`documento-com-capitulo-e-secao`) tem agrupadores, com 8 `Capitulo` e 3 `Secao`. Os rótulos e nomes de capítulo já vêm em maiúsculas ("CAPÍTULO I", "DISPOSIÇÕES PRELIMINARES"). A seção tem rótulo "Seção I" e nome com formatação inline (`<NomeAgrupador><b>Dos beneficiários</b></NomeAgrupador>`). Não há Parte, Livro, Título nem Subseção nas fixtures.

Referências de formatação consideradas na exploração:

- **Emenda:** `citacao2html` troca `class="agrupador"` por `align="center" text-indent="0"`, e seção e subseção por `font-weight="bold"`. Rótulo e nome ficam em `p` separados e sem margens.
- **Imagem `03-exemplo-preambulo-e-articulacao.png` da #72:** confirma rótulo e nome em linhas separadas, centralizados e sem espaço extra.
- **PDF de referência da MPV (anexo da #72):** tem espaço entre as linhas e capítulo sem negrito. A #72 avisa que esses PDFs diferem da especificação, e **foi decidido seguir o texto da #72 e a emenda, sem espaço extra**.

## Goals / Non-Goals

**Goals:**
- Imprimir rótulo e nome de Parte, Livro, Título, Capítulo, Seção e Subseção com a formatação da #72.
- Manter o título do agrupador na mesma página do conteúdo seguinte.

**Non-Goals:**
- `Agrupamento` genérico do lexml-eta (fora da lista da #72): continua apenas atravessado.
- Alteração de norma e omissis (parte 4), links (parte 5), pena e título de dispositivo (parte 6).
- Qualquer mudança em Java, no template Velocity ou nos parâmetros.

## Decisions

### D1. Dois grupos de agrupadores e um template nomeado

```
lx:Parte | lx:Livro | lx:Titulo | lx:Capitulo  (modo dispositivo)
    -> titulo-agrupador(maiusculas = sim, nome em negrito = não)
    -> apply-templates nos filhos, exceto Rotulo e NomeAgrupador
lx:Secao | lx:Subsecao                          (modo dispositivo)
    -> titulo-agrupador(maiusculas = não, nome em negrito = sim)
    -> apply-templates nos filhos, exceto Rotulo e NomeAgrupador
lx:Agrupamento                                  (modo dispositivo)
    -> só atravessa (como hoje)
```

O template nomeado `titulo-agrupador` gera, dentro do bloco da articulação:

```xml
<fo:block text-align="center" text-indent="0" keep-together.within-page="always" keep-with-next.within-page="always">
  <fo:block font-weight="bold">CAPÍTULO III</fo:block>                  <!-- rótulo -->
  <fo:block>DOS PARTICIPANTES DO DESENROLA ADIMPLENTES</fo:block>      <!-- nome (negrito na seção) -->
</fo:block>
```

- **`text-indent="0"`:** sem isso, o bloco herdaria o recuo de 2,5cm da articulação e ficaria fora do centro, como a emenda já corrige com `text-indent="0"`.
- **Sem `space-before` nem `space-after`:** a separação é só a entrelinha, como entre dispositivos (decisão da exploração).
- **`keep-together.within-page="always"`:** rótulo e nome ficam na mesma página. *Correção feita na verificação integrada:* só com o `keep-with-next`, o FOP quebrou a página entre o rótulo "CAPÍTULO V" e o nome, na MPV de teste, porque o `keep-with-next` só vale entre o título e o bloco seguinte, não entre as linhas internas do título.
- **`keep-with-next.within-page="always"`:** o título não fica sozinho no fim da página. Como os títulos são irmãos do primeiro dispositivo dentro do bloco da articulação, um capítulo seguido de seção fica junto com a seção, e a seção fica junto com o primeiro artigo.
- **Rótulo e nome:** `normalize-space()` de `lx:Rotulo` e de `lx:NomeAgrupador`. Somente o texto, o que descarta a formatação inline interna do nome. Rótulo ou nome vazio não gera bloco. Sem nenhum dos dois, não é gerado o bloco externo.

*Alternativa descartada:* um template por tipo de agrupador. Os seis teriam o mesmo corpo, com diferença só nos dois parâmetros.

### D2. Maiúsculas na XSLT com `translate()`

XSLT 1.0 não tem `upper-case()`. O template nomeado `maiusculas` usa `translate()` com um par de variáveis que cobre o alfabeto e as letras acentuadas do português (`áàâãäéèêëíìîïóòôõöúùûüçñ` → `ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ`). Os indicadores ordinais (º, ª), números e pontuação não são alterados.

*Alternativa descartada:* `text-transform="uppercase"` no FO. O resultado ficaria dependente do FOP e não seria verificável no teste XML → FO. Com `translate()`, o fragmento FO já tem o texto em maiúsculas.

### D3. Formatação copiada e origem

| Elemento | FO | Origem |
|---|---|---|
| Bloco do título | `text-align="center" text-indent="0"` | `citacao2html` (`class="agrupador"`) |
| Rótulo | `font-weight="bold"` | `citacao2html` (`Rotulo` → `strong`) e texto da #72 |
| Nome de Seção e Subseção | `font-weight="bold"` | `citacao2html` (`secao`/`subsecao`) e texto da #72 |
| Maiúsculas em Parte, Livro, Título e Capítulo | `translate()` | texto da #72 |
| Título inteiro na mesma página | `keep-together.within-page="always"` | correção da verificação integrada (ver D1) |
| `keep-with-next` | `keep-with-next.within-page="always"` | decisão da exploração (a emenda não precisa, porque a citação é curta) |

### D4. Ajuste dos testes da parte 2

- `DocumentoArticuladoConteudoTransformerTest.artigosDentroDeCapitulosESecoesSemRotuloENomeDoAgrupador` passa a verificar os títulos impressos antes dos artigos. Os testes que usam o primeiro bloco da articulação da MPV como primeiro artigo passam a considerar o título do capítulo.
- `DocumentoArticuladoPdfGeneratorTest.imprimeParteInicialEArticulacaoNaOrdem`: na linha da MPV, o elemento "ainda não impresso" deixa de ser "CAPÍTULO I". Passa a ser um dispositivo do bloco de alteração da MPV (parte 4), escolhido na implementação a partir da fixture.

## Risks / Trade-offs

- [A tabela do `translate()` não cobre algum caractere acentuado raro] → ela cobre o alfabeto português; um caractere fora dela sai como está, sem erro. O risco é baixo porque os rótulos e nomes costumam vir já em maiúsculas.
- [Vários títulos seguidos com `keep-with-next` podem empurrar um bloco maior para a página seguinte] → comportamento desejado, porque evita título órfão. O FOP resolve o encadeamento.
- [O PDF de referência da #72 tem espaço entre agrupadores] → decidido seguir o texto da #72 e a emenda. Se o autor da issue quiser o espaço, será um ajuste de valor (v3.1).

## Migration Plan

Só a XSLT muda. PDFs gerados a partir desta versão passam a ter os títulos dos agrupadores. Rollback: reverter a change.
