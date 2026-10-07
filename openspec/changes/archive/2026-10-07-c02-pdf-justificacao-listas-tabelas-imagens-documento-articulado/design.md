# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual (parte A, change `2026-10-07-c01`): a justificação é impressa pela `documento-articulado-conteudo.xsl` no modo `justificacao` (um bloco por `p`); `ol`, `ul` e `table` caem na regra padrão (não impressos) e `img`, inline no `p`, não gera saída. O corpo da justificação tem `text-indent="2.5cm"` e `line-height` dos parâmetros.

Entrada (esquema `lexml-simples.xsd` do `jsonix-lexml`):
- `ol`/`ul` com `li` (conteúdo inline, `p` ou outra `ol`/`ul`);
- `table` (`id` obrigatório, `width` e `border` inteiros), `tr`, `td`/`th` (inline; `colspan`/`rowspan`; **sem** `width`);
- `img` inline (`src` obrigatório, `width`/`height` inteiros).

Editor (lexml-eta, Quill 1.3.7 + `quill1-table`): listas planas, com aninhamento por `li class="ql-indent-N"`; largura de tabela e de imagem em porcentagem; ao salvar, `ajustaHtmlFromEditor` troca `ql-indent` por `indent` e `ql-align-*` por `align-*`.

Referência de formatação (emenda): `xhtml2fo.xsl` (templates `ol`, `ol/li`, `ul`, `ul/li`, `table`, `table-width`, `table-common-atts`, `column-width`, `td`, `th`, `img`), `HTML2FOConverter.trataTabelas`, `VelocityExtension.html2foTextoLivre` (`margin-bottom: $pMarginBottom` em `p|ol|ul`; parágrafo que começa com imagem centralizado) e `VelocityExtension.trataImagens`.

Verificação feita no explore: o FOP 2.7 renderiza `fo:external-graphic` com `src` em data URI PNG (sem arquivo temporário).

## Goals / Non-Goals

**Goals:**
- Listas, tabelas e imagens com a aparência da emenda, inteiramente na XSLT.
- Aceitar as classes como o editor as grava (`align-*`).
- Exemplos que cubram todas as variações.

**Non-Goals:**
- Reproduzir o aninhamento plano do editor (`indent-N`): ignorado, como na emenda (ajuste fino na atividade de formatação).
- Larguras de colunas: o LexML não as guarda; colunas iguais.
- Converter formatos de imagem em Java (como a emenda faz).
- Conversão JSON <-> XML do `jsonix-lexml`.

## Decisions

### D1. Alinhamento com e sem `ql-`

O template `estilo-paragrafo` passa a testar cada alinhamento pelos dois tokens (`' align-center '` ou `' ql-align-center '`). Mesma regra da emenda (`trataAlinhamentoDePragrafo` casa `align-(...)` com ou sem prefixo).

### D2. Listas (valores da emenda)

```xml
<fo:list-block margin-left="2.5cm" text-indent="0" margin-bottom="{$pMarginBottom}"
    provisional-label-separation="1em" provisional-distance-between-starts="...">
  <fo:list-item>
    <fo:list-item-label end-indent="label-end()"><fo:block text-align="end">N.</fo:block></fo:list-item-label>
    <fo:list-item-body start-indent="body-start()"><fo:block>conteúdo inline</fo:block> [listas internas]</fo:list-item-body>
  </fo:list-item>
</fo:list-block>
```

| Valor | `ol` | `ul` | Origem |
|---|---|---|---|
| `provisional-distance-between-starts` | `(algarismos do nº de itens) * 0.9 + 0.6` em | `1em` | `xhtml2fo.xsl` |
| rótulo | `xsl:number` dos `li` irmãos + "." | `•` | idem |
| `margin-left`, `text-indent` | 2.5cm, 0 | 2.5cm, 0 | idem |
| `margin-bottom` | `$pMarginBottom` | `$pMarginBottom` | `html2foTextoLivre` |

- Conteúdo do `li`: o texto e os elementos inline num bloco; `p` dentro do `li` em blocos próprios; `ol`/`ul` dentro do `li` pelo mesmo template (lista aninhada, que soma mais 2,5cm a partir do corpo do item). A lista interna também tem o `margin-bottom`, como na emenda, o que deixa um pequeno espaço depois dela, dentro do item.
- `indent-N` não é lido.
- Notas de rodapé dentro do item: o modo `inline` já gera `fo:footnote`; dentro de `fo:list-item-body` o FOP aceita.

### D3. Tabelas (valores da emenda)

```xml
<!-- tabela externa, só para centralizar -->
<fo:table table-layout="fixed" width="100%">
  <fo:table-column column-width="proportional-column-width(1)"/>
  <fo:table-column column-width="{N}%"/>        <!-- width da tabela, ou 100% -->
  <fo:table-column column-width="proportional-column-width(1)"/>
  <fo:table-body><fo:table-row><fo:table-cell column-number="2">
    <fo:block text-indent="0">
      <fo:table width="100%" table-layout="fixed" border-collapse="collapse" border="1pt solid">  <!-- border-style="none" se border=0 -->
        <fo:table-column column-width="proportional-column-width(1)"/>  x colunas
        <fo:table-body>
          <fo:table-row>
            <fo:table-cell padding=".2em" [border="1pt solid" se border informado e != 0]
                           [number-columns-spanned] [number-rows-spanned]>
              <fo:block font-size="80%" line-height="140%">conteúdo inline</fo:block>
            </fo:table-cell>
            <!-- th: font-weight="bold" display-align="center" -->
```

- **Número de colunas**: o maior, entre as linhas, de `count(td|th sem colspan) + sum(@colspan)`. Na emenda o `trataTabelas` (Java) completa células faltantes e calcula larguras a partir de `td width`; no LexML não há `width` nas células e o FOP aceita linhas com menos células, então basta declarar as colunas iguais.
- **Largura da tabela**: `@width` inteiro do LexML interpretado como porcentagem (unidade do editor).
- **Bordas como o editor grava** (verificado na validação visual do grupo 5): o plugin de tabelas do lexml-eta sempre grava `border="1"` (`quill1-table/js/TableBlot.js`), então as tabelas reais saem com grade completa. A regra da emenda para `border` ausente (só a moldura) e `border="0"` (sem bordas) continua implementada e coberta por testes unitários; as fixtures passaram a usar `border="1"` em todas as tabelas, como o editor.
- O `id` da tabela (`_tabela<n>`, exigido pelo LexML) não é emitido (não é alvo de remissão).
- Notas de rodapé em células: `fo:footnote` dentro de `fo:table-cell` é aceito pelo FOP 2.7; coberto por teste com o PDF real (task 1.1 confirma).

### D3a. Sobrescrito sem mudar a altura da linha

Decidido na validação visual do grupo 5 (diferença em relação à emenda): o corpo da justificação tem `line-height-shift-adjustment="disregard-shifts"`. Sem isso, o número sobrescrito da nota de rodapé (e `sup`/`sub`) aumenta a altura da linha: a linha fica cerca de 3pt mais baixa que as vizinhas e, numa célula, o texto desalinha da célula ao lado. A propriedade é herdada por parágrafos, listas e células.

### D4. Imagens (valores da emenda, sem arquivo temporário)

```xml
<fo:block text-indent="0">   <!-- o img é inline no p, mas sai em bloco próprio, como na emenda -->
  <fo:external-graphic src="url('{@src}')" content-width="scale-to-fit" scaling="uniform" width="{@width}%"/>  <!-- ou 100% -->
</fo:block>
```

- Só `src` iniciado por `data:image/` é emitido. Outros endereços (http, arquivo) são ignorados: o FOP faria acesso à rede ou ao disco durante a geração.
- **Data URI direto**, sem a gravação em arquivo temporário e a conversão para JPEG da emenda (que deixava arquivos no disco e precisaria de Java no fluxo isolado). Formatos que o FOP não ler: o FOP registra o erro e segue sem a imagem (task 1.1 confirma, incluindo o efeito no PDF/A).
- **Resultado da verificação (task 1.1)**, com o template e o `DocumentoArticuladoFopProcessor` reais (PDF/A-3b, fontes próprias), imagens geradas com ImageIO em data URI e largura de 40%:

  | Caso | Resultado |
  |---|---|
  | PNG com transparência | renderiza (imagem + máscara de transparência, 2 XObjects) |
  | PNG opaco, JPEG, GIF, BMP | renderizam (1 XObject) |
  | ICO (`image/x-icon`) | não lido: o FOP registra `Image not available` e segue; PDF gerado sem a imagem |
  | data URI corrompido | idem |
  | `fo:footnote` em `fo:table-cell` e em `fo:list-item-body` | renderiza no rodapé, com a numeração |

  Nenhum caso impediu a geração; o comportamento do FOP já atende ao requisito "imagem que não puder ser lida não impede o PDF", sem tratamento adicional. A conformidade PDF/A-3B das imagens (sobretudo a transparência, permitida no PDF/A-3) é confirmada no veraPDF na task 5.2; os PDFs da verificação ficaram em `pdf-documento-articulado/verificacoes/parte-b-imagens/`.
- **Parágrafo que começa com imagem** (primeiro nó com conteúdo é `img`): `text-align="center"` e `text-indent="0"`. Na emenda a regra é a regex `<p(.+?)><img` do `html2foTextoLivre`; aqui é aplicada pela intenção (centralizar o parágrafo da imagem), sem depender de o `p` ter atributos.
- **Recuo do bloco da imagem** (decidido na aplicação do grupo 3): o bloco tem `text-indent="0"`. Na emenda ele herda o recuo de primeira linha de 2,5cm do parágrafo, o que faria a imagem em largura total passar da margem direita.
- **Defeito do XSLT do JDK** (encontrado na aplicação do grupo 3): o predicado `[self::* or normalize-space(.)]` dá falso para um elemento sem texto (ex.: `img`), embora `self::*` sozinho funcione. Os testes "primeiro conteúdo do parágrafo" e "trecho do item tem conteúdo" usam `[not(self::text()) or normalize-space(.)]`, com comentário na XSLT.
- O parágrafo só com imagem não é "vazio": o teste de linha em branco da parte A passa a considerar também `img` com data URI.

### D5. Fixtures

Ampliar, seguindo a especificação do lexml-eta e a regra de que o exemplo cobre tudo:

- `documento-com-justificacao` (catálogo): lista numerada com lista aninhada, lista com marcadores, item com `indent-1`, item com formatação, remissão e nota de rodapé, item com `p`; tabela sem `width` e tabela com `width="60"`, `border="1"`, linha de `th`, `colspan` e `rowspan`, célula com formatação e nota; imagem PNG com `width`, sem `width`, sozinha no parágrafo e depois de texto, imagem JPEG, imagem com `src` externo; parágrafos com `align-center`/`align-right`/`align-justify` (como o editor grava).
- `documento-articulado-exemplo`: as mesmas variações, no contexto do Programa de Modernização (mantendo o que já existe).

Mesmo procedimento JSON-first da parte A (fonte com `␣` -> `tojson` -> espaço -> `toxml`).

### D6. Testes

- `DocumentoArticuladoConteudoTransformerTest`: atributos e rótulos das listas (numeração, aninhamento, `indent-N` ignorado), estrutura das tabelas (tabela externa, largura, colunas, bordas, `th`, spans), imagens (largura, centralização, `src` externo ignorado), `align-*`, nota em item e em célula.
- `DocumentoArticuladoPdfGeneratorTest`: texto dos itens e células no PDF, rótulos "1." e "•", imagem presente na página (XObject) com a largura esperada, nota de célula no rodapé, PDF gerado com imagem externa.

## Risks / Trade-offs

- [Imagem em formato que o FOP não lê, ou PNG com transparência no PDF/A-3B] -> Task 1.1 verifica antes da implementação; se um formato quebrar o PDF/A, a decisão volta para o usuário (ex.: omitir esse formato).
- [Nota de rodapé em célula] -> Suportada pelo FOP com restrições de quebra de página; teste com PDF real.
- [Recuo acumulado de listas aninhadas (2,5cm por nível)] -> É o comportamento da emenda; ajuste fino na atividade de formatação.
- [Larguras de coluna perdidas] -> Limitação do LexML; colunas iguais.

## Migration Plan

Não se aplica: biblioteca sem estado; o novo comportamento vale para os próximos PDFs.
