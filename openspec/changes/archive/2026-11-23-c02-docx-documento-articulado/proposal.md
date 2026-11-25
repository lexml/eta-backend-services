# Proposal

## Why

A proposição do ETA (documento articulado) já conta com exportação em PDF/A e em RTF editável via Apache FOP. Contudo, o renderer RTF do FOP é legado e impõe limitações visuais e estruturais (cabeçalho da primeira página movido para o corpo, tabelas sem centralização, conversão com perda de PNG para JPEG, notas de rodapé sem numeração automática e pós-processamento por regex). Oferecer exportação nativa para **DOCX (OOXML)** entrega um arquivo editável moderno, fiel ao layout do PDF no Microsoft Word e no LibreOffice Writer, sem adicionar novas dependências Maven à biblioteca.

## What Changes

- Novo gerador público `DocumentoArticuladoDocxGenerator` no pacote `br.gov.lexml.eta.etaservices.documentoarticulado.pdf`, com a mesma assinatura do gerador RTF (`generate(String json, OutputStream outputStream)`), convivendo com `DocumentoArticuladoRtfGenerator` sem alterá-lo nem alterar o PDF.
- Reaproveitamento do passo comum `DocumentoArticuladoFoGerador` (validação do JSON, conversão JSON → XML, XSLT de conteúdo e template Velocity) como fonte única de verdade.
- Novo tradutor package-private XSL-FO → OOXML (`DocumentoArticuladoFoDocx`), montador de pacote OPC (`PacoteDocx`) e conversor de unidades (`MedidasFo`), usando apenas `dom4j`, `ImageIO` e `java.util.zip` (zero novas dependências).
- Suporte nativo no DOCX aos elementos emitidos pelo XSL-FO do documento articulado:
  - Página A4 com margens do PDF e cabeçalho real somente na primeira página (`header1.xml` + `w:titlePg`, brasão, casa legislativa e `textoCabecalho`);
  - Caixa de pendências antes da epígrafe em fundo amarelo contínuo com texto vermelho e lista de itens;
  - Parte inicial (epígrafe inclusive em múltiplas linhas, ementa, preâmbulo), articulação (agrupadores, dispositivos, pena, título de dispositivo, bloco de alteração com aspas/nota, omissis pontilhado com tab leader);
  - Formatação inline (`b`, `i`, `u`, `sup`/`sub`), links externos (`w:hyperlink r:id`) e remissões internas (`w:bookmarkStart` + `w:hyperlink w:anchor`);
  - Justificação com estilos do editor, exclusão de `<del>`, listas simples e aninhadas, notas de rodapé nativas com numeração automática (`word/footnotes.xml`), tabelas centralizadas com bordas e `colspan`/`rowspan` (`gridSpan`/`vMerge`), imagens embutidas (`PNG`, `JPEG`, `GIF`, `BMP`) sem recodificação e aviso em vermelho para imagens externas/ilegíveis;
  - Anexo de parecer sem justificação, fecho (local e data) e tabela de assinaturas em 2 colunas.
- Fora de escopo: embutir `documento-articulado.xml` no DOCX, anexos, extração de JSON a partir do DOCX gerado, emenda e parecer.

## Capabilities

### New Capabilities
- `docx-documento-articulado`: gerar, sob demanda, o DOCX editável do documento articulado a partir do `documento-articulado.json`, traduzindo o XSL-FO diretamente para OOXML sem novas dependências, preservando conteúdo, estrutura e formatação do PDF.

### Modified Capabilities

(nenhuma; `pdf-documento-articulado` e `rtf-documento-articulado` permanecem inalteradas)

## Impact

- Código: `br.gov.lexml.eta.etaservices.documentoarticulado.pdf` (nova classe pública `DocumentoArticuladoDocxGenerator` e classes package-private `DocumentoArticuladoFoDocx`, `PacoteDocx`, `MedidasFo`).
- API pública: nova classe aditiva, sem quebra nem alteração nas classes existentes.
- Dependências: nenhuma nova dependência no `pom.xml`.
- Testes: novos testes JUnit 5 (`MedidasFoTest`, `DocumentoArticuladoFoDocxTest`, `DocumentoArticuladoDocxGeneratorTest`) com `ConversorDocumentoArticuladoFake`, executáveis por `mvn test` sem rede nem `jsonix-lexml`; utilitário manual `Json2DOCX`.
- Documentação: `docs/api-documento-articulado.md` e `CLAUDE.md`.
