# Spec Delta

## MODIFIED Requirements

### Requirement: Parâmetros de impressão
O tamanho de fonte do texto, o tamanho de fonte de destaque, a entrelinha e os espaços entre parágrafos SHALL seguir as opções de impressão do documento (`lexedit:OpcoesImpressao`), com os valores da emenda. O tamanho da fonte do texto MUST ser o `tamanhoFonte` do documento quando for 14, 16 ou 18, e 14 quando ausente ou com outro valor; o tamanho de destaque (epígrafe e título da justificação) MUST ser 18pt quando a fonte for 18 e 16pt nos demais casos. Quando `reduzirEspacoEntreLinhas` for verdadeiro, a entrelinha MUST ser 120%, o espaço após parágrafo 0.45em e, na justificação, o espaço antes do título 22pt e antes do corpo 10pt; caso contrário (inclusive quando ausente), 150%, 0.6em, 26pt e 14pt. Os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho MUST NOT variar com essas opções. Elementos dimensionados em relação ao texto (notas de rodapé, células de tabela, sobrescrito e subscrito) MUST acompanhar o tamanho da fonte.

#### Scenario: Valores padrão
- **WHEN** um documento sem `lexedit:OpcoesImpressao`, ou com `tamanhoFonte="14"` e `reduzirEspacoEntreLinhas="false"`, é convertido em PDF
- **THEN** o texto é impresso em 14pt com entrelinha de 150%, e a epígrafe em 16pt

#### Scenario: Fonte 16
- **WHEN** o documento tem `tamanhoFonte="16"`
- **THEN** o texto e a epígrafe são impressos em 16pt

#### Scenario: Fonte 18
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto e a epígrafe são impressos em 18pt, e as notas de rodapé e o texto das células de tabela ficam maiores na mesma proporção

#### Scenario: Tamanho fora dos valores do editor
- **WHEN** o documento tem `tamanhoFonte="11"`
- **THEN** o texto é impresso em 14pt

#### Scenario: Reduzir espaço entre linhas
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** as linhas da articulação e da justificação ficam com entrelinha de 120% do tamanho da fonte, os parágrafos com 0.45em de espaço depois, e o título "JUSTIFICAÇÃO" com 22pt antes

#### Scenario: Espaços fixos
- **WHEN** o documento tem `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"`
- **THEN** os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho são os mesmos do padrão
