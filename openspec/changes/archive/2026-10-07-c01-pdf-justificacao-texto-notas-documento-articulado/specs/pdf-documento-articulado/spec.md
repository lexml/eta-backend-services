# Spec Delta

## MODIFIED Requirements

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo), a articulação e a justificação, nessa ordem. Nesta etapa MUST NOT ser impressos: o local e data, as assinaturas e, na justificação, as listas, as tabelas e as imagens.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo, articulação e justificação é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo, os artigos e a justificação, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe) ou não possui justificação
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio nem título no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento contém local e data (ex.: "Sala das Sessões, 24 de abril de 2026.") e assinaturas
- **THEN** o texto extraído do PDF não contém o local e data nem as assinaturas

### Requirement: Remissões e links
As referências com `xlink:href` (`span` ou `Remissao`) no texto da ementa, dos dispositivos, dos títulos de dispositivo, da justificação e das notas de rodapé SHALL ser impressas como links em cinza (`#808080`), destacadas do texto preto, sem sublinhado. A referência cujo `xlink:href` é uma URN (começa com `urn:`) MUST ser um link para `https://normas.leg.br/?urn=` seguido da URN sem codificação. A referência cujo `xlink:href` é o identificador de um dispositivo impresso do próprio documento MUST ser um link interno para esse dispositivo; quando o identificador não corresponde a um dispositivo impresso, a referência MUST ser impressa como texto simples, sem link e sem marca visual. O preâmbulo e o nome dos agrupadores MUST continuar impressos somente como texto.

#### Scenario: Remissão externa a uma lei
- **WHEN** o texto contém "<span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>"
- **THEN** o PDF apresenta "Lei nº 9.394, de 20 de dezembro de 1996" em cinza, com link para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394`

#### Scenario: Remissão externa a um dispositivo de lei
- **WHEN** o texto contém uma referência com `xlink:href="urn:lex:br:federal:lei:1990-07-13;8069!art58"`
- **THEN** o link aponta para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1990-07-13;8069!art58`

#### Scenario: Remissão externa dentro de bloco de alteração
- **WHEN** um dispositivo de um bloco de alteração de norma vigente contém uma referência com URN
- **THEN** a referência é impressa como link para o portal normas.leg.br, como fora do bloco

#### Scenario: Remissão interna válida
- **WHEN** o caput do Art. 2º contém "<Remissao xlink:href="art1">art. 1º</Remissao>" e o documento tem o Art. 1º
- **THEN** "art. 1º" é impresso em cinza, com link interno que leva ao Art. 1º no PDF

#### Scenario: Remissão interna inválida
- **WHEN** o texto contém "<Remissao xlink:href="art4">art. 4º</Remissao>" e o documento não tem o Art. 4º
- **THEN** "art. 4º" é impresso como texto simples, na cor do texto e sem link

#### Scenario: Preâmbulo sem link
- **WHEN** o preâmbulo contém uma referência com `xlink:href`
- **THEN** o PDF apresenta o texto da referência sem link, como o restante do preâmbulo

## ADDED Requirements

### Requirement: Justificação
Quando o documento tiver justificação com texto, o PDF SHALL imprimi-la depois da articulação, precedida do título "JUSTIFICAÇÃO", centralizado, em negrito, no tamanho de fonte de destaque e mantido na mesma página do primeiro parágrafo. Cada parágrafo MUST ser impresso em bloco próprio, alinhado à esquerda, com recuo de primeira linha de 2,5cm, a entrelinha e o espaço após o parágrafo dos parâmetros de impressão. No texto, negrito, itálico, sublinhado, subscrito e sobrescrito MUST ser preservados. Um link (`a`) com `xlink:href` de endereço web MUST ser impresso como link para esse endereço, no mesmo estilo das remissões; um `a` sem endereço MUST ser impresso como texto. Trechos comentados (`span` com identificador) MUST ser impressos como texto normal. Uma justificação sem texto MUST NOT gerar título nem bloco.

#### Scenario: Título e parágrafos
- **WHEN** o documento tem justificação com os parágrafos "Esta proposição aperfeiçoa a política pública." e "Contamos com o apoio dos Pares."
- **THEN** o PDF apresenta, depois do último artigo, "JUSTIFICAÇÃO" centralizado em negrito e, em seguida, os dois parágrafos em blocos próprios, com recuo de primeira linha

#### Scenario: Formatação inline
- **WHEN** um parágrafo da justificação contém "<b>negrito</b> <i>itálico</i> <u>sublinhado</u> H<sub>2</sub>O m<sup>2</sup>"
- **THEN** o PDF apresenta "negrito" em negrito, "itálico" em itálico, "sublinhado" sublinhado, o "2" de H2O rebaixado e o "2" de m2 elevado, com os espaços entre as palavras

#### Scenario: Link com endereço web
- **WHEN** a justificação contém "<a xlink:href="https://www12.senado.leg.br">portal do Senado</a>"
- **THEN** "portal do Senado" é impresso em cinza, com link para `https://www12.senado.leg.br`

#### Scenario: Trecho comentado
- **WHEN** a justificação contém "<span id="_tc1777387565991">nos termos do art. 1º</span>"
- **THEN** o PDF apresenta "nos termos do art. 1º" como texto normal, sem marca do comentário

#### Scenario: Justificação vazia
- **WHEN** a justificação do documento só tem parágrafos sem texto
- **THEN** o PDF não apresenta o título "JUSTIFICAÇÃO"

### Requirement: Estilos de parágrafo da justificação
Os estilos de parágrafo aplicados no editor e registrados como classes no parágrafo SHALL ser impressos com os valores da justificação da emenda: `ql-align-center`, `ql-align-right` e `ql-align-justify` MUST alinhar o parágrafo ao centro, à direita e justificado, e o centralizado e o alinhado à direita MUST ficar sem recuo de primeira linha; `ql-text-indent-0px` MUST retirar o recuo de primeira linha; `ql-margin-bottom-0px` MUST retirar o espaço após o parágrafo; `estilo-ementa` MUST imprimir o parágrafo com recuo à esquerda de 6,5cm e sem recuo de primeira linha; `estilo-norma-alterada` MUST imprimir o parágrafo com recuo à esquerda de 3cm e recuo de primeira linha de 1,5cm. Classes desconhecidas MUST ser ignoradas.

#### Scenario: Parágrafo centralizado
- **WHEN** um parágrafo da justificação tem a classe `ql-align-center`
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

### Requirement: Marcas de revisão na justificação
O PDF SHALL representar a versão revisada da justificação: o conteúdo das exclusões (`del`) MUST NOT ser impresso, e o conteúdo das inclusões (`ins`) MUST ser impresso como o restante do texto, sem formatação que o diferencie.

#### Scenario: Exclusão e inclusão
- **WHEN** a justificação contém "Esta proposição promove a <del id="_rt1">reforma</del><ins id="_rt2">modernização</ins> da gestão pública"
- **THEN** o PDF apresenta "Esta proposição promove a modernização da gestão pública", sem "reforma" e sem sublinhado ou cor em "modernização"

#### Scenario: Nota de rodapé dentro de exclusão
- **WHEN** uma nota de rodapé está dentro de um trecho excluído (`del`)
- **THEN** a nota não é impressa nem numerada

### Requirement: Notas de rodapé
Cada nota de rodapé da justificação (`NotaDeRodape`) SHALL ser impressa com um número sobrescrito na posição em que aparece no texto e com o texto da nota no rodapé da mesma página, precedido do mesmo número, em fonte menor que a do texto e abaixo de um traço separador. A numeração MUST começar em 1 e seguir a ordem das notas impressas no documento. A formatação inline e as remissões do texto da nota MUST ser preservadas como no texto da justificação.

#### Scenario: Nota com referência no texto
- **WHEN** a justificação contém "O conceito é definido em regulamento<NotaDeRodape>Texto <b>formatado</b> da nota de rodapé.</NotaDeRodape>."
- **THEN** o PDF apresenta "O conceito é definido em regulamento" seguido do número "1" sobrescrito e do ponto, e o rodapé da página apresenta "1 Texto formatado da nota de rodapé.", com "formatado" em negrito

#### Scenario: Numeração sequencial
- **WHEN** a justificação contém três notas de rodapé
- **THEN** as notas são numeradas 1, 2 e 3, na ordem em que aparecem no texto

#### Scenario: Notas em páginas diferentes
- **WHEN** a justificação ocupa mais de uma página e tem notas em páginas diferentes
- **THEN** o texto de cada nota é impresso no rodapé da página onde está a sua referência, e a numeração continua de uma página para a outra
