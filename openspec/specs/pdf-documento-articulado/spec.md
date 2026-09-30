# pdf-documento-articulado Specification

## Purpose

Permite salvar a proposição editada no ETA como um PDF/A que carrega o texto em LexML (`documento-articulado.xml`) embutido e reabri-la a partir desse PDF, recuperando o `documento-articulado.json`, com a conversão JSON <-> XML feita pelo executável `jsonix-lexml`.

## Requirements

### Requirement: Gerar PDF com o documento-articulado.xml embutido
O sistema SHALL receber o conteúdo de um `documento-articulado.json` (documento LexML em formato jsonix) e produzir um PDF/A-3B que contenha, como arquivo anexo de nome `documento-articulado.xml`, tipo `text/xml` e relação `Source`, o LexML XML equivalente obtido pela conversão JSON -> XML. O conteúdo do anexo MUST ser exatamente o XML devolvido pela conversão, codificado em UTF-8.

#### Scenario: Documento válido gera PDF com o anexo
- **WHEN** a geração recebe um `documento-articulado.json` válido
- **THEN** é produzido um PDF/A-3B com um único anexo de modelo, chamado `documento-articulado.xml`, cujo conteúdo é o XML resultante da conversão do JSON recebido

#### Scenario: Acentuação preservada no anexo
- **WHEN** o documento contém textos com caracteres acentuados (ex.: "Sala das Sessões", "Fica instituído")
- **THEN** o `documento-articulado.xml` embutido contém esses caracteres corretamente codificados em UTF-8

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

### Requirement: Rejeitar documento inválido na geração
A geração SHALL rejeitar, com `EtaBackendException` e sem escrever bytes no destino, uma entrada que não seja JSON válido ou que não seja um documento LexML (`name.localPart` = `LexML` no namespace `http://www.lexml.gov.br/1.0`, com `value.metadado.identificacao.urn`).

#### Scenario: Conteúdo que não é JSON
- **WHEN** a geração recebe um texto que não é JSON válido
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino

#### Scenario: JSON que não é documento LexML
- **WHEN** a geração recebe um JSON sem `name.localPart` igual a `LexML` ou sem URN de identificação
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino

#### Scenario: Falha na conversão para XML
- **WHEN** a conversão JSON -> XML falha
- **THEN** é lançada `EtaBackendException` com a mensagem de erro da conversão e nada é escrito no destino

### Requirement: Recuperar o JSON a partir do PDF
O sistema SHALL receber um PDF, localizar o anexo `documento-articulado.xml` e devolver o `documento-articulado.json` obtido pela conversão XML -> JSON desse anexo. O JSON devolvido MUST ser semanticamente igual ao JSON usado na geração (mesmos valores; a ordem das chaves pode diferir).

#### Scenario: Ida e volta preserva o documento
- **WHEN** um `documento-articulado.json` é convertido em PDF e o JSON é recuperado desse PDF
- **THEN** o JSON recuperado é semanticamente igual ao original, incluindo os metadados `lexedit:Metadado`

#### Scenario: PDF sem o anexo
- **WHEN** a recuperação recebe um PDF válido que não contém o anexo `documento-articulado.xml` (ex.: um PDF de emenda ou de parecer)
- **THEN** é lançada `EtaBackendException` informando que o arquivo não foi gerado pelo editor de proposições

#### Scenario: Arquivo que não é PDF
- **WHEN** a recuperação recebe bytes que não formam um PDF
- **THEN** é lançada `EtaBackendException` informando que o arquivo não foi gerado pelo editor de proposições

#### Scenario: Sem resíduo de arquivos temporários
- **WHEN** a recuperação termina, com sucesso ou com erro
- **THEN** nenhum arquivo temporário criado por ela permanece no sistema de arquivos

### Requirement: Conversão JSON <-> XML pelo executável jsonix-lexml
A conversão SHALL ser feita pelo executável `jsonix-lexml` informado pela aplicação consumidora, nos comandos `toxml` (JSON -> XML) e `tojson` (XML -> JSON), trocando dados com o executável em UTF-8. Como o executável pode encerrar com código 0 mesmo em erro, a conversão MUST considerar falha qualquer execução que termine com código diferente de zero, que não produza saída ou que produza saída vazia, e MUST lançar `EtaBackendException` contendo a mensagem de erro emitida pelo executável. A conversão MUST ser interrompida e falhar quando exceder o tempo limite configurado, e MUST remover os arquivos temporários que criar.

#### Scenario: Executável inexistente
- **WHEN** a conversão é executada com um caminho de executável que não existe
- **THEN** é lançada `EtaBackendException` identificando o executável

#### Scenario: Entrada rejeitada pelo executável
- **WHEN** o executável não produz saída e escreve um erro (ex.: "Element ... is not known in this context")
- **THEN** é lançada `EtaBackendException` cuja mensagem contém o erro emitido pelo executável

#### Scenario: Tempo limite excedido
- **WHEN** o executável não termina dentro do tempo limite configurado
- **THEN** o processo é encerrado e é lançada `EtaBackendException` informando o tempo limite

#### Scenario: Caminho com espaços
- **WHEN** o caminho do executável ou do diretório temporário contém espaços
- **THEN** a conversão é executada normalmente
