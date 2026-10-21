# Spec Delta

## MODIFIED Requirements

### Requirement: Formatação dos dispositivos
Os dispositivos SHALL ser impressos com texto justificado, recuo de primeira linha de 2,5cm, a entrelinha dos parâmetros de impressão e nenhum espaço adicional entre um dispositivo e o seguinte. Depois do último dispositivo, a articulação MUST terminar com o espaço após o parágrafo dos parâmetros de impressão (0.6em; 0.45em com "reduzir espaço entre linhas"), antes do que vier a seguir (justificação ou local e data). O rótulo MUST ser impresso em negrito, exceto o rótulo da pena, que MUST ser impresso em fonte regular. O rótulo MUST ser tratado como uma palavra só na justificação: os espaços internos dele (ex.: entre "Art." e "15.", entre "§" e "1º") MUST NOT ser esticados, ficando com a mesma largura em todas as linhas; o espaço entre o rótulo e o texto continua ajustável como os demais espaços da linha. No texto, negrito e itálico MUST ser preservados, e as referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A articulação MUST NOT ser envolvida por aspas.

#### Scenario: Rótulo em negrito
- **WHEN** o PDF de um documento com o artigo "Art. 1º Esta Lei tipifica os crimes ..." é gerado
- **THEN** "Art. 1º" é impresso em negrito e o texto do caput em fonte regular, no mesmo bloco

#### Scenario: Rótulo como uma palavra só
- **WHEN** o PDF apresenta dois artigos, "Art. 15." e "Art. 16.", cujas primeiras linhas são justificadas com folgas diferentes
- **THEN** a distância entre "Art." e o número é a mesma nos dois rótulos, e só os espaços fora do rótulo absorvem a folga da linha

#### Scenario: Mesmo recuo em todos os níveis
- **WHEN** o PDF apresenta um artigo, um parágrafo, um inciso e uma alínea
- **THEN** todos têm recuo de primeira linha de 2,5cm, e as linhas seguintes começam na margem esquerda

#### Scenario: Referência a norma no texto do dispositivo
- **WHEN** o caput contém "Esta Lei altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, para ..."
- **THEN** o PDF apresenta "Esta Lei altera a Lei nº 9.394, de 20 de dezembro de 1996, para ..." com "Lei nº 9.394, de 20 de dezembro de 1996" como link para o portal normas.leg.br

#### Scenario: Itálico no texto do dispositivo
- **WHEN** o texto de um parágrafo contém um trecho em itálico
- **THEN** o trecho é impresso em itálico

#### Scenario: Espaço depois da articulação
- **WHEN** um documento em fonte 14, sem "reduzir espaço entre linhas", tem justificação depois da articulação
- **THEN** a distância entre a linha de base do último dispositivo e a do título "JUSTIFICAÇÃO" é de 55pt (8,4pt do espaço após a articulação somados aos 26pt antes do título e às entrelinhas), e entre dois dispositivos continua sem espaço adicional

### Requirement: Epígrafe
A epígrafe SHALL ser impressa centralizada, em negrito, no tamanho de fonte de destaque e em uma única linha (espaços não quebráveis). Os espaços, quebras de linha e tabulações do início e do fim do texto MUST ser removidos; os espaços internos MUST ser preservados, sem serem colapsados (quebras de linha e tabulações internas valem como espaço). Quando, depois de "Nº" (ou "N°"), houver só espaços até a vírgula seguinte (projeto não numerado), esse vão MUST ser impresso com os espaços gravados no documento, com no mínimo 8: um vão menor (inclusive vazio) MUST ser impresso com 8 espaços. Nenhum texto de complemento MUST ser impresso abaixo da epígrafe. A epígrafe MUST NOT alterar a posição do conteúdo que vem depois dela.

#### Scenario: Epígrafe formatada
- **WHEN** o documento possui a epígrafe "MEDIDA PROVISÓRIA Nº 999, DE 2026 " (com espaço sobrando)
- **THEN** o PDF apresenta "MEDIDA PROVISÓRIA Nº 999, DE 2026" centralizada, em negrito e no tamanho de destaque, sem outro texto de complemento abaixo

#### Scenario: Projeto não numerado
- **WHEN** o documento possui a epígrafe "PROJETO DE LEI Nº     , DE" (cinco espaços no lugar do número), ou "PROJETO DE LEI Nº , DE" (um só espaço)
- **THEN** o PDF apresenta "PROJETO DE LEI Nº" seguido de um vão de 8 espaços e de ", DE", em uma linha

#### Scenario: Vão maior gravado pelo editor
- **WHEN** o documento possui a epígrafe "PROJETO DE LEI Nº" seguida de 15 espaços e de ", DE"
- **THEN** o PDF apresenta o vão com os 15 espaços, em uma linha

#### Scenario: Espaços internos preservados
- **WHEN** o documento possui uma epígrafe com dois espaços seguidos fora do vão do número e com espaços e quebras de linha nas pontas
- **THEN** o PDF mantém os dois espaços internos e não imprime os espaços das pontas

#### Scenario: Restante do documento inalterado
- **WHEN** o mesmo documento é gerado antes e depois do ajuste da epígrafe
- **THEN** todas as linhas do PDF, exceto a da epígrafe, ficam com o mesmo texto e na mesma posição

### Requirement: Parâmetros de impressão
O tamanho de fonte do texto, o tamanho de fonte de destaque, o tamanho dos alertas, a entrelinha e os espaços entre parágrafos SHALL seguir as opções de impressão do documento (`lexedit:OpcoesImpressao`). O tamanho da fonte do texto MUST ser o `tamanhoFonte` do documento quando for 14, 16 ou 18, e 14 quando ausente ou com outro valor; o tamanho de destaque (epígrafe e título da justificação) MUST ser 18pt quando a fonte for 18 e 16pt nos demais casos; o tamanho dos alertas MUST ser 2pt menor que o da fonte do texto (12pt, 14pt e 16pt). Quando `reduzirEspacoEntreLinhas` for verdadeiro, a entrelinha MUST ser 120%, o espaço após parágrafo 0.45em e, na justificação, o espaço antes do título 22pt e antes do corpo 10pt; caso contrário (inclusive quando ausente), 150%, 0.6em, 26pt e 14pt. Os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho MUST NOT variar com essas opções. Elementos dimensionados em relação ao texto (notas de rodapé, células de tabela, sobrescrito e subscrito) MUST acompanhar o tamanho da fonte.

#### Scenario: Valores padrão
- **WHEN** um documento sem `lexedit:OpcoesImpressao`, ou com `tamanhoFonte="14"` e `reduzirEspacoEntreLinhas="false"`, é convertido em PDF
- **THEN** o texto é impresso em 14pt com entrelinha de 150%, a epígrafe em 16pt e os alertas em 12pt

#### Scenario: Fonte 16
- **WHEN** o documento tem `tamanhoFonte="16"`
- **THEN** o texto e a epígrafe são impressos em 16pt, e os alertas em 14pt

#### Scenario: Fonte 18
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto e a epígrafe são impressos em 18pt, os alertas em 16pt, e as notas de rodapé e o texto das células de tabela ficam maiores na mesma proporção

#### Scenario: Tamanho fora dos valores do editor
- **WHEN** o documento tem `tamanhoFonte="11"`
- **THEN** o texto é impresso em 14pt e os alertas em 12pt

#### Scenario: Reduzir espaço entre linhas
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** as linhas da articulação e da justificação ficam com entrelinha de 120% do tamanho da fonte, os parágrafos com 0.45em de espaço depois, e o título "JUSTIFICAÇÃO" com 22pt antes

#### Scenario: Espaços fixos
- **WHEN** o documento tem `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"`
- **THEN** os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho são os mesmos do padrão

## ADDED Requirements

### Requirement: Caixa de pendências
O PDF SHALL apresentar, na primeira página, depois do cabeçalho e antes da epígrafe, quando o documento (`lexedit:Metadado`) tiver ao menos uma pendência, uma caixa de fundo amarelo e texto vermelho em negrito com o cabeçalho "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:", alinhado à esquerda e seguido de uma lista com marcadores ("•", sem negrito) com cada pendência do documento, na ordem e com o texto em que foi registrada. O texto da caixa MUST ter o tamanho dos alertas definido nos parâmetros de impressão. A lista MUST NOT ser filtrada nem reescrita. Revisões e comentários MUST NOT gerar caixas próprias: o aviso deles é o da pendência registrada pelo editor. Sem pendências, nenhuma caixa MUST ser impressa.

#### Scenario: Pendências como registradas
- **WHEN** o documento tem as pendências "Não foi informado um texto de justificação." e "Remover comentários."
- **THEN** a caixa de pendências lista os dois itens, nessa ordem e com esse texto, abaixo do cabeçalho "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:"

#### Scenario: Revisões e comentários sem caixa própria
- **WHEN** o documento tem revisões de texto, revisões da articulação e comentários, além de pendências
- **THEN** só a caixa de pendências é impressa, sem os avisos "Este documento contém marcas de revisão ..." e "Este documento contém comentários ..."

#### Scenario: Revisões e comentários sem pendências
- **WHEN** o documento tem revisões ou comentários, mas nenhuma pendência registrada
- **THEN** nenhuma caixa é impressa

#### Scenario: Sem alertas
- **WHEN** o documento não tem pendências (ex.: os documentos da issue #72)
- **THEN** nenhuma caixa é impressa e a primeira página começa pela epígrafe, na mesma posição de hoje

#### Scenario: Tamanho acompanha a fonte
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto da caixa de pendências é impresso em 16pt

## REMOVED Requirements

### Requirement: Alertas e pendências
**Reason**: Avaliação da chefia: as caixas de marcas de revisão e de comentários repetiam o que a lista de pendências registrada pelo editor já diz, e o texto do cabeçalho da lista foi simplificado. O texto dos alertas passa a acompanhar o tamanho da fonte.
**Migration**: Substituído pelo requisito "Caixa de pendências" (só a caixa de pendências, com o novo cabeçalho e o tamanho dos alertas dos parâmetros de impressão).
