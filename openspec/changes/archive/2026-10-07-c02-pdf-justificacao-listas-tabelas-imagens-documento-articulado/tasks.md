# Tasks

## 1. Verificação e fixtures

- [x] 1.1 Verificar com o FOP 2.7 em modo PDF/A-3b (configuração e fontes do documento articulado), antes de implementar: imagem em data URI PNG com transparência, JPEG, GIF, BMP e ICO, e `fo:footnote` dentro de `fo:table-cell` e de `fo:list-item-body`; registrar no design (D3/D4) o resultado por formato (renderiza / ignora com erro registrado / falha). Se algum caso impedir a geração ou quebrar o PDF/A, pausar e levar a decisão ao usuário. Verificar pelo resultado registrado
- [x] 1.2 Ampliar a fixture `documento-com-justificacao` conforme design D5 (listas, tabelas, imagens e classes `align-*` como o editor grava), pelo procedimento JSON-first; conferir que o texto do XML final é igual ao do fonte e que o executável não reporta erro
- [x] 1.3 Ampliar o `documento-articulado-exemplo` com as mesmas variações (mantendo o conteúdo e os metadados existentes), pelo mesmo procedimento; ajustar os testes que contam links, ids ou notas do exemplo. Verificar com `mvn test -Dtest="ConversorDocumentoArticuladoFakeTest,DocumentoArticuladoConteudoTransformerTest,DocumentoArticuladoPdfGeneratorTest"`

## 2. Alinhamento e listas

- [x] 2.1 Na `documento-articulado-conteudo.xsl`, conforme design D1 e D2: classes `align-*` além de `ql-align-*`; template de `ol`/`ul` no modo `justificacao` e dentro de `li` (lista aninhada), com rótulos, recuos, `margin-bottom` e conteúdo do item; `indent-N` ignorado. Valores com comentário de origem (emenda). Verificar pelos testes de 2.2
- [x] 2.2 Estender o `DocumentoArticuladoConteudoTransformerTest` (JUnit 5, sem executável): `align-*`, atributos do `fo:list-block`, rótulos "1."/"2."/"•", numeração da lista aninhada e continuação da externa, `indent-1` no mesmo nível, formatação, remissão e nota de rodapé no item. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 3. Tabelas e imagens

- [x] 3.1 Na XSLT, conforme design D3 e D4: tabela externa centralizadora com a largura da tabela, tabela interna com bordas, colunas iguais (contando `colspan`), células (`td`/`th`) com spans; imagem em data URI em bloco próprio com largura e proporção, `src` externo ignorado, parágrafo que começa com imagem centralizado e não tratado como vazio. Verificar pelos testes de 3.2
- [x] 3.2 Estender o `DocumentoArticuladoConteudoTransformerTest`: largura e colunas das tabelas, bordas por `border`, `th` em negrito, `number-columns-spanned`/`number-rows-spanned`, nota e formatação em célula; largura das imagens (com e sem `width`), centralização, `src` externo sem `fo:external-graphic`. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 4. PDF e documentação

- [x] 4.1 Atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox, sem executável): texto dos itens com os rótulos e das células no PDF, imagens presentes na página (XObject) com a largura esperada, nota de célula e de item no rodapé da página, PDF gerado mesmo com imagem externa. Confirmar que o `DocumentoArticuladoJsonExtractorTest` continua passando. Verificar com `mvn test -Dtest="DocumentoArticulado*Test"`
- [x] 4.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes): listas, tabelas, imagens, classes `align-*` e o que fica fora (aninhamento por `indent-N`, larguras de coluna). Verificar pela revisão do diff

## 5. Verificação integrada

- [x] 5.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 5.2 **Depois do 5.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v8** do `documento-articulado-exemplo`, do `documento-com-justificacao` e do `documento-com-justificacao-longa` em `pdf-documento-articulado/pdfs-gerados/v8/` e atualizar o `README.md` da pasta. Verificar com PDFBox o texto, as imagens, as notas e o JSON recuperado (diferenças só nos espaços entre elementos inline, limitação conhecida); inspeção visual; validar no veraPDF (PDF/A-3B), com atenção às imagens. Registrar o resultado na conclusão da task

  **Resultado (07/10/2026):** PDFs v8 e, depois dos ajustes da validação visual (`border="1"` nas fixtures, como o editor grava, e `line-height-shift-adjustment="disregard-shifts"` no corpo da justificação), v8.1 em `pdf-documento-articulado/pdfs-gerados/`: exemplo (5 páginas), `documento-com-justificacao` (4) e `documento-com-justificacao-longa` (3). PDFBox: rótulos e recuos das listas, células em 11pt com `th` em negrito e células da mesma linha alinhadas, imagens com 40%, 20% e 100% da largura do texto (a de 40% centralizada), imagem externa ausente, notas em item e em célula no rodapé da página da referência. JSON recuperado igual na longa; no exemplo e no catálogo difere só nos espaços entre elementos inline (limitação conhecida da conversão). Inspeção visual ok. veraPDF: os três v8.1 conformes PDF/A-3B (validado pelo usuário).
