# Spec Delta

## REMOVED Requirements

### Requirement: Impressão da parte inicial
**Reason**: A partir da parte 2 da issue #72 a articulação passa a ser impressa, então deixa de valer a proibição de imprimi-la contida neste requisito.
**Migration**: A ordem da parte inicial, a regra para elemento ausente e o que ainda não é impresso passam para o requisito "Conteúdo impresso". A impressão dos dispositivos está nos requisitos "Dispositivos da articulação" e "Formatação dos dispositivos".

## ADDED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo) seguida da articulação, na ordem do documento. Nesta etapa MUST NOT ser impressos: o rótulo e o nome dos agrupadores de artigos (Parte, Livro, Título, Capítulo, Seção, Subseção), os blocos de alteração de norma vigente e os omissis, a pena e o título de dispositivo, a justificação, o local e data e as assinaturas.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo e articulação é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo e os artigos, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe)
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento contém capítulos, um bloco de alteração de norma vigente, pena ou título de dispositivo
- **THEN** o texto extraído do PDF não contém o rótulo do capítulo (ex.: "CAPÍTULO I"), os dispositivos da norma alterada (ex.: "Art. 12-A."), a pena (ex.: "Pena –") nem o título de dispositivo

### Requirement: Dispositivos da articulação
O PDF SHALL imprimir os dispositivos Artigo, Caput, Parágrafo, Inciso, Alínea e Item na ordem do documento, cada um em um bloco próprio iniciado pelo seu rótulo, seguido de um espaço e do texto. O rótulo do artigo MUST ser impresso no mesmo bloco do texto do caput. Os dispositivos subordinados (incisos do caput ou do parágrafo, alíneas, itens) MUST aparecer logo após o texto do dispositivo a que pertencem. Artigos dentro de agrupadores MUST ser impressos.

#### Scenario: Artigo com incisos e parágrafos
- **WHEN** um artigo tem caput com incisos e, depois, parágrafos com incisos
- **THEN** o PDF apresenta o rótulo do artigo com o texto do caput, os incisos do caput, e em seguida cada parágrafo seguido dos seus incisos

#### Scenario: Hierarquia até item
- **WHEN** um inciso tem alíneas e uma alínea tem itens
- **THEN** o PDF apresenta o inciso, suas alíneas e, após cada alínea, os seus itens, cada um com seu rótulo ("I –", "a)", "1.")

#### Scenario: Artigos dentro de capítulos e seções
- **WHEN** os artigos do documento estão dentro de capítulos e seções
- **THEN** o PDF apresenta os artigos com seus dispositivos, sem o rótulo e o nome dos agrupadores

### Requirement: Formatação dos dispositivos
Os dispositivos SHALL ser impressos com a formatação da citação de dispositivos da emenda: texto justificado, recuo de primeira linha de 2,5cm, a entrelinha dos parâmetros de impressão e nenhum espaço adicional entre um dispositivo e o seguinte. O rótulo MUST ser impresso em negrito. No texto, negrito e itálico MUST ser preservados, e referências com link (`span` ou `Remissao` com `xlink:href`) MUST ser impressas como texto simples, sem link. A articulação MUST NOT ser envolvida por aspas.

#### Scenario: Rótulo em negrito
- **WHEN** o PDF de um documento com o artigo "Art. 1º Esta Lei tipifica os crimes ..." é gerado
- **THEN** "Art. 1º" é impresso em negrito e o texto do caput em fonte regular, no mesmo bloco

#### Scenario: Mesmo recuo em todos os níveis
- **WHEN** o PDF apresenta um artigo, um parágrafo, um inciso e uma alínea
- **THEN** todos têm recuo de primeira linha de 2,5cm, e as linhas seguintes começam na margem esquerda

#### Scenario: Referência a norma no texto do dispositivo
- **WHEN** o caput contém "Esta Lei altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, para ..."
- **THEN** o PDF apresenta "Esta Lei altera a Lei nº 9.394, de 20 de dezembro de 1996, para ..." sem link

#### Scenario: Itálico no texto do dispositivo
- **WHEN** o texto de um parágrafo contém um trecho em itálico
- **THEN** o trecho é impresso em itálico
