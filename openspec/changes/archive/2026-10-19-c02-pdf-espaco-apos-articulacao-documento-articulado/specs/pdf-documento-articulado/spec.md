# Spec Delta

## MODIFIED Requirements

### Requirement: Formatação dos dispositivos
Os dispositivos SHALL ser impressos com texto justificado, recuo de primeira linha de 2,5cm, a entrelinha dos parâmetros de impressão e nenhum espaço adicional entre um dispositivo e o seguinte. Depois do último dispositivo, a articulação MUST terminar com o espaço após o parágrafo dos parâmetros de impressão (0.6em; 0.45em com "reduzir espaço entre linhas"), antes do que vier a seguir (justificação ou local e data). O rótulo MUST ser impresso em negrito, exceto o rótulo da pena, que MUST ser impresso em fonte regular. No texto, negrito e itálico MUST ser preservados, e as referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A articulação MUST NOT ser envolvida por aspas.

#### Scenario: Rótulo em negrito
- **WHEN** o PDF de um documento com o artigo "Art. 1º Esta Lei tipifica os crimes ..." é gerado
- **THEN** "Art. 1º" é impresso em negrito e o texto do caput em fonte regular, no mesmo bloco

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
