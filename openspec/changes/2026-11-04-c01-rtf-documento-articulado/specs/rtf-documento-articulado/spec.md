# Spec Delta

## Purpose

Permite exportar a proposição editada no ETA como um documento RTF editável (aberto pelo Word e por outros editores), gerado sob demanda a partir do `documento-articulado.json`, com o mesmo conteúdo textual do PDF mas sem garantia de fidelidade visual.

## ADDED Requirements

### Requirement: Gerar RTF do documento articulado sob demanda
O sistema SHALL receber o conteúdo de um `documento-articulado.json` e escrever no destino informado um documento RTF válido com o texto da proposição. A geração MUST ser independente da geração do PDF: o RTF não é embutido no PDF e o PDF não muda por causa dele.

#### Scenario: Documento válido gera RTF
- **WHEN** a geração recebe um `documento-articulado.json` válido
- **THEN** é escrito no destino um documento RTF que começa com o cabeçalho `{\rtf` e cujas chaves estão balanceadas

#### Scenario: Acentuação preservada
- **WHEN** o documento contém textos com caracteres acentuados (ex.: "Sala das Sessões", "Fica instituído")
- **THEN** o RTF, ao ser lido como RTF, apresenta esses caracteres corretamente

#### Scenario: PDF não é afetado
- **WHEN** um mesmo documento é convertido em PDF antes e depois da introdução da geração de RTF
- **THEN** o PDF gerado tem o mesmo conteúdo e os mesmos anexos

### Requirement: Conteúdo impresso no RTF
O RTF SHALL conter, na mesma ordem do PDF, a parte inicial (epígrafe, ementa e preâmbulo), a articulação com os rótulos e textos dos dispositivos, a justificação, o local e a data e as assinaturas, quando existirem no documento. As pendências registradas MUST constar do RTF antes da epígrafe, como no PDF. O texto de revisões marcadas como excluídas na justificação MUST NOT ser impresso.

#### Scenario: Parte inicial e articulação
- **WHEN** o documento tem epígrafe, ementa, preâmbulo e artigos com parágrafos e incisos
- **THEN** o texto do RTF contém a epígrafe, a ementa, o preâmbulo e, depois deles, cada dispositivo com seu rótulo (ex.: "Art. 1º", "§ 1º", "I –") e seu texto, nessa ordem

#### Scenario: Justificação, local e assinaturas
- **WHEN** o documento tem justificação, local, data e autores parlamentares
- **THEN** o texto do RTF contém a justificação, o local com a data por extenso e o nome de cada parlamentar, depois da articulação

#### Scenario: Pendências
- **WHEN** o documento registra pendências
- **THEN** o texto do RTF contém cada pendência, antes da epígrafe

#### Scenario: Texto excluído na justificação
- **WHEN** a justificação tem um trecho marcado como excluído em revisão
- **THEN** o texto desse trecho não aparece no RTF

### Requirement: Anexo de parecer sem justificação no RTF
Para um documento marcado como anexo de parecer, o RTF SHALL omitir a justificação e manter os demais conteúdos, como no PDF.

#### Scenario: Anexo de parecer
- **WHEN** o documento está marcado como anexo de parecer e tem justificação, local, data e assinaturas
- **THEN** o texto do RTF não contém a justificação e contém o local, a data e as assinaturas

### Requirement: RTF sem modelo embutido e sem anexos
O RTF SHALL NOT embutir o `documento-articulado.xml` nem anexos, e o sistema MUST NOT oferecer a recuperação do `documento-articulado.json` a partir de um RTF.

#### Scenario: Somente o texto
- **WHEN** o RTF é gerado
- **THEN** o resultado não contém o modelo `documento-articulado.xml` nem arquivos anexos

### Requirement: Rejeitar documento inválido na geração do RTF
A geração do RTF SHALL rejeitar, com `EtaBackendException` e sem escrever bytes no destino, uma entrada que não seja JSON válido ou que não seja um documento LexML, com as mesmas condições da geração do PDF.

#### Scenario: Conteúdo que não é JSON
- **WHEN** a geração recebe um texto que não é JSON válido
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino

#### Scenario: JSON que não é documento LexML
- **WHEN** a geração recebe um JSON sem `name.localPart` igual a `LexML` ou sem URN de identificação
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino

#### Scenario: Falha na conversão para XML
- **WHEN** a conversão JSON -> XML falha
- **THEN** é lançada `EtaBackendException` com a mensagem de erro da conversão e nada é escrito no destino

### Requirement: Falha na renderização do RTF
Se a renderização do RTF falhar, o sistema SHALL lançar `EtaBackendException` com a causa e MUST NOT escrever bytes parciais no destino.

#### Scenario: Erro de renderização
- **WHEN** a renderização do RTF falha depois da conversão do documento
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino
