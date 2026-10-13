# Spec Delta

## ADDED Requirements

### Requirement: Cabeçalho
A primeira página do PDF SHALL ter o cabeçalho conforme as opções de impressão do documento (`lexedit:OpcoesImpressao`). Quando `imprimirBrasao` for verdadeiro, o cabeçalho MUST apresentar o Brasão da República centralizado e, abaixo dele, o nome da casa legislativa obtido da autoridade da URN de identificação: "SENADO FEDERAL" para `senado.federal`, "CÂMARA DOS DEPUTADOS" para `camara.deputados` e "CONGRESSO NACIONAL" para `congresso.nacional`; para outra autoridade, só o brasão. Quando `textoCabecalho` não for vazio, o cabeçalho MUST apresentar esse texto centralizado, abaixo do brasão quando houver. Quando o brasão não for impresso e o texto for vazio, o PDF MUST NOT ter cabeçalho. As demais páginas MUST NOT ter cabeçalho. Opção ausente MUST valer o padrão do editor: `imprimirBrasao` verdadeiro e `textoCabecalho` vazio.

#### Scenario: Brasão e casa legislativa
- **WHEN** o documento tem `imprimirBrasao="true"` e a URN `urn:lex:br:senado.federal:projeto.lei:2026;123`
- **THEN** a primeira página apresenta, acima da epígrafe, o Brasão da República centralizado e o texto "SENADO FEDERAL" abaixo dele

#### Scenario: Casa a partir da URN
- **WHEN** a URN do documento tem a autoridade `camara.deputados`
- **THEN** o nome da casa no cabeçalho é "CÂMARA DOS DEPUTADOS"

#### Scenario: Autoridade sem casa legislativa
- **WHEN** a URN do documento tem a autoridade `federal` (ex.: medida provisória)
- **THEN** o cabeçalho apresenta o brasão sem nome de casa

#### Scenario: Texto do cabeçalho
- **WHEN** o documento tem `textoCabecalho="Gabinete do Senador Davi Alcolumbre"`
- **THEN** o cabeçalho apresenta esse texto centralizado, abaixo do brasão e do nome da casa

#### Scenario: Só o texto, sem brasão
- **WHEN** o documento tem `imprimirBrasao="false"` e um `textoCabecalho` não vazio
- **THEN** o cabeçalho apresenta só o texto, sem brasão e sem nome de casa

#### Scenario: Sem cabeçalho
- **WHEN** o documento tem `imprimirBrasao="false"` e `textoCabecalho` vazio
- **THEN** o PDF não tem cabeçalho e o conteúdo da primeira página começa na mesma posição de hoje

#### Scenario: Opções ausentes
- **WHEN** o documento não tem `lexedit:OpcoesImpressao`
- **THEN** o cabeçalho é impresso com o brasão e o nome da casa, sem texto

#### Scenario: Só na primeira página
- **WHEN** o documento tem mais de uma página e o cabeçalho é impresso
- **THEN** o brasão aparece apenas na primeira página
