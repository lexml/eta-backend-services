# Spec Delta

## MODIFIED Requirements

### Requirement: Ementa
A ementa SHALL ser impressa 48pt abaixo da epígrafe, justificada, com recuo à esquerda de 6,5cm e sem recuo de primeira linha, preservando negrito e itálico. As referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A ementa MUST NOT ser envolvida por aspas.

#### Scenario: Ementa com referência a norma
- **WHEN** a ementa contém "Altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, que estabelece ..."
- **THEN** o PDF apresenta o texto "Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece ..." com o recuo da ementa, sem aspas, e "Lei nº 9.394, de 20 de dezembro de 1996" como link para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394`

#### Scenario: Ementa com formatação inline
- **WHEN** a ementa contém trecho em itálico ou negrito
- **THEN** o trecho é apresentado em itálico ou negrito no PDF

### Requirement: Formatação dos dispositivos
Os dispositivos SHALL ser impressos com a formatação da citação de dispositivos da emenda: texto justificado, recuo de primeira linha de 2,5cm, a entrelinha dos parâmetros de impressão e nenhum espaço adicional entre um dispositivo e o seguinte. O rótulo MUST ser impresso em negrito, exceto o rótulo da pena, que MUST ser impresso em fonte regular. No texto, negrito e itálico MUST ser preservados, e as referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A articulação MUST NOT ser envolvida por aspas.

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

## ADDED Requirements

### Requirement: Remissões e links
As referências com `xlink:href` (`span` ou `Remissao`) no texto da ementa, dos dispositivos e dos títulos de dispositivo SHALL ser impressas como links em cinza (`#808080`), destacadas do texto preto, sem sublinhado. A referência cujo `xlink:href` é uma URN (começa com `urn:`) MUST ser um link para `https://normas.leg.br/?urn=` seguido da URN sem codificação. A referência cujo `xlink:href` é o identificador de um dispositivo impresso do próprio documento MUST ser um link interno para esse dispositivo; quando o identificador não corresponde a um dispositivo impresso, a referência MUST ser impressa como texto simples, sem link e sem marca visual. O preâmbulo e o nome dos agrupadores MUST continuar impressos somente como texto.

#### Scenario: Remissão externa a uma lei
- **WHEN** o texto contém "<span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>"
- **THEN** o PDF apresenta "Lei nº 9.394, de 20 de dezembro de 1996" em cinza, com link para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394`

#### Scenario: Remissão externa a um dispositivo de lei
- **WHEN** o texto contém uma referência com `xlink:href="urn:lex:br:federal:lei:1990-07-13;8069!art58"`
- **THEN** o link aponta para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1990-07-13;8069!art58`

#### Scenario: Remissão externa dentro de bloco de alteração
- **WHEN** um dispositivo de um bloco de alteração de norma vigente contém uma referência com URN
- **THEN** a referência é impressa como link para o portal normas.leg.br, como fora do bloco

#### Scenario: Remissão interna válida
- **WHEN** o caput do Art. 2º contém "<Remissao xlink:href="art1">art. 1º</Remissao>" e o documento tem o Art. 1º
- **THEN** "art. 1º" é impresso em cinza, com link interno que leva ao Art. 1º no PDF

#### Scenario: Remissão interna inválida
- **WHEN** o texto contém "<Remissao xlink:href="art4">art. 4º</Remissao>" e o documento não tem o Art. 4º
- **THEN** "art. 4º" é impresso como texto simples, na cor do texto e sem link

#### Scenario: Preâmbulo sem link
- **WHEN** o preâmbulo contém uma referência com `xlink:href`
- **THEN** o PDF apresenta o texto da referência sem link, como o restante do preâmbulo
