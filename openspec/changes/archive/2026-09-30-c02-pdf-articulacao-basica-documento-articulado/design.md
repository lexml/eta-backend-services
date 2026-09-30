# Design

## Context

Motivação e escopo: ver `proposal.md`. Requisitos: ver `specs/pdf-documento-articulado/spec.md`.

Estado atual (parte 1, change `2026-09-30-c01-pdf-parte-inicial-documento-articulado`):

- A XSLT `documentoarticulado/documento-articulado-conteudo.xsl` gera um fragmento FO com raiz `fo:block`. Ela imprime `lx:ParteInicial` e tem templates vazios para `lx:Articulacao` e `lx:ParteFinal`.
- O texto inline da ementa é tratado no modo `inline`: `b` e `i` formatados, qualquer outro elemento vira só o texto.
- Parâmetros disponíveis: `maxTamanhoFonte`, `lineHeight`, `pMarginBottom`. O fluxo tem `font-size` de 14pt.

Estrutura da articulação nos três documentos da #72 (fixtures em `src/test/resources/documentoarticulado/`):

```
Articulacao
|-- Capitulo / Secao (Rotulo, NomeAgrupador, Artigo...)   <- agrupadores: artigos DENTRO deles
'-- Artigo
    |-- TituloDispositivo                                  <- parte 6
    |-- Rotulo "Art. 2º"                                   <- rótulo fica no Artigo...
    |-- Caput
    |   |-- p                                              <- ...o texto fica no Caput
    |   |-- Inciso (Rotulo, p, Alinea (Rotulo, p, Item (Rotulo, p)))
    |   |-- Pena (Rotulo, p)                               <- parte 6
    |   '-- Alteracao (Artigo, Omissis, ...)               <- parte 4
    '-- Paragrafo (Rotulo, p, Inciso..., Pena)
```

Como a emenda imprime a citação de dispositivos, observado no código:

- `template-velocity-emenda.xml` coloca a citação dentro do bloco "Comando de emenda" (`line-height="$lineHeight" text-align="justify" text-indent="2.5cm"`), envolvida por um único `<fo:block margin-bottom="$pMarginBottom">`.
- `citacao2html` troca `Rotulo` por `strong`. No `xhtml2fo.xsl`, cada `p` vira um `<fo:block>` **sem margens próprias** e `strong` vira `fo:inline font-weight="bold"`.
- Resultado: todos os dispositivos herdam o recuo de 2,5cm e a entrelinha, e **não há espaço extra entre eles**. A imagem `03-exemplo-preambulo-e-articulacao.png` da #72 confirma.

## Goals / Non-Goals

**Goals:**
- Imprimir Artigo, Caput, Parágrafo, Inciso, Alínea e Item com a formatação da citação da emenda.
- Atravessar agrupadores para imprimir os artigos de dentro.
- Deixar pontos de extensão explícitos na XSLT para as partes 3 a 6.

**Non-Goals:**
- Rótulo e nome de agrupadores (parte 3); `Alteracao` e `Omissis` (parte 4); links e `id` de destino (parte 5); `Pena` e `TituloDispositivo` (parte 6).
- Mudanças em Java, no template Velocity ou nos parâmetros de impressão.

## Decisions

### D1. Modo próprio para a estrutura da articulação

A articulação é percorrida no modo `dispositivo`, e o texto de cada `p` continua no modo `inline` já existente. Separar os modos evita que os templates estruturais e os de texto interfiram entre si, e que um template genérico (`*`) de um modo pegue elementos do outro.

```
lx:Articulacao ------------------------> fo:block (wrapper: justify, text-indent 2.5cm, line-height)
   apply-templates mode="dispositivo"
     |-- agrupador (Parte|Livro|Titulo|Capitulo|Secao|Subsecao|Agrupamento)
     |      -> só atravessa: apply-templates nos filhos, exceto Rotulo e NomeAgrupador (parte 3)
     |-- lx:Artigo
     |      -> Caput com o Rotulo do artigo como parâmetro
     |      -> depois os demais filhos (Paragrafo...), exceto Rotulo, Caput e TituloDispositivo
     |-- lx:Caput           -> bloco(rótulo recebido + 1º p), p seguintes, filhos (Inciso...)
     |-- lx:Paragrafo|Inciso|Alinea|Item
     |                      -> bloco(próprio Rotulo + 1º p), p seguintes, filhos
     |-- lx:Alteracao, lx:Omissis        -> vazio (parte 4)
     |-- lx:Pena, lx:TituloDispositivo   -> vazio (parte 6)
     '-- * (qualquer outro)              -> vazio
```

Os dispositivos compartilham um template nomeado `bloco-dispositivo(rotulo, paragrafos)`, que gera:

```
<fo:block><fo:inline font-weight="bold">{normalize-space(rotulo)}</fo:inline> {1º p no modo inline}</fo:block>
<fo:block>{p seguintes, se houver}</fo:block>
```

- O espaço entre o rótulo e o texto é um `xsl:text` de um espaço. Os espaços do início do `p` (` Esta Lei...`) são colapsados pelo XSL-FO, como na ementa (D3 da parte 1).
- Dispositivo sem `p` imprime só o rótulo. Dispositivo sem rótulo e sem texto não gera bloco.
- Alternativa descartada: um template por tipo de dispositivo. Todos teriam o mesmo corpo, e o template nomeado concentra a formatação num lugar só. As partes 3 a 6 sobrescrevem só o que muda (ex.: rótulo sem negrito na `Pena`).

### D2. Formatação copiada da emenda

| Elemento | FO gerado | Origem (emenda) |
|---|---|---|
| Wrapper da articulação | `fo:block line-height="{$lineHeight}" text-align="justify" text-indent="2.5cm"` | bloco "Comando de emenda" de `template-velocity-emenda.xml` |
| Cada dispositivo | `fo:block` sem margens (herda o recuo, a justificação e a entrelinha) | `xhtml2fo.xsl`, `match="p"` |
| Rótulo | `fo:inline font-weight="bold"` | `citacao2html` (`Rotulo` → `strong`) e `xhtml2fo.xsl` (`strong`) |
| Texto | modo `inline`: `b`/`i` formatados; `span`, `Remissao` e outros → texto | como a ementa (parte 1) |

- **Espaço entre preâmbulo e articulação:** o `margin-bottom` (0.6em) do último parágrafo do preâmbulo, equivalente ao espaço entre o cabeçalho do comando e a citação na emenda. Nenhum `space-before` no wrapper.
- **Aspas:** não são geradas; na emenda elas vêm do front, dentro do texto da citação.
- Os valores ficam na XSLT com comentário de origem, como na parte 1. Nenhum recurso da emenda é importado.

### D3. Ajuste dos testes da parte 1

Testes existentes que deixam de valer:

- `DocumentoArticuladoConteudoTransformerTest`:
  - `fragmentoBemFormado...` espera 3 blocos na raiz: passa a esperar 4 (o wrapper da articulação);
  - `naoImprimeTextoDosDispositivos`: passa a verificar os elementos ainda não impressos (agrupador, alteração, pena, título de dispositivo);
  - `documentoSemParteInicial...`: o documento mínimo tem um artigo, que agora é impresso.
- `DocumentoArticuladoPdfGeneratorTest.imprimeAParteInicialNaOrdemSemOsDispositivos`: passa a exigir os artigos depois do preâmbulo e a ausência dos elementos das partes 3, 4 e 6.

## Risks / Trade-offs

- [O template genérico vazio (`*` no modo `dispositivo`) omite sem aviso elementos não previstos] → é intencional nesta etapa, e cada omissão conhecida tem template explícito com a parte responsável. Os testes cobrem os três documentos da #72. As partes seguintes removem as omissões.
- [Uma nota de rodapé (`NotaDeRodape`) dentro de um dispositivo sairia inline, pelo modo `inline` genérico] → nenhum documento de teste tem nota em dispositivo; o tratamento de notas está fora da #72.
- [Artigo que altera norma mostra só "passa a vigorar com as seguintes alterações:"] → esperado até a parte 4.
- [Documentos maiores passam a ter várias páginas] → a página mestra "restPage" já existe no template; revalidar no veraPDF a versão v2 dos PDFs.

## Migration Plan

Só a XSLT muda. PDFs gerados a partir desta versão passam a ter os dispositivos impressos. A extração do JSON não muda. Rollback: reverter a change.
