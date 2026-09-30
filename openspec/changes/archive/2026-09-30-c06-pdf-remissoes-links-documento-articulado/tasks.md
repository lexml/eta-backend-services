# Tasks

## 1. Fixture com remissões internas

- [x] 1.1 Criar a fixture `documento-com-remissoes-internas` conforme design D5: escrever o LexML mínimo, gerar o `.json` com `jsonix-lexml` 2.0.0 `tojson` e o `.xml` com `toxml` desse JSON em `src/test/resources/documentoarticulado/`, verificar que a ida e volta é semanticamente igual e que os arquivos estão em UTF-8; registrar o par no `ConversorDocumentoArticuladoFake.comDocumentosDeTeste()` (constante `REMISSOES_INTERNAS`) e cobrir no `ConversorDocumentoArticuladoFakeTest`. Verificar com `mvn test -Dtest=ConversorDocumentoArticuladoFakeTest`

## 2. Links na XSLT

- [x] 2.1 Na `documento-articulado-conteudo.xsl`, conforme design D1 a D4: criar as chaves `dispositivo-impresso` (restrita a `lx:Articulacao//`) e `remissao-interna`; criar o template de `lx:span[@xlink:href] | lx:Remissao[@xlink:href]` no modo `inline` (link externo `https://normas.leg.br/?urn=<URN>`, link interno quando o alvo é dispositivo impresso, senão texto), com `color="#808080"` e sem sublinhado; emitir `id` só nos alvos de remissão interna (rótulo do artigo via parâmetro `idRotulo` repassado ao caput, bloco dos demais dispositivos, bloco do título dos agrupadores). Valores com comentário de origem. Verificar pelos testes de 2.2
- [x] 2.2 Atualizar e estender o `DocumentoArticuladoConteudoTransformerTest` (JUnit 5, sem executável): inverter os testes que exigem "sem `basic-link`" (design D6); cobrir o link externo da ementa e do caput do `documento-com-articulacao-e-alteracao-de-norma` (URL e cor), o link com `!art58` dentro do bloco de alteração, a remissão inválida do exemplo da #71 como texto sem `basic-link`, e com a nova fixture o link interno ao Art. 1º (id no `fo:inline` do rótulo), ao § 1º (id no bloco do parágrafo) e ao capítulo (id no bloco do título), a remissão `art9` como texto, o preâmbulo sem link e nenhum `id` em blocos que não são alvo. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 3. PDF e documentação

- [x] 3.1 Atualizar o `DocumentoArticuladoPdfGeneratorTest`: ler as anotações de link do PDF com PDFBox e verificar, no `documento-com-pena-e-titulo-de-dispositivo`, os links URI para `https://normas.leg.br/?urn=urn:lex:br:federal:decreto.lei:1940-12-07;2848` (ementa) e `...;2848!art327`; na nova fixture, um link interno (ação ou destino de página) para cada remissão válida e nenhum para a inválida; no exemplo da #71, nenhuma anotação de link. Confirmar que o `DocumentoArticuladoJsonExtractorTest` continua passando. Verificar com `mvn test -Dtest="DocumentoArticulado*Test"`
- [x] 3.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado`): links externos e internos, chaves, ids só nos alvos, e a #72 concluída; verificar pela revisão do diff

## 4. Verificação integrada

- [x] 4.1 Rodar `mvn clean install` e verificar que todos os testes passam (novos, os das partes anteriores e os de emenda e parecer) e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 4.2 **Depois do 4.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v6** (ou v6.N após ajuste) dos três documentos da #72 e da nova fixture em `pdf-documento-articulado/pdfs-gerados/v6/` e atualizar o `README.md` da pasta. Verificar com PDFBox as anotações de link (URLs e destinos internos na página certa), a cor dos trechos com link e o JSON recuperado igual ao original; abrir no leitor e testar um link externo e um interno; conferir que a cor dos links se destaca do texto (ajustes: #404040 → #666666 na v6.1 → #808080 na v6.2); validar no veraPDF (PDF/A-3B), que é o ponto de atenção por causa das anotações. Registrar o resultado na conclusão da task

  **Resultado (30/09/2026):** PDFs v6, v6.1 e v6.2 gerados em `pdf-documento-articulado/pdfs-gerados/`. PDFBox: 8, 17 e 3 links externos nos documentos da #72, todos para `https://normas.leg.br/?urn=<URN>`; no `documento-com-remissoes-internas`, 1 externo (ementa) e 3 internos (art. 1º, § 1º do art. 1º, Capítulo I) com o destino na linha certa, `art. 9º` e o preâmbulo sem link, nenhuma anotação de outro tipo; JSON recuperado igual ao original nos quatro. Na validação visual, `#404040` (v6) e `#666666` (v6.1) ficaram pouco destacados do preto; a cor final é `#808080` (v6.2). veraPDF: v6.2 conforme PDF/A-3B.
