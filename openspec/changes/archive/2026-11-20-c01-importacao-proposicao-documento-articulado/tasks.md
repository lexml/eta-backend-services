# Tasks

## 1. Dependência e Extração de PDF

- [x] 1.1 Atualizar `br.gov.lexml.parser.pl:lexml-parser-projeto-lei` de `1.14.1` para `1.15.8` no `pom.xml` e verificar compilação e testes existentes
- [x] 1.2 Expor `extractXmlFromPdf(InputStream)` em `DocumentoArticuladoJsonExtractor` e delegar `extractJsonFromPdf` para ele, preservando os testes existentes

## 2. API e Implementação do Subpacote `documentoarticulado.importacao`

- [x] 2.1 Criar `package-info.java`, `TipoArquivoProposicao`, `ModoParseProposicao`, `OpcoesParseProposicao`, `AvisoParseProposicao` e `ResultadoParseProposicao`
- [x] 2.2 Criar `LeitorArquivoProposicao` com suporte seguro a DOCX (`DOCXReader` + `XHTMLProcessor`) e TXT (UTF-8 com fallback Windows-1252, BOM, preservação de ordem linha-a-linha via XHTML)
- [x] 2.3 Criar `ValidadorXmlLexml` com validação anti-XXE, checagem de `<LexML>`/`<Articulacao>`, normalização de URN placeholder/vazia em `APROVEITAMENTO` e validação opcional de schema
- [x] 2.4 Criar `ParserBlocosProposicao` com suporte aos modos `APROVEITAMENTO` (Cenário 2, perfil amplo de epígrafes e extração flexível de ementa) e `COMPLETO` (Cenário 1, perfil `DocumentProfile`), controle de `usarLinker`, renderização LexML e conversão de avisos de `Validation`
- [x] 2.5 Criar a fachada pública `DocumentoArticuladoParser` com todos os métodos de parseamento por formato, `TipoArquivoProposicao`, `Content-Type` e conveniências Jsonix

## 3. Testes Automatizados e Documentação

- [x] 3.1 Criar testes unitários JUnit 5 (`TipoArquivoProposicaoTest`, `LeitorArquivoProposicaoTest`, `ValidadorXmlLexmlTest`, `DocumentoArticuladoParserTest`) executáveis via `mvn test` sem rede nem executável `jsonix-lexml`
- [x] 3.2 Criar teste de integração opt-in (`DocumentoArticuladoParserIntegracaoCliTest`) para validar DOCX, TXT, XML e PDF ponta a ponta com o executável real `jsonix-lexml` contra os invariantes do `lexml-eta`
- [x] 3.3 Executar a suíte completa `mvn test` e o teste de integração real com `jsonix-lexml-win.exe` e atualizar o `CLAUDE.md`
- [x] 3.4 Suportar listas customizadas de regex de epígrafe (início, continuação e pós-epígrafe) via `OpcoesParseProposicao` e construtor com opções padrão em `DocumentoArticuladoParser`, cobrindo com testes automatizados
- [x] 3.5 Tornar `validarSchema` desabilitado por padrão também no modo `COMPLETO` (habilitação apenas via `validarSchema(true)`), atualizando `docs/api-documento-articulado.md`, a spec e o teste

