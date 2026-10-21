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

### Requirement: Conteúdo impresso
O PDF SHALL imprimir, a partir do `documento-articulado.xml` gerado para o documento, a parte inicial (epígrafe, ementa e preâmbulo), a articulação e a justificação, nessa ordem, seguidas do local e data e das assinaturas, montados a partir dos dados estruturados do documento (`lexedit:Metadado`). A representação textual do local, data e assinaturas no LexML (`ParteFinal`) MUST NOT ser impressa, para não duplicar esse conteúdo.

#### Scenario: Ordem do conteúdo
- **WHEN** um documento com epígrafe, ementa, preâmbulo, articulação, justificação, local e data e autoria de parlamentares é convertido em PDF
- **THEN** o texto extraído do PDF contém a epígrafe, a ementa, o preâmbulo, os artigos, a justificação, o local e data e as assinaturas, nessa ordem

#### Scenario: Elemento ausente
- **WHEN** o documento não possui um dos elementos da parte inicial (ex.: sem epígrafe) ou não possui justificação
- **THEN** o PDF é gerado com os demais elementos, sem bloco vazio nem título no lugar do ausente

#### Scenario: Elementos ainda não impressos
- **WHEN** o documento tem local, data e autoria de parlamentares e também a representação textual deles no LexML (`ParteFinal`)
- **THEN** a representação textual não é impressa: o local e data e cada assinatura aparecem uma única vez no texto extraído do PDF, montados a partir dos dados estruturados

### Requirement: Dispositivos da articulação
O PDF SHALL imprimir os dispositivos Artigo, Caput, Parágrafo, Inciso, Alínea, Item e Pena na ordem do documento, cada um em um bloco próprio iniciado pelo seu rótulo, seguido de um espaço e do texto. O rótulo do artigo MUST ser impresso no mesmo bloco do texto do caput. Os dispositivos subordinados (incisos do caput ou do parágrafo, alíneas, itens, pena) MUST aparecer logo após o texto do dispositivo a que pertencem. Artigos dentro de agrupadores MUST ser impressos logo após o rótulo e o nome do agrupador.

#### Scenario: Artigo com incisos e parágrafos
- **WHEN** um artigo tem caput com incisos e, depois, parágrafos com incisos
- **THEN** o PDF apresenta o rótulo do artigo com o texto do caput, os incisos do caput, e em seguida cada parágrafo seguido dos seus incisos

#### Scenario: Hierarquia até item
- **WHEN** um inciso tem alíneas e uma alínea tem itens
- **THEN** o PDF apresenta o inciso, suas alíneas e, após cada alínea, os seus itens, cada um com seu rótulo ("I –", "a)", "1.")

#### Scenario: Artigos dentro de capítulos e seções
- **WHEN** os artigos do documento estão dentro de capítulos e seções
- **THEN** o PDF apresenta o rótulo e o nome do capítulo, depois os da seção e em seguida os artigos com seus dispositivos

### Requirement: Formatação dos dispositivos
Os dispositivos SHALL ser impressos com texto justificado, recuo de primeira linha de 2,5cm, a entrelinha dos parâmetros de impressão e nenhum espaço adicional entre um dispositivo e o seguinte. Depois do último dispositivo, a articulação MUST terminar com o espaço após o parágrafo dos parâmetros de impressão (0.6em; 0.45em com "reduzir espaço entre linhas"), antes do que vier a seguir (justificação ou local e data). O rótulo MUST ser impresso em negrito, exceto o rótulo da pena, que MUST ser impresso em fonte regular. O rótulo MUST ser tratado como uma palavra só na justificação: os espaços internos dele (ex.: entre "Art." e "15.", entre "§" e "1º") MUST NOT ser esticados, ficando com a mesma largura em todas as linhas; o espaço entre o rótulo e o texto continua ajustável como os demais espaços da linha. No texto, negrito e itálico MUST ser preservados, e as referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A articulação MUST NOT ser envolvida por aspas.

#### Scenario: Rótulo em negrito
- **WHEN** o PDF de um documento com o artigo "Art. 1º Esta Lei tipifica os crimes ..." é gerado
- **THEN** "Art. 1º" é impresso em negrito e o texto do caput em fonte regular, no mesmo bloco

#### Scenario: Rótulo como uma palavra só
- **WHEN** o PDF apresenta dois artigos, "Art. 15." e "Art. 16.", cujas primeiras linhas são justificadas com folgas diferentes
- **THEN** a distância entre "Art." e o número é a mesma nos dois rótulos, e só os espaços fora do rótulo absorvem a folga da linha

#### Scenario: Mesmo recuo em todos os níveis
- **WHEN** o PDF apresenta um artigo, um parágrafo, um inciso e uma alínea
- **THEN** todos têm recuo de primeira linha de 2,5cm, e as linhas seguintes começam na margem esquerda

#### Scenario: Referência a norma no texto do dispositivo
- **WHEN** o caput contém "Esta Lei altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, para ..."
- **THEN** o PDF apresenta "Esta Lei altera a Lei nº 9.394, de 20 de dezembro de 1996, para ..." com "Lei nº 9.394, de 20 de dezembro de 1996" como link para o portal normas.leg.br

#### Scenario: Itálico no texto do dispositivo
- **WHEN** o texto de um parágrafo contém um trecho em itálico
- **THEN** o trecho é impresso em itálico

#### Scenario: Espaço depois da articulação
- **WHEN** um documento em fonte 14, sem "reduzir espaço entre linhas", tem justificação depois da articulação
- **THEN** a distância entre a linha de base do último dispositivo e a do título "JUSTIFICAÇÃO" é de 55pt (8,4pt do espaço após a articulação somados aos 26pt antes do título e às entrelinhas), e entre dois dispositivos continua sem espaço adicional

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

### Requirement: Pena e título de dispositivo
A pena SHALL ser impressa na posição em que aparece no documento, com a formatação dos dispositivos e o rótulo ("Pena –") em fonte regular. O título de dispositivo SHALL ser impresso antes do dispositivo a que pertence, em bloco próprio com a formatação dos dispositivos, sem rótulo e com todo o texto em negrito, e MUST ser mantido na mesma página do dispositivo que o segue. Pena ou título de dispositivo vazio MUST NOT gerar bloco.

#### Scenario: Pena no fim do caput
- **WHEN** o caput do Art. 2º termina com a pena "Pena – reclusão, de 4 (quatro) a 12 (doze) anos, e multa."
- **THEN** o PDF apresenta essa pena logo após o texto do caput e antes do § 1º, com "Pena –" em fonte regular e o mesmo recuo de primeira linha dos dispositivos

#### Scenario: Pena no fim de um parágrafo
- **WHEN** um parágrafo termina com uma pena
- **THEN** o PDF apresenta a pena logo após o texto e os incisos desse parágrafo

#### Scenario: Título de dispositivo antes do artigo
- **WHEN** o Art. 2º tem o título de dispositivo "Desvio ou apropriação de recursos e insumos da saúde"
- **THEN** o PDF apresenta esse título, todo em negrito, sem rótulo e com o recuo de primeira linha dos dispositivos, na linha imediatamente anterior ao "Art. 2º"

#### Scenario: Título de dispositivo de outro tipo de dispositivo
- **WHEN** um parágrafo tem título de dispositivo
- **THEN** o título é apresentado em negrito na linha imediatamente anterior ao parágrafo

#### Scenario: Título não fica sozinho no fim da página
- **WHEN** o título de dispositivo cairia na última linha de uma página
- **THEN** o título é apresentado na página seguinte, junto com o dispositivo a que pertence

### Requirement: Alteração de norma vigente
O bloco de alteração de norma vigente SHALL ser impresso logo após o dispositivo que o introduz, com margem esquerda de 3cm e recuo de primeira linha de 1,5cm, sem espaço adicional antes ou depois. Os dispositivos do bloco MUST ser impressos com a mesma formatação dos dispositivos da articulação. As aspas e a nota de alteração MUST ser impressas somente conforme os atributos do documento: aspas de abertura (“) antes do rótulo do dispositivo com `abreAspas="s"`; aspas de fechamento (”) depois do texto, ou da linha pontilhada, do dispositivo com `fechaAspas="s"`; e, quando houver `notaAlteracao`, um espaço e a nota entre parênteses depois das aspas (ex.: `” (NR)`). As aspas MUST NOT ser impressas em negrito. Nada MUST ser impresso depois da nota de alteração. O omissis MUST ser impresso como uma linha pontilhada até a margem direita. O dispositivo com `textoOmitido="s"` MUST ser impresso com o rótulo seguido de uma linha pontilhada, no lugar do texto.

#### Scenario: Bloco de alteração com aspas e nota
- **WHEN** o Art. 2º altera a Lei nº 9.394 com um bloco cujo artigo "Art. 12-A." tem `abreAspas="s"` e cujo último parágrafo tem `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta, logo após o caput do Art. 2º e com o recuo do bloco de alteração, "“Art. 12-A. ..." e, no fim do último parágrafo, "...” (NR)", sem nenhum caractere depois do "(NR)"

#### Scenario: Dispositivo com texto omitido e omissis
- **WHEN** o bloco altera o art. 327 com o caput com `textoOmitido="s"`, um omissis e o § 3º com `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta "“Art. 327." seguido de linha pontilhada, uma linha pontilhada inteira e "§ 3º A pena será aumentada da metade ... pública.” (NR)"

#### Scenario: Aspas de fechamento no omissis
- **WHEN** o último elemento do bloco é um omissis com `fechaAspas="s"` e `notaAlteracao="NR"`
- **THEN** o PDF apresenta a linha pontilhada seguida de "” (NR)" no fim da mesma linha

#### Scenario: Omissis dentro de um dispositivo
- **WHEN** um inciso com `textoOmitido="s"` contém um omissis seguido das alíneas "k)" e "l)"
- **THEN** o PDF apresenta o rótulo do inciso seguido de linha pontilhada, uma linha pontilhada inteira e em seguida as alíneas "k)" e "l)" com seus textos

#### Scenario: Sem aspas fora dos atributos
- **WHEN** um dispositivo dentro do bloco de alteração não tem `abreAspas` nem `fechaAspas`
- **THEN** o PDF não apresenta aspas nesse dispositivo

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
Os estilos de parágrafo aplicados no editor e registrados como classes no parágrafo SHALL ser impressos conforme as classes. As classes de alinhamento MUST ser reconhecidas com e sem o prefixo `ql-` (o editor grava sem ele): `align-center`/`ql-align-center`, `align-right`/`ql-align-right` e `align-justify`/`ql-align-justify` MUST alinhar o parágrafo ao centro, à direita e justificado, e o centralizado e o alinhado à direita MUST ficar sem recuo de primeira linha; `ql-text-indent-0px` MUST retirar o recuo de primeira linha; `ql-margin-bottom-0px` MUST retirar o espaço após o parágrafo; `estilo-ementa` MUST imprimir o parágrafo com recuo à esquerda de 6,5cm e sem recuo de primeira linha; `estilo-norma-alterada` MUST imprimir o parágrafo com recuo à esquerda de 3cm e recuo de primeira linha de 1,5cm. Classes desconhecidas MUST ser ignoradas.

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

### Requirement: Listas da justificação
As listas da justificação SHALL ser impressas recuadas 2,5cm em relação ao bloco em que estão, sem recuo de primeira linha, com o espaço após a lista dos parâmetros de impressão. Cada item de lista numerada (`ol`) MUST ter o rótulo "N." com a sua posição na lista, alinhado à direita; cada item de lista com marcadores (`ul`) MUST ter o rótulo "•". Uma lista dentro de um item MUST ser impressa aninhada, recuada em relação ao item, com numeração própria. A classe de recuo do editor no item (`indent-N`) MUST ser ignorada. A formatação inline, as remissões e as notas de rodapé do texto do item MUST ser preservadas.

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
As tabelas da justificação SHALL ser impressas centralizadas, na largura indicada pelo atributo `width` da tabela, em porcentagem da largura do texto, ou na largura total quando não houver; com borda externa, exceto quando o atributo `border` for 0, e bordas nas células quando `border` for informado e diferente de 0; com colunas de mesma largura; com o texto das células em fonte menor que a do texto da justificação e sem recuo de primeira linha. Células de cabeçalho (`th`) MUST ser impressas em negrito. A mesclagem de células (`colspan` e `rowspan`) MUST ser respeitada. A formatação inline, as remissões e as notas de rodapé do texto das células MUST ser preservadas.

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

### Requirement: Local e data
Quando o documento tiver `local` preenchido, o PDF SHALL imprimir, depois da justificação (ou da articulação, se não houver justificação), o fecho "`<local>`, `<data>`." com recuo de primeira linha de 2,5cm, mantido na mesma página das assinaturas, com 34pt de espaço antes (26pt com "reduzir espaço entre linhas"). A data MUST sair por extenso em português (ex.: "24 de abril de 2026"). Quando a data estiver vazia, ausente ou inválida, o fecho MUST ser apenas o local seguido de vírgula ("`<local>`,"), sem espaços para preenchimento e sem ponto final (issue #81, especificação 03 do lexml-eta). Sem `local`, o fecho MUST NOT ser impresso.

#### Scenario: Local e data preenchidos
- **WHEN** o documento tem `local="Sala das Sessões"` e `data="2026-04-24"`
- **THEN** o PDF contém "Sala das Sessões, 24 de abril de 2026." depois da justificação, com recuo de 2,5cm

#### Scenario: Data vazia
- **WHEN** o documento tem `local="Sala das Sessões"` e `data` vazia, ausente ou inválida
- **THEN** o fecho é "Sala das Sessões,", sem espaços para preencher, "de" nem ponto final

#### Scenario: Espaço antes conforme as opções de impressão
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** o espaço antes do fecho é de 26pt (34pt sem a opção)

#### Scenario: Sem local
- **WHEN** o documento não tem `local` nem autoria (ex.: os documentos da issue #72)
- **THEN** o PDF termina no último elemento do conteúdo, sem fecho nem assinaturas

### Requirement: Assinaturas
Quando a autoria do documento for de parlamentares (`tipo="Parlamentar"`) com ao menos um `Parlamentar`, o PDF SHALL imprimir, depois do local e data, uma assinatura por parlamentar, na ordem do documento, em duas colunas de mesma largura, centralizadas, em negrito, com 30pt de espaço antes de cada assinatura; com número ímpar de parlamentares, a última assinatura MUST ocupar as duas colunas. Cada assinatura MUST ter: o tratamento seguido do nome ("Senador"/"Senadora" quando a casa do parlamentar não for a Câmara dos Deputados, "Deputado"/"Deputada" quando for, conforme o sexo); "(`<partido>` - `<UF>`)" quando `imprimirPartidoUF` for verdadeiro; e o cargo, quando preenchido. Assinaturas adicionais (linhas em branco para assinatura manual) e assinatura de comissão MUST NOT ser impressas.

#### Scenario: Senadores e deputados
- **WHEN** o documento tem como autores um senador, uma senadora, um deputado e uma deputada
- **THEN** o PDF contém "Senador <nome>", "Senadora <nome>", "Deputado <nome>" e "Deputada <nome>", nessa ordem, em negrito e centralizados

#### Scenario: Duas colunas e número ímpar
- **WHEN** o documento tem três parlamentares
- **THEN** os dois primeiros ficam lado a lado, cada um centralizado na sua metade da largura do texto, e o terceiro fica centralizado na largura toda, abaixo deles

#### Scenario: Partido e UF
- **WHEN** `imprimirPartidoUF` é verdadeiro
- **THEN** abaixo do nome aparece "(UNIÃO - AP)" (partido e UF do parlamentar); quando é falso, nenhuma assinatura tem partido e UF

#### Scenario: Cargo
- **WHEN** um parlamentar tem `cargo="Presidente do Senado Federal"` e outro tem cargo vazio
- **THEN** o cargo aparece abaixo do primeiro, e o segundo não tem linha de cargo

#### Scenario: Mantidas com o fecho
- **WHEN** o fecho e as assinaturas não cabem no fim de uma página
- **THEN** o fecho não fica sozinho no fim da página: vai para a página seguinte junto com a primeira linha de assinaturas

#### Scenario: Autoria por comissão
- **WHEN** a autoria do documento é de uma comissão (`tipo="Comissão"`)
- **THEN** nenhuma assinatura é impressa; o fecho é impresso se houver local

### Requirement: Caixa de pendências
O PDF SHALL apresentar, na primeira página, depois do cabeçalho e antes da epígrafe, quando o documento (`lexedit:Metadado`) tiver ao menos uma pendência, uma caixa de fundo amarelo e texto vermelho em negrito com o cabeçalho "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:", alinhado à esquerda e seguido de uma lista com marcadores ("•", sem negrito) com cada pendência do documento, na ordem e com o texto em que foi registrada. O texto da caixa MUST ter o tamanho dos alertas definido nos parâmetros de impressão. A lista MUST NOT ser filtrada nem reescrita. Revisões e comentários MUST NOT gerar caixas próprias: o aviso deles é o da pendência registrada pelo editor. Sem pendências, nenhuma caixa MUST ser impressa.

#### Scenario: Pendências como registradas
- **WHEN** o documento tem as pendências "Não foi informado um texto de justificação." e "Remover comentários."
- **THEN** a caixa de pendências lista os dois itens, nessa ordem e com esse texto, abaixo do cabeçalho "Este documento contém pendências e não deve ser protocolado até que sejam resolvidas:"

#### Scenario: Revisões e comentários sem caixa própria
- **WHEN** o documento tem revisões de texto, revisões da articulação e comentários, além de pendências
- **THEN** só a caixa de pendências é impressa, sem os avisos "Este documento contém marcas de revisão ..." e "Este documento contém comentários ..."

#### Scenario: Revisões e comentários sem pendências
- **WHEN** o documento tem revisões ou comentários, mas nenhuma pendência registrada
- **THEN** nenhuma caixa é impressa

#### Scenario: Sem alertas
- **WHEN** o documento não tem pendências (ex.: os documentos da issue #72)
- **THEN** nenhuma caixa é impressa e a primeira página começa pela epígrafe, na mesma posição de hoje

#### Scenario: Tamanho acompanha a fonte
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto da caixa de pendências é impresso em 16pt

### Requirement: Anexo de parecer
Quando o `lexedit:Metadado` do documento tiver `anexoParecer` verdadeiro, o PDF MUST NOT imprimir a justificação (título "JUSTIFICAÇÃO", corpo e as notas de rodapé dela), mesmo que o documento a tenha. O local e data e as assinaturas SHALL ser impressos quando o documento os tiver, conforme os requisitos "Local e data" e "Assinaturas", logo depois da articulação. O cabeçalho, os alertas e pendências, a parte inicial e a articulação SHALL continuar impressos como nos demais documentos. Com o atributo ausente, falso ou com valor que não seja verdadeiro, o PDF SHALL ser o mesmo de um documento sem o atributo.

#### Scenario: Anexo de parecer sem justificação, fecho e assinaturas
- **WHEN** o documento tem `anexoParecer="true"`, articulação e justificação com nota de rodapé, e não tem local nem autoria
- **THEN** o PDF termina no último dispositivo da articulação, sem o título "JUSTIFICAÇÃO", sem o texto e as notas da justificação, sem local e data e sem assinaturas

#### Scenario: Local e autoria preenchidos
- **WHEN** o documento tem `anexoParecer="true"`, justificação e também `local`, `data` e autoria de parlamentares
- **THEN** a justificação não é impressa, e o local e data e as assinaturas são impressos logo depois da articulação

#### Scenario: Demais partes mantidas
- **WHEN** o documento tem `anexoParecer="true"`, cabeçalho e pendências
- **THEN** o cabeçalho, as caixas de alerta e pendências, a epígrafe, a ementa, o preâmbulo e a articulação são impressos como em um documento sem o atributo

#### Scenario: Atributo ausente ou falso
- **WHEN** o documento não tem `anexoParecer` ou tem `anexoParecer="false"`
- **THEN** a justificação, o local e data e as assinaturas são impressos normalmente

### Requirement: Epígrafe
A epígrafe SHALL ser impressa centralizada, em negrito, no tamanho de fonte de destaque e em uma única linha (espaços não quebráveis). Os espaços, quebras de linha e tabulações do início e do fim do texto MUST ser removidos; os espaços internos MUST ser preservados, sem serem colapsados (quebras de linha e tabulações internas valem como espaço). Quando, depois de "Nº" (ou "N°"), houver só espaços até a vírgula seguinte (projeto não numerado), esse vão MUST ser impresso com os espaços gravados no documento, com no mínimo 8: um vão menor (inclusive vazio) MUST ser impresso com 8 espaços. Nenhum texto de complemento MUST ser impresso abaixo da epígrafe. A epígrafe MUST NOT alterar a posição do conteúdo que vem depois dela.

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

#### Scenario: Restante do documento inalterado
- **WHEN** o mesmo documento é gerado antes e depois do ajuste da epígrafe
- **THEN** todas as linhas do PDF, exceto a da epígrafe, ficam com o mesmo texto e na mesma posição

### Requirement: Ementa
A ementa SHALL ser impressa 48pt abaixo da epígrafe, justificada, com recuo à esquerda de 6,5cm e sem recuo de primeira linha, preservando negrito e itálico. As referências com `xlink:href` MUST ser impressas como links, conforme o requisito "Remissões e links". A ementa MUST NOT ser envolvida por aspas.

#### Scenario: Ementa com referência a norma
- **WHEN** a ementa contém "Altera a <span xlink:href="urn:lex:br:federal:lei:1996-12-20;9394">Lei nº 9.394, de 20 de dezembro de 1996</span>, que estabelece ..."
- **THEN** o PDF apresenta o texto "Altera a Lei nº 9.394, de 20 de dezembro de 1996, que estabelece ..." com o recuo da ementa, sem aspas, e "Lei nº 9.394, de 20 de dezembro de 1996" como link para `https://normas.leg.br/?urn=urn:lex:br:federal:lei:1996-12-20;9394`

#### Scenario: Ementa com formatação inline
- **WHEN** a ementa contém trecho em itálico ou negrito
- **THEN** o trecho é apresentado em itálico ou negrito no PDF

### Requirement: Preâmbulo
O preâmbulo SHALL ser impresso 72pt abaixo da ementa, justificado e com recuo de primeira linha de 2,5cm, somente como texto: qualquer formatação inline MUST ser descartada. Cada parágrafo do preâmbulo SHALL ser impresso como um bloco próprio, com os espaços excedentes removidos.

#### Scenario: Preâmbulo sem formatação inline
- **WHEN** o preâmbulo contém "<b>O PRESIDENTE DA REPÚBLICA</b>, no uso da atribuição que lhe confere o art. 62 da Constituição, adota a seguinte Medida Provisória, com força de lei:"
- **THEN** o PDF apresenta o texto do preâmbulo sem negrito, com recuo de primeira linha de 2,5cm

### Requirement: Parâmetros de impressão
O tamanho de fonte do texto, o tamanho de fonte de destaque, o tamanho dos alertas, a entrelinha e os espaços entre parágrafos SHALL seguir as opções de impressão do documento (`lexedit:OpcoesImpressao`). O tamanho da fonte do texto MUST ser o `tamanhoFonte` do documento quando for 14, 16 ou 18, e 14 quando ausente ou com outro valor; o tamanho de destaque (epígrafe e título da justificação) MUST ser 18pt quando a fonte for 18 e 16pt nos demais casos; o tamanho dos alertas MUST ser 2pt menor que o da fonte do texto (12pt, 14pt e 16pt). Quando `reduzirEspacoEntreLinhas` for verdadeiro, a entrelinha MUST ser 120%, o espaço após parágrafo 0.45em e, na justificação, o espaço antes do título 22pt e antes do corpo 10pt; caso contrário (inclusive quando ausente), 150%, 0.6em, 26pt e 14pt. Os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho MUST NOT variar com essas opções. Elementos dimensionados em relação ao texto (notas de rodapé, células de tabela, sobrescrito e subscrito) MUST acompanhar o tamanho da fonte.

#### Scenario: Valores padrão
- **WHEN** um documento sem `lexedit:OpcoesImpressao`, ou com `tamanhoFonte="14"` e `reduzirEspacoEntreLinhas="false"`, é convertido em PDF
- **THEN** o texto é impresso em 14pt com entrelinha de 150%, a epígrafe em 16pt e os alertas em 12pt

#### Scenario: Fonte 16
- **WHEN** o documento tem `tamanhoFonte="16"`
- **THEN** o texto e a epígrafe são impressos em 16pt, e os alertas em 14pt

#### Scenario: Fonte 18
- **WHEN** o documento tem `tamanhoFonte="18"`
- **THEN** o texto e a epígrafe são impressos em 18pt, os alertas em 16pt, e as notas de rodapé e o texto das células de tabela ficam maiores na mesma proporção

#### Scenario: Tamanho fora dos valores do editor
- **WHEN** o documento tem `tamanhoFonte="11"`
- **THEN** o texto é impresso em 14pt e os alertas em 12pt

#### Scenario: Reduzir espaço entre linhas
- **WHEN** o documento tem `reduzirEspacoEntreLinhas="true"`
- **THEN** as linhas da articulação e da justificação ficam com entrelinha de 120% do tamanho da fonte, os parágrafos com 0.45em de espaço depois, e o título "JUSTIFICAÇÃO" com 22pt antes

#### Scenario: Espaços fixos
- **WHEN** o documento tem `tamanhoFonte="18"` e `reduzirEspacoEntreLinhas="true"`
- **THEN** os espaços antes da ementa (48pt) e do preâmbulo (72pt) e o cabeçalho são os mesmos do padrão

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
