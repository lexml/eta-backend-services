# Design

## Context

O template `lx:Epigrafe` de `documento-articulado-conteudo.xsl` gera um único `fo:block` (centralizado, negrito, `$maxTamanhoFonte`) com o valor textual da epígrafe inteira: apara as pontas (`aparar-inicio`/`aparar-fim`), garante o vão do número (`vao-do-numero`) e troca os espaços por `&#160;`. O `br` gravado pelo `lexml-eta` é o elemento `br` do namespace LexML (`http://www.lexml.gov.br/1.0`, prefixo `lx` na XSLT), sem texto, e por isso some no `xsl:value-of`. O espaço de 48pt depois da epígrafe é o `space-before` da ementa, não um `space-after` da epígrafe.

No JSON, o `br` é `{ name: { localPart: 'br', … }, value: { TYPE_NAME: 'br_gov_lexml__1.Br' } }` no `content` da epígrafe; `DocumentoArticuladoTemplateProcessor#titulo` concatena só os textos, emendando as linhas no título do XMP.

A referência de formatação é a emenda (`template-velocity-emenda.xml`): `epigrafe.texto` em negrito e, num bloco abaixo, `epigrafe.complemento` sem negrito, ambos centralizados e em `$maxTamanhoFonte`, sem espaço entre os dois.

## Goals / Non-Goals

**Goals:**
- Primeira linha como hoje; demais linhas abaixo, sem negrito; epígrafe sem `br` idêntica à de hoje.
- Título do XMP com as linhas separadas por espaço.

**Non-Goals:**
- Formatação inline dentro da epígrafe (`b`, `i`): continua lida só como texto, como hoje.
- Epígrafe da emenda e do parecer (templates próprios, fluxos isolados).
- Atualizar o `jsonix-lexml` do host (é do `lexeditweb-editor`).

## Decisions

### D1. Linhas por contagem de `br` anteriores, na XSLT

A linha *n* (0 = primeira) é o valor textual dos nós filhos da epígrafe que não são `lx:br` e têm *n* irmãos `lx:br` antes (`count(preceding-sibling::lx:br) = $n`). A primeira linha passa pelo mesmo tratamento de hoje (aparar, `vao-do-numero`, `&#160;`) e sai no bloco atual. Para cada `lx:br` (`xsl:for-each`, `$n = position()`), a linha seguinte é aparada, trocada para `&#160;` e, se não for vazia, sai num `fo:block` centralizado, `$maxTamanhoFonte`, sem `font-weight` (herdado normal). A primeira linha vazia também não gera bloco.

O bloco da primeira linha é mantido igual (mesmo atributo, mesmo texto) para que a epígrafe sem `br` fique byte a byte como hoje: os testes existentes do template continuam valendo.

Alternativas descartadas:
- **Pré-processar o XML em Java** (trocar `br` por marcador e dividir): espalha a regra entre Java e XSLT, contra a arquitetura atual, em que o conteúdo é só da XSLT.
- **`fo:block` vazio no lugar do `br`, dentro do mesmo bloco em negrito**: quebraria a linha, mas não permitiria tirar o negrito das demais sem `fo:inline`, e o vão do número/`&#160;` são calculados sobre o texto inteiro.
- **`vao-do-numero` em todas as linhas**: só a primeira tem "Nº" de projeto não numerado; na segunda (referência ao documento, já numerado) o vão não se aplica, e aplicá-lo arriscaria mexer em texto legítimo.

### D2. Título do XMP

Em `acumularTexto`, um item cujo `name.localPart` é `br` acrescenta um espaço; a normalização de espaços que já existe junta e apara. Sem `br`, o título não muda.

### D3. Testes sem o executável

- `DocumentoArticuladoConteudoTransformerTest`: XML mínimo com `<br/>` no namespace LexML (como o `jsonix-lexml` 2.4.0 grava), cobrindo duas e três linhas, linhas vazias, vão do número na primeira linha e epígrafe sem `br` inalterada.
- `DocumentoArticuladoTemplateProcessorTest`: título com `br`.
- `DocumentoArticuladoPdfGeneratorTest`: um par de fixtures novo (`documento-com-epigrafe-varias-linhas`), com o XML gerado pelo `jsonix-lexml` 2.4.0 real (`C:/jsonix-lexml-win/desenv`) a partir de um JSON do `lexml-eta`, registrado no `ConversorDocumentoArticuladoFake`; o teste confere pelo PDFBox as linhas na ordem, a fonte em negrito só na primeira e a ementa 48pt abaixo da última.

## Risks / Trade-offs

- [Host com `jsonix-lexml` anterior à 2.4.0] → a conversão de documento com `br` falha antes do PDF (o CLI sai com código 0 e saída vazia, que o `ConversorDocumentoArticuladoCli` já trata como falha). Registrado no `CLAUDE.md`; a atualização é do host.
- [`br` dentro de um elemento inline (ex.: `<b>a<br/>b</b>`)] → o `preceding-sibling` só vê `br` filho direto da epígrafe; o `lexml-eta` não grava esse caso (o `br` é sempre filho direto, pelo conversor da epígrafe). Fica como hoje: o `br` aninhado é ignorado.
