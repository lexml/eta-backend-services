## ADDED Requirements

### Requirement: Atualização do parser LexML para 1.15.8
O projeto SHALL utilizar a versão `1.15.8` da biblioteca `br.gov.lexml.parser.pl:lexml-parser-projeto-lei`.

#### Scenario: Dependência configurada no POM
- **WHEN** o projeto é compilado e testado pelo Maven
- **THEN** a versão resolvida de `lexml-parser-projeto-lei` é `1.15.8`

### Requirement: Extração de XML em PDF do documento articulado
O `DocumentoArticuladoJsonExtractor` SHALL expor `extractXmlFromPdf(InputStream)` para obter em memória o conteúdo UTF-8 do anexo `documento-articulado.xml` embutido em PDFs gerados pelo editor de proposições, mantendo o comportamento atual de `extractJsonFromPdf(InputStream, Writer)`.

#### Scenario: PDF gerado pelo editor de proposições
- **WHEN** `extractXmlFromPdf` recebe um PDF gerado por `DocumentoArticuladoPdfGenerator`
- **THEN** devolve o XML UTF-8 exato do anexo `documento-articulado.xml`

#### Scenario: PDF sem o anexo esperado
- **WHEN** `extractXmlFromPdf` recebe um PDF sem `documento-articulado.xml` ou bytes que não formam um PDF válido
- **THEN** lança `EtaBackendException` com a mensagem `"Não se trata de um arquivo gerado pelo editor de proposições."`

### Requirement: Importação unificada de DOCX, TXT, XML e PDF para Jsonix
O `DocumentoArticuladoParser` SHALL receber um `ConversorDocumentoArticulado` no construtor e converter entradas nos formatos `DOCX`, `TXT`, `XML` (LexML) e `PDF` (com `documento-articulado.xml` embutido) em `ResultadoParseProposicao` contendo o Jsonix gerado, o XML LexML e a lista imutável de avisos de validação (`AvisoParseProposicao`), oferecendo também métodos de conveniência que retornam diretamente a `String` Jsonix. O envio deverá aceitar identificação explícita (`TipoArquivoProposicao`) ou por `Content-Type` (`application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `text/plain`, `application/xml`, `text/xml` e `application/pdf`, ignorando parâmetros como `; charset=utf-8`).

#### Scenario: Dispatch por Content-Type
- **WHEN** `parsePorContentType` recebe um arquivo válido com um dos Content-Types suportados (com ou sem parâmetro de charset)
- **THEN** o arquivo é roteado para o parser/extrator correspondente e devolve Jsonix válido

#### Scenario: Content-Type não suportado
- **WHEN** `parsePorContentType` recebe `Content-Type` nulo, vazio ou não suportado
- **THEN** lança `EtaBackendException` identificando a recusa

### Requirement: Modo APROVEITAMENTO (Cenário 2 — padrão) para nova proposição no lexml-eta
No modo `APROVEITAMENTO` (utilizado por padrão quando nenhuma opção é informada), o parser SHALL priorizar o aproveitamento de **ementa** e **articulação** de arquivos DOCX e TXT para iniciar uma nova proposição no `lexml-eta`, isolando epígrafes de proposições ou normas existentes (sem contaminar a ementa) e descartando seções finais (fecho, assinatura, justificação, anexos e legislação citada). O modo `APROVEITAMENTO` SHALL aceitar documentos sem epígrafe, sem preâmbulo e inclusive documentos contendo apenas articulação (sem ementa), e MUST gerar no XML uma URN compatível com a validação do `lexml-eta`.

#### Scenario: Proposição ou norma existente em DOCX ou TXT
- **WHEN** um arquivo DOCX ou TXT contém epígrafe (de proposição ou norma vigente), ementa, preâmbulo, articulação e justificação e é parseado em modo `APROVEITAMENTO`
- **THEN** a ementa e todos os dispositivos da articulação (excluindo justificação e assinatura) são extraídos para XML com URN válida e convertidos para Jsonix

#### Scenario: Documento apenas com articulação
- **WHEN** um arquivo DOCX ou TXT começa diretamente com dispositivos articulados (ex.: `Art. 1º ...`) sem epígrafe, ementa ou preâmbulo
- **THEN** o parseamento em modo `APROVEITAMENTO` conclui com sucesso contendo os dispositivos da articulação estruturados

#### Scenario: Documento com cabeçalho informal antes da ementa
- **WHEN** um arquivo DOCX ou TXT não possui epígrafe padrão, mas apresenta título informal seguido de linha em branco e ementa antes do preâmbulo ou articulação
- **THEN** o bloco imediatamente anterior ao preâmbulo/articulação é aproveitado como ementa

#### Scenario: Preservação de ordem das linhas em TXT
- **WHEN** um arquivo TXT contém dispositivos em linhas consecutivas sem linhas em branco entre si, em UTF-8 (com ou sem BOM) ou Windows-1252
- **THEN** a ordem exata das linhas é preservada e cada dispositivo é reconhecido separadamente

#### Scenario: Documento sem dispositivos reconhecidos
- **WHEN** um arquivo DOCX ou TXT não contém nenhum dispositivo estruturado da articulação
- **THEN** o parser lança `EtaBackendException` com mensagem clara voltada ao usuário

### Requirement: Modo COMPLETO (Cenário 1) para proposição existente
No modo `COMPLETO`, o parser SHALL realizar o parseamento completo de epígrafe, ementa, preâmbulo e articulação de arquivos DOCX e TXT segundo o `DocumentProfile` selecionado em `OpcoesParseProposicao` (por autoridade e tipo de norma), aplicando as obrigatoriedades de epígrafe e ementa definidas pelo perfil e validando contra o esquema XSD somente quando `validarSchema` for habilitado explicitamente (desabilitado por padrão, em ambos os modos).

#### Scenario: Parseamento completo com perfil válido
- **WHEN** um arquivo DOCX ou TXT completo (epígrafe, ementa, preâmbulo e articulação) é parseado com `OpcoesParseProposicao.completo("senado.federal", "projeto.lei;pls")`
- **THEN** epígrafe, ementa, preâmbulo e articulação são preservados no XML gerado e convertidos para Jsonix

#### Scenario: Epígrafe ausente em perfil que a exige
- **WHEN** um arquivo sem epígrafe é parseado em modo `COMPLETO` com um perfil que exige epígrafe obrigatória
- **THEN** o parser lança `EtaBackendException` informando que a epígrafe está ausente ou fora do padrão

### Requirement: Validação e importação de XML LexML
Para arquivos `XML`, o parser SHALL validar que a entrada é um XML bem-formado (com proteção contra DOCTYPE/XXE), pertence ao namespace `http://www.lexml.gov.br/1.0` com elemento raiz `LexML` e possui `<ProjetoNorma>` com `<Articulacao>` não vazia, antes de converter para Jsonix. No modo `APROVEITAMENTO`, se o atributo `URN` de `<Identificacao>` estiver ausente, vazio ou contiver `LEXML_URN_ID`, o parser SHALL normalizá-lo para uma URN válida, preservando intactos documentos que já possuam URN válida (inclusive `lexedit:Metadado`).

#### Scenario: XML LexML válido preservado intacto
- **WHEN** um XML LexML válido com URN preenchida e `lexedit:Metadado` é enviado ao `parseXml`
- **THEN** o XML é preservado sem alterações e convertido para Jsonix via `ConversorDocumentoArticulado`

#### Scenario: XML LexML com placeholder LEXML_URN_ID
- **WHEN** um XML LexML possui `LEXML_URN_ID` na URN e é parseado em modo `APROVEITAMENTO`
- **THEN** a URN é normalizada para o padrão aceito pelo `lexml-eta` antes da conversão para Jsonix

#### Scenario: XML inválido, com DOCTYPE ou sem articulação
- **WHEN** é enviado um XML malformado, com declaração `DOCTYPE`, de outro esquema (ex.: emenda) ou sem `<Articulacao>` válida
- **THEN** lança `EtaBackendException`

### Requirement: Tratamento de avisos de validação do parser
Os problemas não-fatais apontados por `Validation.validaEstrutura` (e `validaComSchema` quando habilitado) SHALL ser convertidos em objetos `AvisoParseProposicao` e retornados em `ResultadoParseProposicao`. Quando `falharComAvisos` estiver desabilitado (padrão), o Jsonix SHALL ser devolvido normalmente junto com os avisos; quando habilitado, qualquer aviso SHALL lançar `EtaBackendException`.

#### Scenario: Descontinuidade de numeração no modo tolerante vs estrito
- **WHEN** um texto possui `Art. 1º` seguido de `Art. 3º`
- **THEN** com `falharComAvisos` falso o resultado é devolvido com `temAvisos()` verdadeiro; com `falharComAvisos` verdadeiro lança `EtaBackendException`

### Requirement: Controle de uso do linker externo
O parser SHALL detectar automaticamente `/usr/local/bin/linkertool` quando `usarLinker` não for especificado em `OpcoesParseProposicao`, permitindo também forçar ou desabilitar explicitamente o uso do linker sem inicializar o `ActorSystem` quando desabilitado.

#### Scenario: Ambiente sem linkertool
- **WHEN** `/usr/local/bin/linkertool` não existe e `usarLinker` não foi forçado
- **THEN** o parseamento executa com `useLinker = false` sem inicializar processos de linker externos

### Requirement: Regexes customizados de epígrafe configuráveis pelo chamador
O `OpcoesParseProposicao` SHALL permitir informar listas adicionais de expressões regulares para início de epígrafe (`regexEpigrafeAdicionais`), continuação (`regexEpigrafeContinuacaoAdicionais`) e pós-epígrafe (`regexPosEpigrafeAdicionais`), e o `DocumentoArticuladoParser` SHALL oferecer construtor opcional que receba um `OpcoesParseProposicao` padrão para a instância. Os padrões adicionais SHALL ser validados no `build()` (lançando `EtaBackendException` se inválidos), normalizados automaticamente (remoção de diacríticos e correspondência case-insensitive Unicode contra o texto normalizado) e somados aos regexes do modo `APROVEITAMENTO` ou do `DocumentProfile` do modo `COMPLETO`.

#### Scenario: Padrão de epígrafe incomum informado em runtime
- **WHEN** um arquivo possui uma epígrafe fora dos padrões embutidos (ex.: `"INDICAÇÃO Nº 42, DE 2026"`) e esse padrão é informado via `OpcoesParseProposicao`
- **THEN** a epígrafe é reconhecida e isolada para não contaminar `<Ementa>`, sem necessidade de alteração no `eta-backend-services`

#### Scenario: Regex customizado com sintaxe inválida
- **WHEN** `OpcoesParseProposicao.builder()` recebe uma expressão regular sintaticamente inválida
- **THEN** o método `build()` lança `EtaBackendException` identificando o padrão inválido

