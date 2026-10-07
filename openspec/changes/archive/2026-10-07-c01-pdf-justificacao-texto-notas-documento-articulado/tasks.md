# Tasks

## 1. Fixtures com justificação

- [x] 1.1 Criar a fixture `documento-com-justificacao` conforme design D6 (catálogo da parte A, com os metadados lexedit das specs 08, 09, 12 e 13 do lexml-eta e local e data e assinaturas na `ParteFinal`): escrever o LexML-fonte, gerar o `.json` com `jsonix-lexml` 2.0.0 `tojson`, reinserir no JSON os espaços entre elementos inline descartados pelo `tojson`, gerar o `.xml` com `toxml` desse JSON e conferir que o texto dos parágrafos do XML final é igual ao do fonte (inclusive os espaços); UTF-8. Verificar pela comparação do texto e pela ausência de erro do executável
- [x] 1.2 Criar a fixture `documento-com-justificacao-longa` (justificação realista de 3 a 4 páginas com notas de rodapé em páginas diferentes), pelo mesmo procedimento de 1.1. Verificar pela comparação do texto e pela ausência de erro do executável
- [x] 1.3 Registrar os dois pares no `ConversorDocumentoArticuladoFake.comDocumentosDeTeste()` (constantes `JUSTIFICACAO` e `JUSTIFICACAO_LONGA`) e cobrir no `ConversorDocumentoArticuladoFakeTest` (JUnit 5, sem executável). Verificar com `mvn test -Dtest=ConversorDocumentoArticuladoFakeTest`

## 2. Justificação na XSLT

- [x] 2.1 Na `documento-articulado-conteudo.xsl`, conforme design D1 a D3: aplicar `lx:ProjetoNorma/lx:Justificacao` depois da `lx:Norma`; título "JUSTIFICAÇÃO" e bloco do corpo só quando houver texto fora de `del`; um bloco por `p` com `margin-bottom`; classes do Quill por token; `ol`, `ul` e `table` não impressos. Valores com comentário de origem (emenda). Verificar pelos testes de 2.3
- [x] 2.2 No modo `inline`, conforme design D4: `u`, `sub`, `sup`, `a` com endereço web (link no estilo das remissões), `del` omitido, `ins` e `span` sem `xlink:href` como texto. Verificar pelos testes de 2.3
- [x] 2.3 Estender o `DocumentoArticuladoConteudoTransformerTest` (JUnit 5, sem executável) com o `documento-com-justificacao`: posição e atributos do título, parágrafos (recuo, `margin-bottom`, alinhamento padrão), cada classe de D3, cada marca inline, `del`/`ins`, trecho comentado, links do `a` e das remissões na justificação, parágrafo vazio, justificação vazia sem título, listas e tabelas não impressas; e confirmar que os testes da #72 continuam passando. Verificar com `mvn test -Dtest=DocumentoArticuladoConteudoTransformerTest`

## 3. Notas de rodapé

- [x] 3.1 Na XSLT, conforme design D5: `NotaDeRodape` -> `fo:footnote` com número sobrescrito, corpo em 0.7em com "número + espaço + texto" e propriedades herdadas zeradas; numeração `level="any"` que ignora notas em `del`. No template Velocity, ativar o `fo:static-content` `xsl-footnote-separator` (traço de 50%, 0.5pt) antes do `fo:flow`. Verificar pelos testes de 3.2
- [x] 3.2 Estender o `DocumentoArticuladoConteudoTransformerTest` com as notas do `documento-com-justificacao`: números 1, 2 e 3 na ordem, nota em `del` ausente e sem número, formatação e remissão dentro da nota, propriedades zeradas no corpo; e o `DocumentoArticuladoTemplateProcessorTest` com o separador antes do fluxo. Verificar com `mvn test -Dtest="DocumentoArticuladoConteudoTransformerTest,DocumentoArticuladoTemplateProcessorTest"`

## 4. PDF e documentação

- [x] 4.1 Atualizar o `DocumentoArticuladoPdfGeneratorTest` (JUnit 5, PDFBox, sem executável): ordem parte inicial -> articulação -> justificação; versão revisada (sem "reforma", com "modernização" no exemplo); fontes de negrito e itálico; anotações de link do `a` e das remissões; texto de cada nota abaixo do corpo na mesma página; numeração contínua em páginas diferentes no `documento-com-justificacao-longa`; local e data e assinaturas ausentes; trocar o `justificacaoAindaNaoImpressa` pela verificação da justificação impressa. Confirmar que o `DocumentoArticuladoJsonExtractorTest` continua passando. Verificar com `mvn test -Dtest="DocumentoArticulado*Test"`
- [x] 4.2 Atualizar o `CLAUDE.md` (seção `etaservices.documentoarticulado` e Testes): justificação, classes do Quill, revisões, notas de rodapé, separador no template, novas fixtures e o que ainda não é impresso (listas, tabelas e imagens). Verificar pela revisão do diff

## 5. Verificação integrada

- [x] 5.1 Rodar `mvn clean install` e verificar que todos os testes passam (novos, os da #71/#72 e os de emenda e parecer) e que nenhum arquivo de emenda, parecer ou `lexmljsonix` foi alterado; descartar a regravação do `emenda.pdf` (`git checkout -- emenda.pdf`)
- [x] 5.2 **Depois do 5.1**, gerar com `DocumentoArticuladoJson2PDF` e o `jsonix-lexml` real os PDFs **v7** do `documento-articulado-exemplo` e das duas fixtures novas em `pdf-documento-articulado/pdfs-gerados/v7/` e atualizar o `README.md` da pasta. Verificar com PDFBox o texto, as fontes, as notas no rodapé (posição e numeração) e o JSON recuperado igual ao original; inspeção visual; validar no veraPDF (PDF/A-3B), com atenção às notas de rodapé. Registrar o resultado na conclusão da task

  **Resultado (07/10/2026):** PDFs v7 em `pdf-documento-articulado/pdfs-gerados/v7/`: exemplo (4 páginas, com a justificação ampliada a pedido do usuário para cobrir todos os recursos), `documento-com-justificacao` (2) e `documento-com-justificacao-longa` (3). PDFBox: ordem, versão revisada, fontes e notas conferidas (cada nota no rodapé da página da referência, abaixo do texto, numeração contínua; catálogo 1-2 na p1 e 3 na p2; longa 1 na p1, 2-3 na p2 e 4-6 na p3); XML embutido com os espaços entre elementos inline. Inspeção visual ok. JSON recuperado igual ao original na longa; no exemplo e no catálogo difere só nos textos formados por espaço entre elementos inline, descartados pelo `jsonix-lexml tojson` na reabertura (limitação conhecida da conversão, fora do escopo; ignorando esses textos, idêntico). veraPDF: os três conformes PDF/A-3B (validado pelo usuário).
