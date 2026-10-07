# Spec Delta

## MODIFIED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo), a articulação e a justificação, nessa ordem. Nesta etapa MUST NOT ser impressos: o local e data e as assinaturas.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo, articulação e justificação é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo, os artigos e a justificação, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe) ou não possui justificação
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio nem título no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento contém local e data (ex.: "Sala das Sessões, 24 de abril de 2026.") e assinaturas
- **THEN** o texto extraído do PDF não contém o local e data nem as assinaturas

### Requirement: Estilos de parágrafo da justificação
Os estilos de parágrafo aplicados no editor e registrados como classes no parágrafo SHALL ser impressos com os valores da justificação da emenda. As classes de alinhamento MUST ser reconhecidas com e sem o prefixo `ql-` (o editor grava sem ele): `align-center`/`ql-align-center`, `align-right`/`ql-align-right` e `align-justify`/`ql-align-justify` MUST alinhar o parágrafo ao centro, à direita e justificado, e o centralizado e o alinhado à direita MUST ficar sem recuo de primeira linha; `ql-text-indent-0px` MUST retirar o recuo de primeira linha; `ql-margin-bottom-0px` MUST retirar o espaço após o parágrafo; `estilo-ementa` MUST imprimir o parágrafo com recuo à esquerda de 6,5cm e sem recuo de primeira linha; `estilo-norma-alterada` MUST imprimir o parágrafo com recuo à esquerda de 3cm e recuo de primeira linha de 1,5cm. Classes desconhecidas MUST ser ignoradas.

#### Scenario: Parágrafo centralizado
- **WHEN** um parágrafo da justificação tem a classe `align-center` (ou `ql-align-center`)
- **THEN** o parágrafo é impresso centralizado e sem recuo de primeira linha

#### Scenario: Parágrafo sem recuo e sem espaço depois
- **WHEN** um parágrafo tem as classes `ql-text-indent-0px ql-margin-bottom-0px`
- **THEN** o parágrafo começa na margem esquerda e o parágrafo seguinte vem logo abaixo, sem espaço adicional

#### Scenario: Estilo ementa
- **WHEN** um parágrafo tem a classe `estilo-ementa`
- **THEN** o parágrafo é impresso a 6,5cm da margem esquerda, sem recuo de primeira linha

#### Scenario: Estilo norma alterada
- **WHEN** um parágrafo tem a classe `estilo-norma-alterada`
- **THEN** o parágrafo é impresso a 3cm da margem esquerda, com recuo de primeira linha de 1,5cm

## ADDED Requirements

### Requirement: Listas da justificação
As listas da justificação SHALL ser impressas com a formatação de listas da emenda: recuadas 2,5cm em relação ao bloco em que estão, sem recuo de primeira linha, com o espaço após a lista dos parâmetros de impressão. Cada item de lista numerada (`ol`) MUST ter o rótulo "N." com a sua posição na lista, alinhado à direita; cada item de lista com marcadores (`ul`) MUST ter o rótulo "•". Uma lista dentro de um item MUST ser impressa aninhada, recuada em relação ao item, com numeração própria. A classe de recuo do editor no item (`indent-N`) MUST ser ignorada. A formatação inline, as remissões e as notas de rodapé do texto do item MUST ser preservadas.

#### Scenario: Lista numerada
- **WHEN** a justificação contém `<ol><li>Primeira etapa.</li><li>Segunda etapa.</li></ol>`
- **THEN** o PDF apresenta "1. Primeira etapa." e "2. Segunda etapa." em linhas próprias, recuados em relação ao texto

#### Scenario: Lista com marcadores
- **WHEN** a justificação contém `<ul><li>Órgãos da administração direta.</li></ul>`
- **THEN** o PDF apresenta "• Órgãos da administração direta."

#### Scenario: Lista aninhada
- **WHEN** o segundo item de uma lista numerada contém outra lista numerada com dois itens
- **THEN** os itens da lista interna são impressos numerados "1." e "2.", mais recuados que os itens da lista externa, e a lista externa continua com o item seguinte numerado "3." quando houver

#### Scenario: Item com recuo do editor
- **WHEN** um item tem a classe `indent-1`
- **THEN** o item é impresso no mesmo nível dos demais itens da lista

### Requirement: Tabelas da justificação
As tabelas da justificação SHALL ser impressas com a formatação de tabelas da emenda: centralizadas, na largura indicada pelo atributo `width` da tabela, em porcentagem da largura do texto, ou na largura total quando não houver; com borda externa, exceto quando o atributo `border` for 0, e bordas nas células quando `border` for informado e diferente de 0; com colunas de mesma largura; com o texto das células em fonte menor que a do texto da justificação e sem recuo de primeira linha. Células de cabeçalho (`th`) MUST ser impressas em negrito. A mesclagem de células (`colspan` e `rowspan`) MUST ser respeitada. A formatação inline, as remissões e as notas de rodapé do texto das células MUST ser preservadas.

#### Scenario: Tabela simples
- **WHEN** a justificação contém uma tabela com as linhas "Indicador | Meta" e "Processos digitalizados | 80%"
- **THEN** o PDF apresenta a tabela com duas colunas de mesma largura, com o texto de cada célula na sua posição, centralizada e com borda externa

#### Scenario: Largura da tabela
- **WHEN** a tabela tem o atributo `width="60"`
- **THEN** a tabela ocupa 60% da largura do texto e fica centralizada

#### Scenario: Cabeçalho e células mescladas
- **WHEN** a primeira linha da tabela é de células `th` e uma célula da tabela tem `colspan="2"`
- **THEN** as células da primeira linha são impressas em negrito e a célula mesclada ocupa as duas colunas

### Requirement: Imagens da justificação
As imagens da justificação (`img`) cujo `src` é uma imagem embutida (data URI `data:image/...`) SHALL ser impressas em bloco próprio, na largura indicada pelo atributo `width`, em porcentagem da largura do texto, ou na largura total quando não houver, mantendo a proporção. Um parágrafo cujo conteúdo começa com uma imagem MUST ser impresso centralizado e sem recuo de primeira linha. Uma imagem cujo `src` não é uma imagem embutida MUST NOT ser carregada nem impressa. Uma imagem que não puder ser lida MUST NOT impedir a geração do PDF.

#### Scenario: Imagem com largura
- **WHEN** um parágrafo da justificação contém apenas `<img src="data:image/png;base64,..." width="40"/>`
- **THEN** o PDF apresenta a imagem centralizada, com 40% da largura do texto

#### Scenario: Imagem sem largura
- **WHEN** a imagem não tem o atributo `width`
- **THEN** a imagem é impressa na largura total do texto, mantendo a proporção

#### Scenario: Imagem no meio do texto
- **WHEN** um parágrafo contém "Logotipo do Programa:" seguido de uma imagem
- **THEN** o PDF apresenta o texto e, abaixo dele, a imagem em bloco próprio

#### Scenario: Imagem externa
- **WHEN** a imagem tem `src="https://exemplo.gov.br/logo.png"`
- **THEN** a imagem não é carregada nem impressa, e o PDF é gerado com o restante do texto
