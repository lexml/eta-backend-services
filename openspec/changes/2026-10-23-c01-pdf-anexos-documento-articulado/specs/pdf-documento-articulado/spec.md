# Spec Delta

## ADDED Requirements

### Requirement: Anexos informados separadamente do documento
A geração SHALL aceitar, além do `documento-articulado.json`, uma lista de anexos (`AnexoPDFA`: nome do arquivo, conteúdo e indicação de impressão) informada separadamente do JSON. Itens nulos ou sem conteúdo MUST ser ignorados. A geração sem a lista, ou com lista vazia, MUST produzir o mesmo PDF que a geração anterior à existência dos anexos.

#### Scenario: Geração sem anexos
- **WHEN** a geração é chamada sem a lista de anexos, ou com lista vazia ou só com itens nulos
- **THEN** o PDF tem as mesmas páginas e o mesmo único anexo `documento-articulado.xml` da geração sem anexos

#### Scenario: Geração com anexos
- **WHEN** a geração recebe o JSON e uma lista com anexos válidos
- **THEN** o PDF é produzido a partir do JSON e os anexos são tratados conforme os requisitos de embutir e de concatenar

### Requirement: Anexos embutidos no PDF/A
Todos os anexos informados SHALL ser embutidos no PDF/A-3B como arquivos anexos com relação `Data`, com o nome informado (`anexo.pdf` se ausente) e tipo conforme a extensão (`application/pdf` para `.pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document` para `.docx`, `application/octet-stream` nos demais casos), independentemente da indicação de impressão. O anexo `documento-articulado.xml` do modelo MUST permanecer presente, com o mesmo conteúdo.

#### Scenario: Anexo não impresso fica embutido
- **WHEN** a geração recebe um anexo com `imprimir = false`
- **THEN** o PDF contém esse arquivo como anexo com o conteúdo original e suas páginas não aparecem no PDF

#### Scenario: Anexo impresso também fica embutido
- **WHEN** a geração recebe um anexo PDF com `imprimir = true`
- **THEN** o PDF contém esse arquivo como anexo com o conteúdo original e, além disso, suas páginas ao final do documento

#### Scenario: Modelo preservado
- **WHEN** a geração recebe anexos
- **THEN** o PDF continua contendo o `documento-articulado.xml` com o XML da conversão do JSON, e os metadados XMP de PDF/A continuam presentes

### Requirement: Páginas dos anexos concatenadas ao final
As páginas dos anexos com `imprimir = true` SHALL ser concatenadas, na ordem em que foram recebidos, ao final do PDF do documento articulado. Anexos com `imprimir = false` MUST NOT acrescentar páginas.

#### Scenario: Um anexo impresso
- **WHEN** o documento gera 3 páginas e é recebido um anexo PDF de 2 páginas com `imprimir = true`
- **THEN** o PDF final tem 5 páginas e as 2 últimas são as do anexo

#### Scenario: Vários anexos, só alguns impressos
- **WHEN** são recebidos, nesta ordem, o anexo A (`imprimir = true`, 1 página), o B (`imprimir = false`, 4 páginas) e o C (`imprimir = true`, 2 páginas)
- **THEN** as páginas do PDF final são as do documento seguidas pela do anexo A e pelas do anexo C, sem as do B

### Requirement: Hash de verificação sobre o PDF com anexos
O hash de verificação MD5 do XMP SHALL ser calculado sobre os bytes do PDF final, depois da concatenação das páginas dos anexos, e substituir o valor de placeholder.

#### Scenario: Hash preenchido com anexos impressos
- **WHEN** o PDF é gerado com anexos com `imprimir = true`
- **THEN** o XMP não contém o valor de placeholder `00000000000000000000000000000000` e o hash gravado é o MD5 do arquivo final

### Requirement: Nome de anexo reservado
A geração SHALL rejeitar, com `EtaBackendException` e sem escrever bytes no destino, um anexo cujo nome seja `documento-articulado.xml`, para não conflitar com o anexo do modelo usado na recuperação do JSON.

#### Scenario: Anexo com o nome do modelo
- **WHEN** a geração recebe um anexo chamado `documento-articulado.xml`
- **THEN** é lançada `EtaBackendException` e nada é escrito no destino

### Requirement: Falha na geração com anexos
Se algum anexo com `imprimir = true` não puder ser lido como PDF, a geração SHALL lançar `EtaBackendException` com mensagem que identifique o anexo e MUST NOT escrever bytes no destino. Os streams recebidos são lidos uma única vez e a geração não depende da posição deles depois da chamada.

#### Scenario: Anexo impresso que não é PDF
- **WHEN** a geração recebe um anexo com `imprimir = true` cujo conteúdo não forma um PDF
- **THEN** é lançada `EtaBackendException` com o nome do anexo e nada é escrito no destino

#### Scenario: Anexo não PDF só embutido
- **WHEN** a geração recebe um anexo `.docx` com `imprimir = false`
- **THEN** o arquivo é embutido no PDF sem validação do conteúdo e a geração termina com sucesso

### Requirement: Recuperação do JSON com anexos no PDF
A recuperação do JSON SHALL continuar funcionando em um PDF gerado com anexos, devolvendo o mesmo JSON que seria devolvido para o PDF gerado sem anexos.

#### Scenario: Ida e volta com anexos
- **WHEN** um PDF é gerado com anexos impressos e não impressos e o JSON é recuperado desse PDF
- **THEN** o JSON recuperado é semanticamente igual ao original
