# Proposal

## Why

Depois da parte 1 da issue [#72](https://github.com/lexml/eta-backend-services/issues/72) (change `2026-09-30-c01-pdf-parte-inicial-documento-articulado`), o PDF da proposição imprime epígrafe, ementa e preâmbulo, mas ainda não imprime nenhum dispositivo. Esta é a **parte 2** das 6 em que a #72 foi dividida: imprimir a **articulação básica** (artigo, caput, parágrafo, inciso, alínea e item) com a formatação da citação de dispositivos da emenda. É a base que as partes 3 a 6 (agrupadores, alteração de norma, remissões, pena e título de dispositivo) vão completar.

## What Changes

- O PDF passa a imprimir, depois do preâmbulo e na ordem do documento, os dispositivos **Artigo, Caput, Parágrafo, Inciso, Alínea e Item**:
  - rótulo em negrito seguido do texto no mesmo bloco. No artigo, o rótulo do artigo sai junto com o texto do caput ("**Art. 1º** Esta Lei ...");
  - formatação da citação de dispositivos da emenda: justificado, recuo de primeira linha de 2,5cm, entrelinha de 150% e **sem espaço extra entre dispositivos**;
  - negrito e itálico preservados no texto; referências com link (`span`, `Remissao`) saem como texto simples (links na parte 5);
  - sem as aspas que envolvem a citação na emenda.
- Artigos dentro de **agrupadores** (Parte, Livro, Título, Capítulo, Seção, Subseção) são impressos. O rótulo e o nome dos agrupadores ainda não são impressos (parte 3).
- Continuam **sem impressão** nesta etapa: blocos de alteração de norma vigente e omissis (parte 4), pena e título de dispositivo (parte 6), além de justificação, local e data e assinaturas.
- O requisito "Impressão da parte inicial", que proibia imprimir a articulação, é substituído por um requisito de conteúdo impresso que abrange a parte inicial e a articulação.

## Capabilities

### New Capabilities
<!-- Nenhuma: evolui a capability existente. -->

### Modified Capabilities
- `pdf-documento-articulado`: o PDF passa a imprimir os dispositivos da articulação (artigo, caput, parágrafo, inciso, alínea e item), atravessando agrupadores. O requisito "Impressão da parte inicial" é substituído por "Conteúdo impresso".

## Impact

- **Código:** só a XSLT `src/main/resources/documentoarticulado/documento-articulado-conteudo.xsl`, com novos templates para a articulação e templates vazios explícitos para o que fica para as partes 3, 4 e 6. O Java, o template Velocity e a API não mudam.
- **Testes:** novos casos no `DocumentoArticuladoConteudoTransformerTest` (XML → FO) e no `DocumentoArticuladoPdfGeneratorTest` (texto extraído do PDF), com os três documentos da #72 já presentes como fixtures. Os testes da parte 1 que exigem "sem texto de dispositivos" precisam ser ajustados.
- **Nenhuma alteração** em emenda, parecer ou `lexmljsonix`; nenhuma dependência nova.
- **PDFs de referência:** nova versão **v2** em `pdf-documento-articulado/pdfs-gerados/` (pasta local, não versionada), revalidada no veraPDF. Agora os PDFs podem ter mais de uma página.
