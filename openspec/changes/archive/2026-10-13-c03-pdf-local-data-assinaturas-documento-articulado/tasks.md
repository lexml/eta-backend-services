# Tasks

## 1. Leitura do local, data e assinaturas

- [x] 1.1 Conforme design D2: criar `FechoDocumentoArticulado` (local, data por extenso em pt-BR, assinaturas com tratamento, partido/UF e cargo; só `tipo="Parlamentar"`) e `getEspacoAntesFecho()` em `ParametrosImpressaoDocumentoArticulado` (34pt; 26pt com "reduzir"). Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): `FechoDocumentoArticuladoTest` (data preenchida, vazia, ausente e inválida; local ausente e aparado; Senador/Senadora/Deputado/Deputada; partido/UF com `imprimirPartidoUF` true e false; cargo preenchido e vazio; ordem; comissão e sem autoria -> sem assinaturas) e o caso novo no `ParametrosImpressaoDocumentoArticuladoTest`. Verificar com `mvn test -Dtest="FechoDocumentoArticuladoTest,ParametrosImpressao*Test"`

## 2. Template

- [x] 2.1 Conforme design D3: ativar no `template-velocity-documento-articulado.xml`, depois do `$conteudoFo`, os blocos "Local e data" e "Assinaturas" da emenda adaptados (`$local`, `$dataPorExtenso`, `$espacoAntesFecho`, `$assinaturas` em pares), mantendo comentadas as assinaturas adicionais; o `DocumentoArticuladoTemplateProcessor` põe esses valores no contexto (escapados), e o comentário do cabeçalho do template lista as variáveis novas. Verificar pelos testes de 2.2
- [x] 2.2 Estender o `DocumentoArticuladoTemplateProcessorTest`: FO com o fecho (recuo, espaço antes, `keep-with-next`) e a tabela de duas colunas com pares e última célula com `number-columns-spanned="2"`; data vazia com espaços; sem local nem autoria, nada; comissão, só o fecho; nome com `&` escapado. Verificar com `mvn test -Dtest=DocumentoArticuladoTemplateProcessorTest`

## 3. Fixtures, PDF e documentação

- [x] 3.1 Conforme design D4: atualizar pelo procedimento JSON-first o `documento-articulado-exemplo` (cinco autores: senador com cargo, senadora sem cargo, deputado e deputada fictícios, quinto fictício; `ParteFinal` coerente) e o `documento-com-justificacao-longa` (`imprimirPartidoUF="false"`, `data` vazia), conferindo que só a autoria/data/`ParteFinal` mudaram. Atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox): ordem (justificação, fecho, assinaturas) e cada texto uma única vez; fecho com recuo e data por extenso; assinaturas em negrito, centralizadas nas colunas e a ímpar no centro da largura; partido/UF e cargo; data vazia e sem partido/UF na justificação longa; variações em memória (comissão, sem local) registradas no `ConversorDocumentoArticuladoFake`; documentos da #72 sem fecho; substituir o teste/cenário "Elementos ainda não impressos"; ajustar testes que dependam do fim do exemplo. Verificar com `mvn test -Dtest="DocumentoArticulado*Test,ConversorDocumentoArticuladoFakeTest,FechoDocumentoArticuladoTest"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes: fecho e assinaturas pelo `lexedit:Metadado`, padrão da emenda, data vazia, `ParteFinal` ignorado, fixtures) e registrar no `pdf-documento-articulado/historico.md`, nos itens da atividade de formatação, a alternativa do `ParteFinal` textual (design D5). Verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v11** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v11/` e atualizar o `README.md` da pasta. Verificar com PDFBox o fecho e as assinaturas (posições, negrito, colunas) no exemplo, na justificação longa e na justificação, a ausência nos documentos da #72 e o JSON recuperado; inspeção visual das últimas páginas; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
