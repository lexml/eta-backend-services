# Spec Delta

## REMOVED Requirements

### Requirement: PDF sem impressão do texto nesta etapa
**Reason**: A partir desta etapa (issue #72, parte 1) o PDF passa a imprimir o texto da proposição, então a proibição de imprimir texto deixa de valer.
**Migration**: Os metadados de PDF/A, o título e o hash de verificação continuam exigidos pelo requisito "Metadados de PDF/A e verificação do PDF". O que é impresso passa a ser definido pelos requisitos "Impressão da parte inicial", "Epígrafe", "Ementa" e "Preâmbulo".

## ADDED Requirements

### Requirement: Metadados de PDF/A e verificação do PDF
O PDF gerado SHALL conter os metadados XMP de PDF/A (título, datas de criação e modificação, identificação PDF/A parte 3, conformidade B) e o hash de verificação MD5 do arquivo no XMP. O título do documento MUST ser a epígrafe da proposição quando houver, ou a URN de identificação caso contrário.

#### Scenario: Título a partir da epígrafe
- **WHEN** o documento possui epígrafe "PROJETO DE LEI Nº , DE"
- **THEN** o título nos metadados do PDF é o texto da epígrafe, sem espaços excedentes

#### Scenario: Título a partir da URN
- **WHEN** o documento não possui epígrafe
- **THEN** o título nos metadados do PDF é a URN de `metadado.identificacao.urn`

#### Scenario: Hash de verificação preenchido
- **WHEN** o PDF é gerado
- **THEN** o XMP não contém mais o valor de placeholder `00000000000000000000000000000000` no hash de verificação

### Requirement: Impressão da parte inicial
O PDF SHALL imprimir a parte inicial da proposição na ordem epígrafe, ementa e preâmbulo, a partir do `documento-articulado.xml` gerado para o documento. Nesta etapa a articulação, a justificação, o local e data e as assinaturas MUST NOT ser impressos.

#### Scenario: Ordem da parte inicial
- **WHEN** um documento com epígrafe, ementa e preâmbulo é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, depois a ementa e depois o preâmbulo

#### Scenario: Articulação ainda não impressa
- **WHEN** um documento com articulação é convertido em PDF
- **THEN** o texto extraído do PDF não contém o texto dos dispositivos da articulação

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe)
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio no lugar do ausente

### Requirement: Epígrafe
A epígrafe SHALL ser impressa centralizada, em negrito, no tamanho de fonte de destaque, em uma única linha (espaços não quebráveis) e com os espaços excedentes do texto removidos. O complemento da epígrafe usado na emenda MUST NOT ser impresso.

#### Scenario: Epígrafe formatada
- **WHEN** o documento possui a epígrafe "MEDIDA PROVISÓRIA Nº 999, DE 2026 " (com espaço sobrando)
- **THEN** o PDF apresenta "MEDIDA PROVISÓRIA Nº 999, DE 2026" centralizada, em negrito e no tamanho de destaque, sem outro texto de complemento abaixo

### Requirement: Ementa
A ementa SHALL ser impressa 48pt abaixo da epígrafe, justificada, com recuo à esquerda de 6,5cm e sem recuo de primeira linha, preservando negrito e itálico. Nesta etapa o texto de referências com link (`span` com `xlink:href`) MUST ser impresso como texto simples, sem link. A ementa MUST NOT ser envolvida por aspas.

#### Scenario: Ementa com referência a norma
- **WHEN** a ementa contém "Altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, que estabelece ..."
- **THEN** o PDF apresenta o texto "Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece ..." com o recuo da ementa, sem link e sem aspas

#### Scenario: Ementa com formatação inline
- **WHEN** a ementa contém trecho em itálico ou negrito
- **THEN** o trecho é apresentado em itálico ou negrito no PDF

### Requirement: Preâmbulo
O preâmbulo SHALL ser impresso 72pt abaixo da ementa, justificado e com recuo de primeira linha de 2,5cm, somente como texto: qualquer formatação inline MUST ser descartada. Cada parágrafo do preâmbulo SHALL ser impresso como um bloco próprio, com os espaços excedentes removidos.

#### Scenario: Preâmbulo sem formatação inline
- **WHEN** o preâmbulo contém "<b>O PRESIDENTE DA REPÚBLICA</b>, no uso da atribuição que lhe confere o art. 62 da Constituição, adota a seguinte Medida Provisória, com força de lei:"
- **THEN** o PDF apresenta o texto do preâmbulo sem negrito, com recuo de primeira linha de 2,5cm

### Requirement: Parâmetros de impressão
O tamanho de fonte do texto, o tamanho de fonte de destaque, a entrelinha e o espaço entre parágrafos SHALL ser parâmetros da geração, com os valores padrão 14pt, 16pt, 150% e 0.6em. Nesta etapa os valores padrão MUST ser usados independentemente das opções de impressão registradas no documento.

#### Scenario: Valores padrão
- **WHEN** um documento é convertido em PDF
- **THEN** o texto é impresso em 14pt com entrelinha de 150%, e a epígrafe em 16pt
