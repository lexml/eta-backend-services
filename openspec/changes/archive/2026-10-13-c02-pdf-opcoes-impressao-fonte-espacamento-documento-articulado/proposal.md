# Proposal

## Why

A issue [#74](https://github.com/lexml/eta-backend-services/issues/74) pede que o PDF da proposição respeite as configurações de impressão, deixando "tamanho de letra e espaçamento por último". A parte A (change `2026-10-13-c01`) fez o cabeçalho; esta é a **parte B**, que completa a #74: o PDF passa a usar o tamanho da fonte (`tamanhoFonte`) e a opção de reduzir o espaço entre linhas (`reduzirEspacoEntreLinhas`) registrados no documento (`lexedit:OpcoesImpressao`, especificação 02 do lexml-eta). Hoje são sempre usados os valores padrão (`ParametrosImpressaoDocumentoArticulado.padrao()`), como a spec registra explicitamente.

## What Changes

- **Tamanho da fonte** do texto conforme `tamanhoFonte` (14, 16 ou 18, os valores do editor); o tamanho de destaque (epígrafe e título "JUSTIFICAÇÃO") é 18pt quando a fonte é 18, senão 16pt, como na emenda. Valor ausente ou fora de 14, 16 e 18: 14.
- **Reduzir espaço entre linhas**: entrelinha de 120% (em vez de 150%), espaço após parágrafo de 0.45em (em vez de 0.6em) e, na justificação, 22pt antes do título e 10pt antes do corpo (em vez de 26pt e 14pt), os valores da emenda.
- Os espaços de 48pt (antes da ementa) e 72pt (antes do preâmbulo), definidos na #72, continuam fixos.
- Notas de rodapé, células de tabela, sobrescrito e subscrito, por serem relativos ao tamanho do texto, acompanham a fonte; o cabeçalho (ITC Stone Sans 11pt e texto) continua fixo, como na emenda.
- **Exemplo**: o `documento-articulado-exemplo` passa a ter `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"`, para exercitar as opções; o padrão continua coberto pelas outras fixtures.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Parâmetros de impressão" deixa de usar sempre os padrões e passa a seguir as opções de impressão do documento.

## Impact

- `ParametrosImpressaoDocumentoArticulado`: construção a partir das opções do documento, mais os dois espaços da justificação.
- Leitura das opções de impressão compartilhada entre os parâmetros e o cabeçalho (hoje no `DocumentoArticuladoTemplateProcessor`).
- `DocumentoArticuladoPdfGenerator`, `DocumentoArticuladoConteudoTransformer` (dois `xsl:param` a mais) e `documento-articulado-conteudo.xsl` (espaços da justificação parametrizados).
- Testes (`ParametrosImpressaoDocumentoArticuladoTest`, `DocumentoArticuladoConteudoTransformerTest`, `DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoPdfGeneratorTest`) e a fixture `documento-articulado-exemplo`; testes que medem posições no exemplo são ajustados.
- `CLAUDE.md`.
- Sem mudança de API pública nem de dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
