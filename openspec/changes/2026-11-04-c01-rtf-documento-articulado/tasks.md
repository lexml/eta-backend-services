# Tasks

## 1. Passo compartilhado de geração do XSL-FO

- [x] 1.1 Extrair de `DocumentoArticuladoPdfGenerator.generate` o trecho que vai de `validar` até o `xslFo` (conversão, opções, XSLT, Velocity) para um ponto package-private que devolve `xslFo` e `xml`, sem alterar o comportamento nem a API pública do PDF; verificar rodando `mvn test -Dtest='DocumentoArticuladoPdfGeneratorTest,DocumentoArticuladoPdfGeneratorAnexosTest,DocumentoArticuladoFopProcessorTest'` sem editar esses testes
- [x] 1.2 Rodar `mvn test` completo e verificar que toda a suíte atual continua passando (rede de proteção da refatoração)

## 2. Renderização em RTF e PoC

- [x] 2.1 Criar `DocumentoArticuladoFopProcessor.gerarRtf(String xslFo)` com a fábrica FOP existente e `MIME_RTF`, sem PDF/A, XMP, anexos nem hash, devolvendo os bytes só após a renderização completa; verificar com teste JUnit 5 em `DocumentoArticuladoFopProcessorTest` que o resultado começa com `{\rtf` e que um XSL-FO inválido gera `EtaBackendException`
- [x] 2.2 PoC: gerar o RTF a partir do FO dos documentos da #72/#75 (`documento-com-articulacao-e-alteracao-de-norma`, `documento-com-capitulo-e-secao`, `documento-com-justificacao`, `documento-com-justificacao-longa`) e do `documento-articulado-exemplo`; verificar que cada um renderiza sem exceção e registrar, no `design.md`, o que o RTF preserva e o que perde (cabeçalho/brasão, pendências, omissis, notas de rodapé, listas, tabelas, imagens, assinaturas, recuos)
- [x] 2.3 Adaptar o XSL-FO ao renderer RTF em Java (`DocumentoArticuladoFoRtf`, usado só por `gerarRtf`; XSLT, Velocity e PDF intocados): trocar imagens `data:` que não sejam JPEG por um aviso em vermelho, remover o `url('...')` dos links externos e dar largura absoluta às colunas das tabelas (removendo a tabela externa de centralização); verificar com testes em `DocumentoArticuladoFopProcessorTest` (sem base64 nem texto de exceção no RTF, aviso presente, JPEG mantido, `HYPERLINK "https://..."` sem `url('`, nenhuma célula com `\cellx0`, PDF com o mesmo número de páginas) e rodando `mvn test` do PDF sem edição

## 3. Gerador público de RTF

- [ ] 3.1 Criar `DocumentoArticuladoRtfGenerator` (construtor com `ConversorDocumentoArticulado`; `generate(String json, OutputStream out)`) usando o passo compartilhado da 1.1 e o `gerarRtf` da 2.1, sem escrever no destino em caso de falha e com Javadoc informando que anexos não são embutidos nem concatenados ao RTF; verificar com o teste da 3.2 e conferindo o Javadoc na revisão do código
- [ ] 3.2 Testes JUnit 5 (`DocumentoArticuladoRtfGeneratorTest`, com `ConversorDocumentoArticuladoFake`, sem rede nem `jsonix-lexml`) cobrindo os cenários da spec `rtf-documento-articulado`: RTF válido com chaves balanceadas, acentuação, parte inicial e articulação na ordem, justificação/local/data/assinaturas, pendências antes da epígrafe, texto excluído na justificação ausente, anexo de parecer sem justificação, ausência de `documento-articulado.xml` e de anexos, JSON inválido, JSON que não é LexML, falha na conversão e falha de renderização sem escrita parcial; verificar com `mvn test -Dtest=DocumentoArticuladoRtfGeneratorTest`
- [ ] 3.3 Teste de regressão do PDF no mesmo arquivo ou em `DocumentoArticuladoPdfGeneratorTest`: o PDF de um documento gerado depois de usar o gerador de RTF no mesmo fluxo continua com o mesmo conteúdo e anexo; verificar com `mvn test -Dtest=DocumentoArticuladoPdfGeneratorTest`

## 4. Inspeção manual e documentação

- [ ] 4.1 Criar o utilitário `main` em `src/test/java/.../documentoarticulado/` (nos moldes de `Json2PDF`) que gera o `.rtf` das fixtures em `target/`, com checklist de inspeção no Word no próprio cabeçalho da classe; verificar executando-o e abrindo um dos arquivos no Word
- [ ] 4.2 Atualizar a `CLAUDE.md` (seção `documentoarticulado`: `DocumentoArticuladoRtfGenerator`, limitações do RTF, sem extração) e a lista de testes; verificar que o texto reflete o código e o comportamento da PoC
- [ ] 4.3 Rodar `mvn test` completo e verificar que toda a suíte passa

## Workflow follow-up

- Decidir, depois de avaliar o RTF, se o DOCX nativo é obrigatório; se for, abrir uma nova change.
- Arquivar a change depois da revisão.
