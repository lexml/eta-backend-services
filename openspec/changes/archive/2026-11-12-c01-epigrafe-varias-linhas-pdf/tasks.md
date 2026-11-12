# Tasks

## 1. Epígrafe em várias linhas na XSLT

- [x] 1.1 Conforme design D1: no template `lx:Epigrafe` de `documento-articulado-conteudo.xsl`, separar as linhas pelos `lx:br` (contagem de `br` anteriores), manter o bloco atual para a primeira linha e gerar, para cada linha seguinte não vazia, um `fo:block` centralizado, em `$maxTamanhoFonte`, sem negrito, aparado e com `&#160;`; atualizar o comentário do template. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável) em `DocumentoArticuladoConteudoTransformerTest`: duas linhas (primeira em negrito, segunda sem `font-weight`, mesmo tamanho e centralizada), três linhas, `br` seguidos e `br` no fim (sem bloco vazio), vão do número na primeira linha com segunda linha, e epígrafe sem `br` com o mesmo fragmento de hoje (testes existentes inalterados). Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 2. Título do XMP

- [x] 2.1 Conforme design D2: em `DocumentoArticuladoTemplateProcessor#acumularTexto`, acrescentar um espaço para o item `br`; teste em `DocumentoArticuladoTemplateProcessorTest` com epígrafe de duas linhas (título "… (SUBSTITUTIVO) (ao Projeto …)") e sem `br` (inalterado). Verificar com `mvn test -Dtest=DocumentoArticuladoTemplateProcessorTest`

## 3. Pipeline completo e documentação

- [x] 3.1 Criar o par de fixtures `documento-com-epigrafe-varias-linhas` (`.json` gerado pelo `lexml-eta` com epígrafe do PLV ou do substitutivo do SF; `.xml` gerado pelo `jsonix-lexml` 2.4.0 real, `C:/jsonix-lexml-win/desenv`) e registrá-lo no `ConversorDocumentoArticuladoFake`. Verificar que o XML tem o `br` na `Epigrafe` e que `ConversorDocumentoArticuladoFakeTest` passa
- [x] 3.2 Teste JUnit 5 em `DocumentoArticuladoPdfGeneratorTest` (design D3): o PDF da nova fixture tem as linhas da epígrafe na ordem, a primeira em fonte negrito e as demais em fonte regular, e a ementa 48pt abaixo da última linha. Verificar com `mvn test -Dtest=DocumentoArticuladoPdfGeneratorTest`
- [x] 3.3 Atualizar o `CLAUDE.md` (epígrafe em várias linhas; título do XMP; `jsonix-lexml` 2.4.0 para epígrafe com `br`). Verificar pela revisão do diff
- [x] 3.4 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`), se houver
- [x] 3.5 Verificação visual com o executável real: gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` 2.4.0 o PDF da nova fixture e conferir a epígrafe (primeira linha em negrito, demais abaixo sem negrito, ementa logo depois) e o título do XMP; registrar o resultado na conclusão da task
  - Resultado (12/11/2026): PDF gerado em `target/documento-com-epigrafe-varias-linhas.pdf` (14 páginas, JSON recuperado do próprio PDF). Página 1 renderizada com PDFBox: "PROJETO DE LEI DE CONVERSÃO Nº        , DE 2026" centralizada em negrito no tamanho de destaque e, logo abaixo, "(Medida Provisória nº 1.089, de 2021)" centralizada sem negrito, no mesmo tamanho, como no exemplo da issue; a ementa vem depois, com o espaço de sempre. Título do XMP: "PROJETO DE LEI DE CONVERSÃO Nº , DE 2026 (Medida Provisória nº 1.089, de 2021)". O PDF não foi versionado (a pasta `pdf-documento-articulado/pdfs-gerados/vFinal` não foi regerada).
