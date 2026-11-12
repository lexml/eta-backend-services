# Proposal

## Why

O editor (`lexml-eta`, issues #1053 e #1060) passou a gravar epígrafes com mais de uma linha, separadas pelo elemento `br` (substitutivo do Senado, PLV), e exige o `jsonix-lexml` 2.4.0 para isso. O PDF do documento articulado imprime a epígrafe com `xsl:value-of`, que ignora o `br`: as linhas saem emendadas numa só, em negrito. A issue #84 pede a primeira linha como hoje e as demais logo abaixo, sem negrito, como a epígrafe das emendas.

## What Changes

- A epígrafe passa a ser impressa linha a linha, separando o texto nos elementos `br`.
- A primeira linha mantém a formatação atual: centralizada, em negrito, no tamanho de destaque, numa linha só, com as pontas aparadas, os espaços internos preservados e o vão do número de projeto não numerado.
- Cada linha seguinte é impressa logo abaixo, centralizada, sem negrito e no mesmo tamanho de destaque, como o complemento da epígrafe das emendas, com as mesmas regras de aparar as pontas e preservar os espaços internos. Há quantas linhas o documento tiver.
- Linhas vazias (dois `br` seguidos ou `br` numa ponta) não são impressas.
- O título nos metadados do PDF (XMP) junta as linhas com um espaço, em vez de emendá-las.
- Epígrafe sem `br` sai exatamente como hoje, e o restante do documento não muda de posição.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Epígrafe" passa a admitir várias linhas (primeira em negrito, demais sem negrito); o requisito "Metadados de PDF/A e verificação do PDF" passa a juntar as linhas da epígrafe no título.

## Impact

- `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl` (template `lx:Epigrafe`).
- `DocumentoArticuladoTemplateProcessor#titulo` (título do XMP).
- Testes: `DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoTemplateProcessorTest` e uma fixture com epígrafe de várias linhas.
- Documentação: `CLAUDE.md` e o `README.md` dos PDFs gerados, se novos PDFs forem gerados.
- Sem mudança de API Java nem de dependência. **Dependência externa:** só documentos convertidos pelo `jsonix-lexml` 2.4.0 ou mais novo trazem `br` na epígrafe; com versão anterior, a conversão de um documento assim falha antes do PDF (comportamento do CLI, fora desta change). O host (`lexeditweb-editor`) já precisa da 2.4.0 por causa do `lexml-eta`.
