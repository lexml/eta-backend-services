# Proposal

## Why

A issue [#76](https://github.com/lexml/eta-backend-services/issues/76) pede que o PDF da proposição apresente **local, data e assinaturas**, hoje explicitamente não impressos (requisito "Conteúdo impresso"). A issue registra que a quantidade adicional de assinaturas de deputados e senadores será desativada, de modo que não há linhas em branco para assinatura manual. Só parlamentares (senadores e deputados) assinam; a autoria por comissão não é tratada nesta etapa, como no editor (issue 4 de `lexml-eta/docs/extensao-formato-lexml/issues.md`).

## What Changes

- **Local e data** (fecho) e **assinaturas** impressos **depois da justificação**, no padrão de estrutura e de estilo da emenda (`template-velocity-emenda.xml`), a partir dos dados estruturados do `lexedit:Metadado` (especificações 03 e 04 do lexml-eta): `local`, `data` e `Autoria` com os `Parlamentar`.
- Fecho: "`<local>`, `<data por extenso>`." com recuo de 2,5cm; **data vazia ou ausente** sai com espaços para preencher à mão ("`<local>`, ___ de ___ de ___."), como na emenda. Espaço antes de 34pt (26pt com "reduzir espaço entre linhas"), o `$spacing4` da emenda.
- Assinaturas: em duas colunas centralizadas, em negrito, 30pt antes de cada uma; com número ímpar, a última ocupa as duas colunas. Cada assinatura: tratamento + nome (Senador/Senadora para o Senado, Deputado/Deputada para a Câmara, pelo sexo), "(PARTIDO - UF)" quando `imprimirPartidoUF` e o cargo quando houver.
- **Fora**: assinaturas adicionais (tabelas em branco) e autoria por comissão (sem assinatura; o fecho é impresso se houver local).
- Documento sem local nem autoria de parlamentares (os documentos da #72): nada é impresso.
- A representação textual `ParteFinal` (`LocalDataFecho`/`AssinaturaTexto`) que o editor grava no LexML continua ignorada; fica registrada como alternativa para a atividade de formatação.
- **Exemplo** cobrindo todas as possibilidades: senadores e deputados dos dois sexos, com e sem cargo, número ímpar de assinaturas; as demais variações (data vazia, sem partido/UF, comissão, sem local) em outras fixtures ou em variações nos testes.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: o requisito "Conteúdo impresso" passa a incluir local, data e assinaturas depois da justificação; novos requisitos "Local e data" e "Assinaturas".

## Impact

- Nova leitura do local, data e autoria a partir do JSON (no estilo do `OpcoesImpressaoDocumentoArticulado`), usada pelo `DocumentoArticuladoTemplateProcessor`.
- `ParametrosImpressaoDocumentoArticulado`: espaço antes do fecho (34pt/26pt).
- `template-velocity-documento-articulado.xml`: blocos "Local e data" e "Assinaturas" adaptados do trecho da emenda que está comentado como "NÃO RETIRAR", depois do `$conteudoFo`; as assinaturas adicionais continuam comentadas.
- Fixtures (`documento-articulado-exemplo` e as da #75), testes (`DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoPdfGeneratorTest`, teste da nova leitura) e `CLAUDE.md`.
- Sem mudança de API pública nem de dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
