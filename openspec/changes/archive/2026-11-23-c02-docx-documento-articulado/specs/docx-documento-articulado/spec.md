# docx-documento-articulado Specification

## Purpose

Permite exportar a proposição editada no ETA como um documento Word (`.docx` / OOXML) editável, gerado sob demanda a partir do `documento-articulado.json`, preservando o conteúdo, a estrutura e a formatação principal do PDF sem novas dependências externas.

## Requirements

### Requirement: Gerar DOCX do documento articulado sob demanda
O sistema SHALL receber o conteúdo de um `documento-articulado.json` e escrever no destino informado um arquivo `.docx` (ZIP/OOXML) válido com o conteúdo da proposição. A geração MUST ser independente da geração do PDF e do RTF: o DOCX não é embutido no PDF e nem o PDF nem o RTF mudam por causa dele.

#### Scenario: Documento válido gera DOCX
- **WHEN** a geração recebe um `documento-articulado.json` válido
- **THEN** é escrito no destino um arquivo ZIP válido contendo `[Content_Types].xml`, `_rels/.rels`, `word/document.xml`, `word/styles.xml` e `word/_rels/document.xml.rels`

#### Scenario: Acentuação preservada
- **WHEN** o documento contém textos com caracteres acentuados (ex.: "Sala das Sessões", "Fica instituído")
- **THEN** o `word/document.xml` em UTF-8 apresenta esses caracteres corretamente

#### Scenario: PDF e RTF não são afetados
- **WHEN** um mesmo documento é convertido em PDF ou RTF antes e depois da geração de DOCX
- **THEN** o PDF e o RTF gerados mantêm o mesmo conteúdo e anexos

### Requirement: Conteúdo impresso no DOCX
O DOCX SHALL conter, na mesma ordem do PDF, a caixa de pendências (quando houver), a parte inicial (epígrafe, ementa e preâmbulo), a articulação com os rótulos e textos dos dispositivos, a justificação, o local e a data e as assinaturas, quando existirem no documento. O texto de revisões marcadas como excluídas na justificação MUST NOT ser impresso.

#### Scenario: Parte inicial e articulação
- **WHEN** o documento tem epígrafe, ementa, preâmbulo e artigos com parágrafos e incisos
- **THEN** o DOCX contém a epígrafe, a ementa, o preâmbulo e, depois deles, cada dispositivo com seu rótulo e seu texto, nessa ordem

#### Scenario: Justificação, local e assinaturas
- **WHEN** o documento tem justificação, local, data e autores parlamentares
- **THEN** o DOCX contém a justificação, o local com a data por extenso e o nome de cada parlamentar em tabela de assinaturas, depois da articulação

#### Scenario: Caixa de pendências equivalente ao PDF
- **WHEN** o documento registra pendências
- **THEN** o DOCX apresenta, antes da epígrafe, uma caixa com fundo amarelo (`FFFF00`), texto vermelho (`FF0000`) com o título em negrito e a lista de pendências registradas

#### Scenario: Texto excluído na justificação
- **WHEN** a justificação tem um trecho marcado como excluído em revisão
- **THEN** o texto desse trecho não aparece no DOCX

### Requirement: Anexo de parecer sem justificação no DOCX
Para um documento marcado como anexo de parecer, o DOCX SHALL omitir a justificação e manter os demais conteúdos, como no PDF.

#### Scenario: Anexo de parecer
- **WHEN** o documento está marcado como anexo de parecer e tem justificação, local, data e assinaturas
- **THEN** o DOCX não contém a justificação e contém o local, a data e as assinaturas

### Requirement: Cabeçalho somente na primeira página no DOCX
Quando o documento tem cabeçalho (`imprimirBrasao` verdadeiro ou `textoCabecalho` preenchido), o DOCX SHALL colocá-lo em um cabeçalho de primeira página (`word/header1.xml` com `w:titlePg`), contendo o Brasão da República e o nome da casa legislativa (quando `imprimirBrasao` é verdadeiro e a URN identifica uma casa) e o `textoCabecalho` (quando preenchido). Com `imprimirBrasao` falso, o DOCX MUST NOT conter o Brasão nem o nome da casa.

#### Scenario: Brasão, casa e texto do cabeçalho
- **WHEN** o documento tem `imprimirBrasao` verdadeiro, URN de uma casa legislativa e `textoCabecalho` preenchido
- **THEN** o DOCX contém `word/header1.xml` referenciado como `first` com `w:titlePg`, incluindo a imagem do Brasão, o nome da casa e o texto do cabeçalho

#### Scenario: Sem Brasão
- **WHEN** o documento tem `imprimirBrasao` falso
- **THEN** o DOCX não contém a imagem do Brasão nem o nome da casa legislativa

### Requirement: Notas de rodapé nativas no DOCX
As notas de rodapé da justificação SHALL ser geradas como notas de rodapé nativas do Word em `word/footnotes.xml`, referenciadas no corpo por `w:footnoteReference`, sem duplicação do número no texto da nota.

#### Scenario: Nota de rodapé na justificação
- **WHEN** a justificação tem uma nota de rodapé
- **THEN** o DOCX contém `word/footnotes.xml` com o conteúdo da nota associado ao `w:footnoteReference` correspondente no corpo

### Requirement: Tabelas, listas e omissis no DOCX
O DOCX SHALL renderizar listas simples e aninhadas com recuo deslocado, linhas pontilhadas de omissis com tabulação pontilhada (`w:leader="dot"`) e tabelas com colunas de largura positiva, centralização preservada na tabela da justificação, bordas e mesclagens (`w:gridSpan` e `w:vMerge`).

#### Scenario: Tabela da justificação com spans e centralização
- **WHEN** a justificação tem uma tabela com largura percentual, `colspan` e `rowspan`
- **THEN** o DOCX contém uma única tabela centralizada (`w:jc="center"`) com `w:gridSpan` e `w:vMerge` correspondentes

#### Scenario: Omissis
- **WHEN** a articulação tem `Omissis` ou dispositivo com `textoOmitido="s"`
- **THEN** o parágrafo correspondente no DOCX possui tab stop com `w:leader="dot"` e caractere de tabulação (`w:tab`)

### Requirement: Links externos e remissões internas no DOCX
Os links externos (remissões a normas e links da justificação) SHALL ser gerados como hiperlinks externos (`TargetMode="External"`) com o endereço limpo (sem `url('...')`). As remissões internas SHALL apontar via `w:anchor` para um marcador (`w:bookmarkStart`/`w:bookmarkEnd`) no dispositivo de destino.

#### Scenario: Remissão a uma norma
- **WHEN** o documento tem uma remissão a uma norma por URN
- **THEN** o relacionamento de hiperlink aponta para `https://normas.leg.br/?urn=<URN>`

#### Scenario: Remissão interna
- **WHEN** o documento tem uma remissão interna para um dispositivo impresso
- **THEN** o DOCX contém `<w:hyperlink w:anchor="<id>">` e o respectivo `<w:bookmarkStart w:name="<id>"/>`

### Requirement: Imagens no DOCX
O DOCX SHALL embutir em `word/media/` as imagens da justificação (`data:image/*`) legíveis no formato original (como PNG e JPEG), mantendo a proporção. Imagens externas ou ilegíveis MUST ser substituídas por um aviso em vermelho informando que foram omitidas, sem incluir dados base64 nem mensagens de exceção no documento.

#### Scenario: Imagens PNG e JPEG
- **WHEN** a justificação tem uma imagem PNG e uma imagem JPEG embutidas
- **THEN** ambas são embutidas em `word/media/` em seus formatos originais e referenciadas por `w:drawing`, sem aviso de omissão

#### Scenario: Imagem externa ou ilegível
- **WHEN** a justificação tem uma imagem com URL externa ou dados base64 inválidos
- **THEN** o DOCX contém no lugar da imagem o aviso em vermelho de imagem omitida, sem o endereço nem o conteúdo base64

### Requirement: DOCX sem modelo embutido e sem anexos
O DOCX SHALL NOT embutir o `documento-articulado.xml` nem anexos.

#### Scenario: Somente o documento editável
- **WHEN** o DOCX é gerado
- **THEN** o ZIP não contém `documento-articulado.xml` nem arquivos anexos

### Requirement: Rejeitar documento inválido e escrita atômica na geração do DOCX
A geração do DOCX SHALL rejeitar, com `EtaBackendException` e sem escrever bytes no destino, uma entrada que não seja JSON válido, que não seja um documento LexML, ou cuja conversão/geração falhe.

#### Scenario: Entrada inválida ou falha na conversão
- **WHEN** a geração recebe JSON inválido, JSON não-LexML ou o conversor falha
- **THEN** é lançada `EtaBackendException` e nenhum byte é escrito no `OutputStream`
