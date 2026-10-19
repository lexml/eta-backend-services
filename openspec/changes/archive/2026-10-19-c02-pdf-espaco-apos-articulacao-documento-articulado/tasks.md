# Tasks

## 1. XSLT e testes

- [x] 1.1 Conforme design D1: `padding-bottom="{$pMarginBottom}"` no bloco da articulação da `documento-articulado-conteudo.xsl`, com o comentário do template atualizado. Verificar pelos testes de 1.2
- [x] 1.2 Testes JUnit 5 (sem executável): no `DocumentoArticuladoConteudoTransformerTest`, o bloco da articulação com `padding-bottom` 0.6em (0.45em com "reduzir") e os dispositivos sem margem própria; no `DocumentoArticuladoPdfGeneratorTest`, na fixture `documento-com-justificacao` (fonte 14, sem "reduzir"), 55pt entre o último dispositivo e "JUSTIFICAÇÃO" e 21pt entre dispositivos. Verificar com `mvn test -Dtest="DocumentoArticuladoConteudoTransformerTest,DocumentoArticuladoPdfGeneratorTest"`

## 2. Verificação integrada

- [x] 2.1 Rodar `mvn clean install` e verificar que todos os testes passam e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`); atualizar o `CLAUDE.md` (espaço após a articulação); a pedido do usuário, o `documento-articulado-exemplo` volta a `reduzirEspacoEntreLinhas="false"` (par regenerado JSON-first, só a opção muda), com o teste de comparação "reduzir" usando uma variação
- [x] 2.2 **Depois do 2.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v13.2** de todos os documentos de teste em `pdf-documento-articulado/pdfs-gerados/v13.2/`, mais o exemplo sem "reduzir" em `pdf-documento-articulado/PDF-COMPARACAO/` para comparação com as emendas, e atualizar o `README.md` da pasta. Verificar com PDFBox os 55pt antes de "JUSTIFICAÇÃO" e que nada mais mudou em relação à v13.1; validar no veraPDF (PDF/A-3B). Registrar o resultado na conclusão da task
