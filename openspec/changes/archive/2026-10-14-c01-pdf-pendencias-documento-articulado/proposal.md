# Proposal

## Why

A issue [#79](https://github.com/lexml/eta-backend-services/issues/79) pede que o PDF da proposição apresente as **pendências** "a exemplo das emendas": avisos destacados, no início do documento, de que ele não deve ser protocolado enquanto houver marcas de revisão, comentários ou pendências. O editor (lexml-eta) registra esses dados no `lexedit:Metadado` (especificações 08, 09, 11 e 13 de `lexml-eta/docs/extensao-formato-lexml/`), e hoje o PDF não mostra nada disso.

## What Changes

- **Alertas no padrão da emenda** (trecho "Alertas" e "Pendências de Preenchimento" do `template-velocity-emenda.xml`), antes da epígrafe, na primeira página (depois do cabeçalho), com os textos e o estilo da emenda (fundo amarelo, texto vermelho, negrito, 12pt fixo):
  - **marcas de revisão**: "Este documento contém marcas de revisão e não deve ser protocolado até que estas sejam removidas.", quando o documento tiver revisões de texto (`lexedit:RevisoesTextuais`) ou da articulação (`lexedit:RevisoesArticulacao`);
  - **comentários**: "Este documento contém comentários e não deve ser protocolado até que sejam removidos.", quando houver algum comentário (`lexedit:Comentarios`);
  - **pendências de preenchimento**: "Este documento contém pendências de preenchimento e não deve ser protocolado até que estas sejam resolvidas:" seguido da lista de `lexedit:Pendencias`, **cada item como chegou**, quando houver pendências.
- As caixas aparecem pelos dados, como na emenda. A lista não é filtrada: um item pode repetir o aviso de uma caixa (ex.: "Remover comentários."); se necessário, isso será revisto depois.
- **Exemplo**: as pendências passam a ser as cinco previstas na especificação 13 do lexml-eta (justificação, remissões internas inválidas, comentários, revisão de texto e revisão na articulação), sem depender de coerência com o restante do documento; as demais fixtures ficam como estão.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `pdf-documento-articulado`: novo requisito "Alertas e pendências".

## Impact

- Nova leitura dos alertas a partir do JSON (no estilo do `FechoDocumentoArticulado`), usada pelo `DocumentoArticuladoTemplateProcessor`.
- `template-velocity-documento-articulado.xml`: os blocos "Alertas" e "Pendências de Preenchimento" da emenda, hoje comentados como "NÃO RETIRAR", ativados antes do `$conteudoFo`.
- Fixture `documento-articulado-exemplo`, testes (`DocumentoArticuladoTemplateProcessorTest`, `DocumentoArticuladoPdfGeneratorTest`, teste da nova leitura) e `CLAUDE.md`.
- Sem mudança de API pública nem de dependências; sem impacto em emenda, parecer ou `lexmljsonix`.
