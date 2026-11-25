# Tasks

## 1. Infraestrutura OOXML (`MedidasFo` e `PacoteDocx`)

- [x] 1.1 Criar `MedidasFo` (package-private em `documentoarticulado.pdf`) para conversão de unidades XSL-FO (`mm`, `cm`, `pt`, `in`, `px`, `em`, `%`) em twips, half-points, EMUs e entrelinha Word
- [x] 1.2 Criar `PacoteDocx` (package-private) para montagem do ZIP/OOXML (`[Content_Types].xml`, `_rels/.rels`, `word/styles.xml`, `word/settings.xml`, `word/document.xml`, partes opcionais `word/header1.xml`, `word/footnotes.xml`, `word/media/*` e relacionamentos `.rels`)
- [x] 1.3 Criar testes unitários JUnit 5 em `MedidasFoTest` cobrindo as conversões de unidades e verificar com `mvn test -Dtest=MedidasFoTest`

## 2. Tradutor XSL-FO → DOCX (`DocumentoArticuladoFoDocx`)

- [x] 2.1 Implementar `DocumentoArticuladoFoDocx` com configuração de página A4, cabeçalho somente na primeira página (`header1.xml` + `w:titlePg`), pilha de estilos herdados, achatamento de `fo:block`/`fo:block-container`, caixa de pendências (célula 1×1 com fundo amarelo e padding), inline (`b`, `i`, `u`, `sup`/`sub`, cor), links externos e remissões internas (`w:bookmarkStart`/`w:hyperlink`)
- [x] 2.2 Adicionar suporte a omissis (`fo:leader` → tab stop pontilhado), notas de rodapé nativas (`word/footnotes.xml` sem duplicação de número), listas simples e aninhadas (`fo:list-block`), tabelas (remoção da tabela centralizadora, `w:jc="center"`, larguras, bordas, `w:gridSpan`, `w:vMerge`) e imagens (`data:image/*` nativas em `word/media/` e aviso em vermelho para externas/ilegíveis)
- [x] 2.3 Criar testes unitários JUnit 5 em `DocumentoArticuladoFoDocxTest` validando cada construção OOXML gerada e verificar com `mvn test -Dtest=DocumentoArticuladoFoDocxTest`

## 3. Gerador público `DocumentoArticuladoDocxGenerator` e testes de ponta a ponta

- [x] 3.1 Criar `DocumentoArticuladoDocxGenerator` (público, construtor com `ConversorDocumentoArticulado`, `generate(String json, OutputStream outputStream)`) reutilizando `DocumentoArticuladoFoGerador`, com escrita atômica e Javadoc completo
- [x] 3.2 Criar `DocumentoArticuladoDocxGeneratorTest` (JUnit 5 com `ConversorDocumentoArticuladoFake`, sem rede nem `jsonix-lexml`) cobrindo todos os cenários da spec `docx-documento-articulado`, incluindo todas as fixtures, rejeições de entrada sem escrita parcial, não-regressão do PDF e do RTF e leitura do DOCX gerado via `LeitorArquivoProposicao.lerDocx`
- [x] 3.3 Verificar rodando `mvn test -Dtest='MedidasFoTest,DocumentoArticuladoFoDocxTest,DocumentoArticuladoDocxGeneratorTest'`

## 4. Inspeção manual, documentação e validação final

- [x] 4.1 Criar utilitário manual `Json2DOCX` em `src/test/java/.../documentoarticulado/pdf/` (nos moldes de `Json2RTF`/`DocumentoArticuladoJson2PDF`) com checklist de inspeção visual no Word e LibreOffice Writer
- [x] 4.2 Atualizar `docs/api-documento-articulado.md` e `CLAUDE.md` documentando `DocumentoArticuladoDocxGenerator`
- [x] 4.3 Rodar `mvn test` completo e verificar que toda a suíte passa sem regressões
