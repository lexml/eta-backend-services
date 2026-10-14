# Tasks

## 1. Leitura dos alertas

- [x] 1.1 Conforme design D1: criar `AlertasDocumentoArticulado` (revisões de texto ou da articulação, comentários e pendências como registradas) e compartilhar com o `FechoDocumentoArticulado` a localização do `lexedit:Metadado`. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): `AlertasDocumentoArticuladoTest` (só revisões de texto, só da articulação, nenhuma; sequência sem comentário e com comentário; pendências na ordem e sem alteração, inclusive repetindo as caixas; pendência em branco descartada; documento sem `lexedit:Metadado`) e o `FechoDocumentoArticuladoTest` continuando a passar. Verificar com `mvn test -Dtest="AlertasDocumentoArticuladoTest,FechoDocumentoArticuladoTest"`

## 2. Template

- [x] 2.1 Conforme design D2: ativar no `template-velocity-documento-articulado.xml`, antes do `$conteudoFo`, os blocos "Alertas" e "Pendências de Preenchimento" da emenda adaptados (`$possuiRevisoes`, `$possuiComentarios`, `$pendencias`); o `DocumentoArticuladoTemplateProcessor` põe esses valores no contexto (pendências escapadas), e o comentário do cabeçalho do template lista as variáveis novas. Verificar pelos testes de 2.2
- [x] 2.2 Estender o `DocumentoArticuladoTemplateProcessorTest`: as três caixas antes do conteúdo, na ordem, com os textos e atributos da emenda (amarelo, vermelho, negrito, 12pt, alinhamento); lista com um item por pendência, como registrada; sem dados, nenhuma caixa; só pendências; texto com `&` escapado. Verificar com `mvn test -Dtest=DocumentoArticuladoTemplateProcessorTest`

## 3. Fixture, PDF e documentação

- [x] 3.1 Conforme design D3: substituir pelo procedimento JSON-first as pendências do `documento-articulado-exemplo` pelas cinco da especificação 13 do lexml-eta, conferindo que só elas mudaram. Atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox): no exemplo, as três caixas e as cinco pendências antes da epígrafe, na primeira página, em 12pt (caixas em negrito, itens sem negrito); na justificação, as caixas de revisão e de comentários e a lista com dois itens; na justificação longa, só a de revisão e a lista; documentos da #72 sem caixas; variação com fonte 18 mantendo 12pt e variação só com pendências; ajustar testes que dependam da posição do conteúdo no exemplo. Verificar com `mvn test -Dtest="DocumentoArticulado*Test,ConversorDocumentoArticuladoFakeTest,AlertasDocumentoArticuladoTest,FechoDocumentoArticuladoTest"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes: alertas e pendências no padrão da emenda, condições pelos dados, lista sem filtro, 12pt fixo, pendências do exemplo). Verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v12** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v12/` e atualizar o `README.md` da pasta. Verificar com PDFBox as caixas e as pendências (ordem, 12pt, posição antes da epígrafe) e o JSON recuperado; inspeção visual da primeira página do exemplo, comparando com as imagens da emenda em `pdf-documento-articulado/imagens/atividade-pendencias/`; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
