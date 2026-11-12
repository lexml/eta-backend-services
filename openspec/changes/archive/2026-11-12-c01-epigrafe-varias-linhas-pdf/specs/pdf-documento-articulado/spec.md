# Spec Delta

## MODIFIED Requirements

### Requirement: Metadados de PDF/A e verificação do PDF
O PDF gerado SHALL conter os metadados XMP de PDF/A (título, datas de criação e modificação, identificação PDF/A parte 3, conformidade B) e o hash de verificação MD5 do arquivo no XMP. O título do documento MUST ser a epígrafe da proposição quando houver, ou a URN de identificação caso contrário. Quando a epígrafe tiver várias linhas, o título MUST juntá-las com um espaço.

#### Scenario: Título a partir da epígrafe
- **WHEN** o documento possui epígrafe "PROJETO DE LEI Nº , DE"
- **THEN** o título nos metadados do PDF é o texto da epígrafe, sem espaços excedentes

#### Scenario: Título a partir de epígrafe com várias linhas
- **WHEN** o documento possui a epígrafe "EMENDA Nº - CCJ (SUBSTITUTIVO)", uma quebra de linha e "(ao Projeto de Lei nº 1.234, de 2026)"
- **THEN** o título nos metadados do PDF é "EMENDA Nº - CCJ (SUBSTITUTIVO) (ao Projeto de Lei nº 1.234, de 2026)"

#### Scenario: Título a partir da URN
- **WHEN** o documento não possui epígrafe
- **THEN** o título nos metadados do PDF é a URN de `metadado.identificacao.urn`

#### Scenario: Hash de verificação preenchido
- **WHEN** o PDF é gerado
- **THEN** o XMP não contém mais o valor de placeholder `00000000000000000000000000000000` no hash de verificação

### Requirement: Epígrafe
A epígrafe SHALL ser impressa centralizada, em negrito, no tamanho de fonte de destaque e em uma única linha (espaços não quebráveis). Os espaços, quebras de linha e tabulações do início e do fim do texto MUST ser removidos; os espaços internos MUST ser preservados, sem serem colapsados (quebras de linha e tabulações internas valem como espaço). Quando, depois de "Nº" (ou "N°"), houver só espaços até a vírgula seguinte (projeto não numerado), esse vão MUST ser impresso com os espaços gravados no documento, com no mínimo 8: um vão menor (inclusive vazio) MUST ser impresso com 8 espaços. Quando a epígrafe tiver elementos `br`, essas regras valem para a primeira linha, e cada linha seguinte MUST ser impressa logo abaixo, centralizada, sem negrito, no tamanho de destaque, em uma única linha e com as pontas aparadas e os espaços internos preservados; linhas vazias MUST NOT ser impressas. Nenhum outro texto de complemento MUST ser impresso abaixo da epígrafe. A epígrafe sem `br` MUST NOT alterar a posição do conteúdo que vem depois dela, e o espaço entre a última linha da epígrafe e a ementa MUST ser o mesmo da epígrafe de uma linha.

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

#### Scenario: Epígrafe com duas linhas
- **WHEN** o documento possui a epígrafe "EMENDA Nº        - CCJ (SUBSTITUTIVO)", um elemento `br` e "(ao Projeto de Lei nº 1.234, de 2026)"
- **THEN** o PDF apresenta "EMENDA Nº        - CCJ (SUBSTITUTIVO)" centralizada, em negrito e no tamanho de destaque, e logo abaixo "(ao Projeto de Lei nº 1.234, de 2026)" centralizada, sem negrito e no tamanho de destaque

#### Scenario: Epígrafe com mais de duas linhas
- **WHEN** a epígrafe tem dois elementos `br` separando três trechos
- **THEN** o PDF apresenta a primeira linha em negrito e a segunda e a terceira, uma abaixo da outra, com a formatação da segunda linha

#### Scenario: Linhas vazias
- **WHEN** a epígrafe tem dois `br` seguidos, ou termina com `br`
- **THEN** o PDF não imprime linha vazia entre as linhas nem depois da última

#### Scenario: Projeto não numerado com segunda linha
- **WHEN** a epígrafe é "PROJETO DE LEI DE CONVERSÃO Nº" seguida de 7 espaços, de ", DE 2026", de um `br` e de "(Medida Provisória nº 1.234, de 2026)"
- **THEN** a primeira linha apresenta o vão do número com 8 espaços, e a segunda linha sai sem negrito, com o texto como gravado

#### Scenario: Restante do documento inalterado
- **WHEN** o mesmo documento, com epígrafe sem `br`, é gerado antes e depois do ajuste da epígrafe
- **THEN** todas as linhas do PDF, inclusive a da epígrafe, ficam com o mesmo texto e na mesma posição

#### Scenario: Ementa depois da última linha
- **WHEN** a epígrafe tem duas linhas
- **THEN** a ementa começa 48pt abaixo da segunda linha da epígrafe
