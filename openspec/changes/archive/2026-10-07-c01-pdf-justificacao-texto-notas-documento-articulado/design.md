# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver a spec delta.

Estado atual (fim da #72):

- `documento-articulado-conteudo.xsl` gera o fragmento FO a partir do `documento-articulado.xml`. O template raiz só percorre `lx:LexML/lx:ProjetoNorma/lx:Norma`; a `Justificacao`, irmã de `Norma` dentro de `ProjetoNorma`, é ignorada.
- O modo `inline` já trata texto, `b`, `i`, remissões (`span`/`Remissao` com `xlink:href`, chaves `dispositivo-impresso` e `remissao-interna`) e ignora as demais marcas, imprimindo só o texto.
- O template Velocity tem a sequência de páginas com um único `fo:flow`; o separador de notas de rodapé da emenda (`xsl-footnote-separator`) está comentado ("NÃO RETIRAR").

Formato de entrada (especificação do lexml-eta, `docs/extensao-formato-lexml/06`, `07` e `09`, conferido no `lexml-simples.xsd` do `jsonix-lexml`):

```xml
<ProjetoNorma>
  <Norma>...</Norma>
  <Justificacao>
    <PartePrincipal>
      <p class="ql-align-center">... <b>..</b> <i>..</i> <u>..</u> <sub>..</sub> <sup>..</sup>
         <a xlink:href="https://...">..</a> <span xlink:href="urn:...">..</span> <span id="_tc..">..</span>
         <del id="_rt..">..</del><ins id="_rt..">..</ins>
         texto<NotaDeRodape>Texto <b>formatado</b> da nota.</NotaDeRodape>.</p>
      <ol>/<ul>/<table>     (parte B)
    </PartePrincipal>
  </Justificacao>
</ProjetoNorma>
```

Referência de formatação: a justificação da emenda (`template-velocity-emenda.xml`, `VelocityExtension.html2foTextoLivre`, `HTML2FOConverter.trataAlinhamentoDePragrafo` e `xhtml2fo.xsl`), cujos valores são copiados, sem referenciar nenhum recurso da emenda.

## Goals / Non-Goals

**Goals:**
- Imprimir a justificação com a aparência da justificação da emenda, inteiramente na XSLT do conteúdo (mesma arquitetura da #72).
- Notas de rodapé com `fo:footnote`, numeração calculada na XSLT.
- Fixtures que sigam a especificação do lexml-eta e cubram todos os recursos da parte A.

**Non-Goals:**
- Listas, tabelas e imagens (parte B). Nesta change são omitidas.
- Local e data, assinaturas, cabeçalho, opções de impressão.
- Corrigir ou contornar limitações do `jsonix-lexml`/lexml-eta na conversão (ex.: perda de espaços entre elementos inline no sentido XML -> JSON, perda de `href`/`width`). A conversão não faz parte desta atividade.

## Decisions

### D1. Justificação na XSLT, depois da `Norma`

O template raiz passa a aplicar, depois de `lx:Norma`, os templates de `lx:ProjetoNorma/lx:Justificacao`. O título "JUSTIFICAÇÃO" é gerado pela XSLT (como os títulos dos agrupadores), não pelo Velocity, para que todo o conteúdo continue num só lugar.

- A justificação só é impressa se tiver texto fora de exclusões: `PartePrincipal//text()[not(ancestor::lx:del)][normalize-space()]`. Sem isso, nada (nem título).
- Mais de uma `Justificacao` (o esquema permite): um único título, conteúdos na ordem do documento.
- Alternativa descartada: título no template Velocity. Separaria o título do conteúdo e exigiria passar ao Velocity a informação "tem justificação".

### D2. Valores de formatação (copiados da emenda)

| Elemento | Valores | Origem na emenda |
|---|---|---|
| título | `text-align="center"`, `font-weight="bold"`, `font-size="{$maxTamanhoFonte}"`, `space-before="26pt"`, `keep-with-next.within-page="always"` | bloco "JUSTIFICAÇÃO" (`$spacing3` = 26pt sem "reduzir espaço") |
| corpo | `space-before="14pt"`, `line-height="{$lineHeight}"`, `text-indent="2.5cm"`, sem `text-align` (alinhado à esquerda, como no editor) | bloco `role="Justificativa"` (`$spacing1` = 14pt) |
| parágrafo | `fo:block margin-bottom="{$pMarginBottom}"` | `html2foTextoLivre` (`margin-bottom: $pMarginBottom` em `p`) |

Parágrafo sem texto (`<p/>`, linha em branco do editor): sai como bloco com um espaço não separável, isto é, uma linha em branco, como na emenda (`<p><br></p>` do Quill vira uma linha vazia). Decidido na aplicação do grupo 2.

Os espaços de 26pt e 14pt ficam fixos na XSLT, com comentário de origem, como os 48pt e 72pt da #72. O `white-space-collapse="false"` da emenda não é copiado: o tratamento de espaços do XSL-FO padrão já é o usado no resto do documento.

### D3. Classes do Quill, por token

Template do parágrafo da justificação lê `@class` por token (`contains(concat(' ', normalize-space(@class), ' '), ' ql-align-center ')`) e escreve os atributos:

| Classe | Atributos FO | Origem |
|---|---|---|
| `ql-align-center` / `ql-align-right` | `text-align="center"`/`"right"` + `text-indent="0"` | `trataAlinhamentoDePragrafo` |
| `ql-align-justify` | `text-align="justify"` | idem |
| `ql-align-left` | nada (já é o padrão) | idem |
| `ql-text-indent-0px` | `text-indent="0"` | `html2foTextoLivre` |
| `ql-margin-bottom-0px` | `margin-bottom="0"` | idem |
| `estilo-ementa` | `margin-left="6.5cm"`, `text-indent="0"` | `html2foTextoLivre` (o CSS do editor também justifica, mas a emenda não; vale a emenda) |
| `estilo-norma-alterada` | `margin-left="3cm"`, `text-indent="1.5cm"` | `html2foTextoLivre` |

Critério confirmado na aplicação do grupo 2: a emenda é a base das decisões de estilo; ajustes finos ficam para uma atividade própria de formatação. Exceção mantida: links em cinza e sem sublinhado (padrão do PDF definido na #72), em vez do azul sublinhado do `xhtml2fo.xsl`.

Quando há mais de uma classe, todas se aplicam; `text-indent="0"` do alinhamento prevalece sobre o recuo padrão por ser atributo do próprio bloco.

### D4. Inline: reaproveitar o modo `inline` e completar as marcas

| Elemento | Saída |
|---|---|
| `b`, `i`, `span`/`Remissao` com `xlink:href`, texto | templates existentes |
| `u` | `fo:inline text-decoration="underline"` |
| `sub` / `sup` | `fo:inline baseline-shift="sub"`/`"super"` `font-size="0.7em"` (valores do `xhtml2fo.xsl`) |
| `a` com `xlink:href` iniciado por `http://` ou `https://` | `fo:basic-link external-destination` com `color="{$corLink}"`, como as remissões |
| `a` sem endereço web | só o conteúdo |
| `del` | nada |
| `ins`, `span` sem `xlink:href` (comentários `_tc`) | só o conteúdo |
| `NotaDeRodape` | `fo:footnote` (D5) |
| `img` e demais | só o texto dos filhos (img não tem: nada) |

O modo `inline` é o mesmo da articulação, então ementa e dispositivos também ganham `u`, `sub`, `sup` e `a`; isso não muda nenhum documento existente (o editor não gera essas marcas na articulação) e mantém um único conjunto de regras inline.

`ol`, `ul` e `table` dentro de `PartePrincipal` não têm template na parte A e caem na regra padrão (não impressos), como pede a spec.

### D5. Notas de rodapé com `fo:footnote`

```xml
<fo:footnote>
  <fo:inline baseline-shift="super" font-size="0.7em">N</fo:inline>
  <fo:footnote-body>
    <fo:block font-size="{$tamanhoFonte}" text-indent="0" start-indent="0" end-indent="0" text-align="start"
              font-weight="normal" font-style="normal" text-decoration="none">
      <fo:block font-size="0.7em" line-height="1.5em">N <conteúdo inline></fo:block>
    </fo:block>
  </fo:footnote-body>
</fo:footnote>
```

Estrutura de dois blocos da emenda: o externo volta ao tamanho do texto (`$tamanhoFonte`) e o interno aplica 0.7em, para a nota não herdar um tamanho alterado. Para isso a XSLT recebe um quarto parâmetro, `tamanhoFonte` (`"14pt"`, de `ParametrosImpressaoDocumentoArticulado.getTamanhoFonte()`), passado pelo `DocumentoArticuladoConteudoTransformer` como os demais (ajuste feito na aplicação do grupo 3). A margem esquerda dos estilos de parágrafo (`margin-left`) é herdada como `start-indent`, por isso é ele que o corpo zera.

- **Número**: `xsl:number level="any" count="lx:NotaDeRodape[not(ancestor::lx:del)]" from="lx:ProjetoNorma"`. Notas dentro de `del` não casam o template (o `del` não aplica templates) e não entram na contagem. A numeração é calculada na impressão, como pede a spec 07.
- **Corpo**: `0.7em`, `line-height 1.5em` e "número + espaço + texto" vêm da emenda (`xhtml2fo.xsl`, template `nota-rodape`, e `VelocityTemplateProcessor`, que prefixa o número ao texto). No XSL-FO o `fo:footnote-body` herda as propriedades do bloco onde a nota está (recuo de 2,5cm, alinhamento, margens, e negrito/itálico se a nota estiver dentro de `b`/`i`), por isso o bloco do corpo **zera** essas propriedades explicitamente.
- **Separador**: no template Velocity, `fo:static-content flow-name="xsl-footnote-separator"` com `fo:leader leader-length="50%" rule-thickness="0.5pt" leader-pattern="rule"`, copiado da emenda e colocado antes do `fo:flow` (ordem exigida pelo XSL-FO). É a única alteração no template.
- Alternativa descartada: notas no fim da justificação (endnotes). A #75 e a emenda usam rodapé.

### D6. Fixtures segundo a especificação do lexml-eta

Dois documentos novos em `src/test/resources/documentoarticulado/`, registrados no `ConversorDocumentoArticuladoFake.comDocumentosDeTeste()`:

1. **`documento-com-justificacao`** (catálogo da parte A): articulação curta (2 artigos) e justificação com parágrafo padrão; cada formatação inline (com espaço entre elementos adjacentes); `a` com endereço web; remissão externa (URN) e interna (`art1`); trecho comentado `_tc`; cada classe de D3; `del`/`ins` com ids `_rt`; três notas de rodapé (uma com negrito, uma com remissão) e uma nota dentro de `del`; um parágrafo vazio. Metadados conforme as specs 08, 09, 12 e 13 (`lexedit:Comentarios`, `lexedit:RevisoesTextuais`, `lexedit:Usuarios`, `lexedit:Pendencias`). Local e data e assinaturas na `ParteFinal`, para o cenário "Elementos ainda não impressos".
2. **`documento-com-justificacao-longa`**: justificação realista de 3 a 4 páginas, com notas distribuídas em páginas diferentes, para a numeração contínua e as notas em várias páginas (teste com PDFBox).

Geração do par, como nas fixtures da #72: LexML-fonte escrito à mão -> `jsonix-lexml tojson` -> JSON; XML final = `jsonix-lexml toxml` do JSON. O `tojson` descarta texto só com espaços entre dois elementos inline; nesses pontos o espaço é **reinserido no JSON** (é o que o editor gera a partir do HTML do Quill), e o `toxml` o preserva. Assim o par representa a entrada real da impressão (JSON -> XML).

O `documento-articulado-exemplo` (documento básico usado nos testes manuais) tinha só a justificação curta do exemplo da spec do lexml-eta. Por pedido do usuário na aplicação do grupo 5, ele passa a ter a justificação **completa**: mantém os dois parágrafos originais e acrescenta todos os recursos da parte A (inline lado a lado, sub/sup, remissões externa e interna, `a` com e sem endereço, novas revisões `_rt` com nota dentro de `del`, novo comentário `_tc`, os 7 estilos, parágrafo vazio, notas formatadas e com remissão) e também lista numerada aninhada, lista com marcadores, tabela e imagem (parte B), com os metadados `lexedit` correspondentes.

### D7. Testes

- `DocumentoArticuladoConteudoTransformerTest` (XML -> FO, sem executável): título e posição, blocos e atributos dos parágrafos, cada classe, cada marca inline, `del`/`ins`, numeração das notas (incluindo a nota em `del`), propriedades zeradas no corpo da nota, link do `a`, remissões na justificação e na nota, justificação vazia, `ol`/`ul`/`table` não impressos.
- `DocumentoArticuladoPdfGeneratorTest` (PDF real com PDFBox): ordem do texto, versão revisada, fontes (negrito/itálico), anotações de link, texto da nota abaixo do corpo na mesma página, numeração contínua em várias páginas no documento longo, local e data e assinaturas ausentes.
- O teste atual `justificacaoAindaNaoImpressa` (exemplo) passa a verificar a justificação impressa na versão revisada.

## Risks / Trade-offs

- [Nota longa ou muitas notas no fim da página] O FOP pode levar a referência ou parte do corpo para a página seguinte. -> Comportamento padrão do XSL-FO, aceito; o documento longo da fixture serve para inspeção visual.
- [Classes geradas pelo editor mudarem quando o lexml-eta implementar a #991] -> Tratamento por token e classes desconhecidas ignoradas; ajuste localizado num template.
- [Propriedades herdadas pelo corpo da nota] -> D5 zera recuo, margens, alinhamento, peso, estilo e decoração; teste de FO verifica.
- [Modo `inline` compartilhado] Mudança no inline afeta ementa e articulação. -> Só são acrescentados elementos que a articulação não usa; os testes da #72 continuam valendo.

## Migration Plan

Não se aplica: biblioteca sem estado; o novo comportamento vale para os próximos PDFs gerados. PDFs antigos continuam abrindo (o anexo não muda).
