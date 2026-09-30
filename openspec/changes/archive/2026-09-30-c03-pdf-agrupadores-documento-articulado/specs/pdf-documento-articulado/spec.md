# Spec Delta

## MODIFIED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo) seguida da articulação, na ordem do documento. Nesta etapa MUST NOT ser impressos: os blocos de alteração de norma vigente e os omissis, a pena e o título de dispositivo, a justificação, o local e data e as assinaturas.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo e articulação é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo e os artigos, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe)
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento contém um bloco de alteração de norma vigente, pena ou título de dispositivo
- **THEN** o texto extraído do PDF não contém os dispositivos da norma alterada (ex.: "Art. 12-A."), a pena (ex.: "Pena –") nem o título de dispositivo

### Requirement: Dispositivos da articulação
O PDF SHALL imprimir os dispositivos Artigo, Caput, Parágrafo, Inciso, Alínea e Item na ordem do documento, cada um em um bloco próprio iniciado pelo seu rótulo, seguido de um espaço e do texto. O rótulo do artigo MUST ser impresso no mesmo bloco do texto do caput. Os dispositivos subordinados (incisos do caput ou do parágrafo, alíneas, itens) MUST aparecer logo após o texto do dispositivo a que pertencem. Artigos dentro de agrupadores MUST ser impressos logo após o rótulo e o nome do agrupador.

#### Scenario: Artigo com incisos e parágrafos
- **WHEN** um artigo tem caput com incisos e, depois, parágrafos com incisos
- **THEN** o PDF apresenta o rótulo do artigo com o texto do caput, os incisos do caput, e em seguida cada parágrafo seguido dos seus incisos

#### Scenario: Hierarquia até item
- **WHEN** um inciso tem alíneas e uma alínea tem itens
- **THEN** o PDF apresenta o inciso, suas alíneas e, após cada alínea, os seus itens, cada um com seu rótulo ("I –", "a)", "1.")

#### Scenario: Artigos dentro de capítulos e seções
- **WHEN** os artigos do documento estão dentro de capítulos e seções
- **THEN** o PDF apresenta o rótulo e o nome do capítulo, depois os da seção e em seguida os artigos com seus dispositivos

## ADDED Requirements

### Requirement: Agrupadores
O PDF SHALL imprimir o rótulo e o nome de cada agrupador de artigos (Parte, Livro, Título, Capítulo, Seção e Subseção) antes do seu conteúdo, cada um em uma linha própria, centralizados, sem recuo de primeira linha e sem espaço adicional antes ou depois. Em Parte, Livro, Título e Capítulo, o rótulo e o nome MUST ser impressos em letras maiúsculas, com o rótulo em negrito e o nome em fonte regular. Em Seção e Subseção, o rótulo e o nome MUST ser impressos em negrito, com a capitalização do documento. O nome MUST ser impresso somente como texto, sem a formatação inline que contiver. Rótulo ou nome vazio MUST NOT gerar linha em branco. O título do agrupador MUST ser mantido na mesma página do conteúdo que o segue.

#### Scenario: Capítulo
- **WHEN** o documento contém o capítulo com rótulo "CAPÍTULO I" e nome "DISPOSIÇÕES PRELIMINARES"
- **THEN** o PDF apresenta, centralizados e em linhas separadas, "CAPÍTULO I" em negrito e "DISPOSIÇÕES PRELIMINARES" em fonte regular, seguidos do primeiro artigo do capítulo

#### Scenario: Nome em minúsculas em Parte, Livro, Título ou Capítulo
- **WHEN** o rótulo ou o nome de um título ou capítulo está no documento com minúsculas ou acentos (ex.: "Das disposições gerais")
- **THEN** o PDF apresenta o texto em maiúsculas, com os acentos preservados (ex.: "DAS DISPOSIÇÕES GERAIS")

#### Scenario: Seção com nome formatado
- **WHEN** o documento contém a seção com rótulo "Seção I" e nome "<b>Dos beneficiários</b>"
- **THEN** o PDF apresenta, centralizados e em linhas separadas, "Seção I" e "Dos beneficiários", ambos em negrito e com a capitalização original

#### Scenario: Subseção
- **WHEN** o documento contém uma subseção
- **THEN** o rótulo e o nome da subseção são apresentados centralizados e em negrito, como os da seção

#### Scenario: Agrupador sem nome
- **WHEN** um agrupador tem rótulo e não tem nome
- **THEN** o PDF apresenta apenas a linha do rótulo, sem linha em branco no lugar do nome

#### Scenario: Sem espaço adicional
- **WHEN** o PDF apresenta um capítulo seguido de uma seção e de um artigo
- **THEN** a distância entre as linhas do capítulo, da seção e do artigo é a mesma entrelinha usada entre dispositivos
