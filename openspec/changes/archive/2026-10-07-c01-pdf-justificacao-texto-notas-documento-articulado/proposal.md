# Proposal

## Why

Com a issue [#72](https://github.com/lexml/eta-backend-services/issues/72) concluída, o PDF da proposição imprime a parte inicial e a articulação, mas não a justificação. A issue [#75](https://github.com/lexml/eta-backend-services/issues/75) pede que a justificação seja impressa, testando todas as funcionalidades de formatação do editor, tratando as notas de rodapé e imprimindo a versão revisada do texto (sem as exclusões e com as inclusões sem destaque).

A #75 foi dividida em duas partes. Esta é a **parte A**: título, parágrafos, formatação inline, estilos de parágrafo do editor, marcas de revisão e notas de rodapé. A **parte B** (listas, tabelas e imagens) fica para uma change seguinte, porque exige layout próprio no XSL-FO.

O formato de entrada segue a especificação do lexml-eta em `docs/extensao-formato-lexml/` (06 – justificação e conteúdo rico, 07 – notas de rodapé, 09 – revisão de texto). O lexml-eta ainda não salva a justificação nesse formato (issues lexml-eta#991, #993 e #994 abertas); por isso esta change cria documentos de exemplo que seguem a especificação. A conversão JSON <-> XML do `jsonix-lexml` não faz parte desta atividade.

## What Changes

- O PDF passa a imprimir a **justificação** (`ProjetoNorma/Justificacao/PartePrincipal`) depois da articulação, com o título "JUSTIFICAÇÃO" e a formatação da justificação da emenda (título centralizado em negrito no tamanho de destaque; parágrafos com entrelinha, recuo de primeira linha de 2,5cm e espaço entre parágrafos dos parâmetros de impressão).
- **Formatação inline** do editor: negrito, itálico, sublinhado, subscrito e sobrescrito. Links (`a` com `xlink:href`) e remissões (`span`/`Remissao` com `xlink:href`) da justificação seguem o padrão de links já usado no PDF.
- **Estilos de parágrafo** do editor, pelas classes do Quill, com os mesmos valores usados pela emenda: alinhamento (`ql-align-*`), sem recuo (`ql-text-indent-0px`), sem espaço depois (`ql-margin-bottom-0px`), estilo ementa (`estilo-ementa`) e estilo norma alterada (`estilo-norma-alterada`).
- **Marcas de revisão**: o conteúdo de `del` não é impresso; o de `ins` é impresso como texto normal. Trechos comentados (`span` com id `_tc…`) saem como texto normal.
- **Notas de rodapé** (`NotaDeRodape`, inline): número sobrescrito no texto e o texto da nota no rodapé da página, com separador e numeração sequencial calculada na impressão (notas dentro de exclusões não são impressas nem numeradas).
- Listas, tabelas e imagens da justificação **ainda não são impressas** (parte B). Local e data e assinaturas continuam sem impressão.
- **Documentos de exemplo** novos, seguindo a especificação do lexml-eta: um catálogo com todos os recursos da parte A e uma justificação longa, para testar notas em várias páginas.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: a justificação deixa de constar entre os elementos não impressos ("Conteúdo impresso"); as remissões da justificação e das notas de rodapé passam a ter link ("Remissões e links"); novos requisitos para a impressão da justificação, das marcas de revisão e das notas de rodapé.

## Impact

- `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`: novos templates para a justificação, parágrafos, classes do Quill, inline, revisões e notas de rodapé.
- `src/main/resources/documentoarticulado/template-velocity-documento-articulado.xml`: separador das notas de rodapé (`xsl-footnote-separator`) na sequência de páginas.
- Testes (`DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoPdfGeneratorTest`, `ConversorDocumentoArticuladoFake`/`FakeTest`) e novas fixtures em `src/test/resources/documentoarticulado/`.
- `CLAUDE.md` (seção `etaservices.documentoarticulado`).
- Sem impacto em emenda, parecer ou `lexmljsonix`; sem mudança de API pública nem de dependências.
