# conversor-lexml-jsonix Specification

## Purpose

Converter o texto LexML das proposições entre XML e JSON (Jsonix) pelo executável `jsonix-lexml`, para os hosts que consomem a biblioteca (por exemplo, o texto da proposição na emenda e o `parser/jsonix` do lexeditweb), com falhas e tempo limite tratados como erro e as propriedades de configuração `lexml-jsonix.*`.

## Requirements

### Requirement: Conversão pelo executável jsonix-lexml
O bean `ConversorLexmlJsonix` SHALL converter o texto LexML de XML para JSON (Jsonix) por `xmlToJson` e de JSON para XML por `jsonToXml`, executando o `jsonix-lexml` informado em `lexml-jsonix.cli` com os subcomandos `tojson` e `toxml`, entrada e saída em arquivos de um diretório temporário próprio da conversão. O diretório SHALL ser removido ao final, com sucesso ou falha.

#### Scenario: Conversão bem-sucedida
- **WHEN** o executável termina com código 0 e grava uma saída não vazia
- **THEN** o conversor devolve o conteúdo da saída e remove o diretório temporário

### Requirement: Falha de conversão
O conversor SHALL lançar exceção, e MUST NOT devolver texto vazio, quando o executável terminar com código diferente de 0, terminar sem gravar a saída ou gravar a saída vazia. A mensagem SHALL identificar a direção da conversão e incluir o conteúdo do stderr do executável, limitado em tamanho.

#### Scenario: Erro informado só no stderr
- **WHEN** o executável termina com código 0, sem saída, e escreve a mensagem de erro no stderr
- **THEN** o conversor lança exceção cuja mensagem contém a mensagem do stderr

#### Scenario: Código de saída de erro
- **WHEN** o executável termina com código diferente de 0
- **THEN** o conversor lança exceção com o código e a mensagem do stderr

#### Scenario: Saída vazia
- **WHEN** o executável grava uma saída vazia
- **THEN** o conversor lança exceção, e não devolve texto vazio

### Requirement: Tempo limite
O conversor SHALL encerrar o executável e lançar exceção quando a conversão passar do tempo limite configurado em `lexml-jsonix.timeout`, com padrão de 60 segundos.

#### Scenario: Conversão que não termina
- **WHEN** o executável não termina dentro do tempo limite
- **THEN** o processo é encerrado e o conversor lança exceção que informa o tempo limite

### Requirement: Propriedades obsoletas
As propriedades `lexml-jsonix.usar-entrada-saida-padrao-durante-conversao` e `lexml-jsonix.remover-diretorio-apos-conversao` SHALL continuar aceitas, sem efeito na conversão, que é sempre por arquivo com remoção do diretório. Quando uma delas for informada com valor diferente desse comportamento, o conversor SHALL registrar um aviso no log ao ser criado.

#### Scenario: Configuração antiga por entrada e saída padrão
- **WHEN** a aplicação informa `usar-entrada-saida-padrao-durante-conversao=true`
- **THEN** a aplicação sobe normalmente, a conversão é feita por arquivo e um aviso de propriedade obsoleta é registrado

### Requirement: Executável inexistente
Fora do perfil `test`, a criação do conversor SHALL falhar quando o arquivo de `lexml-jsonix.cli` não existir, como hoje.

#### Scenario: Caminho inválido
- **WHEN** `lexml-jsonix.cli` aponta para um arquivo que não existe e o perfil não é `test`
- **THEN** a criação do bean falha com `FileNotFoundException` que nomeia o caminho
