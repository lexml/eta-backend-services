# Design

## Context

O motivo está em proposal.md e os requisitos em `specs/pdf-documento-articulado/spec.md`. Estado atual relevante:

- **Alertas.** As três caixas ficam no `template-velocity-documento-articulado.xml`, com `font-size="12pt"` fixo. As flags vêm de `AlertasDocumentoArticulado` (`possuiRevisoes`, `possuiComentarios`, `getPendencias`), passadas ao contexto pelo `DocumentoArticuladoTemplateProcessor`.
- **Rótulo.** O `bloco-dispositivo` imprime o rótulo num `fo:inline` (negrito) seguido de um espaço, num bloco `text-align="justify"`.
  - Os espaços internos do rótulo ("Art.␣15.") são glue ajustável do FOP.
  - Conferido no FOP 2.7: `CharUtilities.isAdjustableSpace` devolve verdadeiro para U+0020 **e** U+00A0. Trocar por `&#160;` não resolve.
- **Epígrafe.** O template `lx:Epigrafe` faz `normalize-space(.)` e troca os espaços por `&#160;`.
  - O `normalize-space` colapsa os vãos internos. Por isso "PROJETO DE LEI Nº·····, DE", gravado assim pelo editor e preservado pelo `jsonix-lexml` nos dois sentidos (testado), sai "Nº , DE".
- **Parâmetros.** `ParametrosImpressaoDocumentoArticulado` já calcula os tamanhos por `tamanhoFonte` e os entrega ao template como strings com unidade.

## Goals / Non-Goals

**Goals:**
- Cada ajuste mexe só no seu ponto.
- A epígrafe não move nada abaixo dela.
- O rótulo não muda o texto de nenhum parágrafo nem o número de páginas. Isso é comprovado por comparação dos PDFs antes e depois. Quebras de linha diferentes nos parágrafos com rótulo foram aceitas (ver D2).

**Non-Goals:**
- Mudar a emenda, que mantém os 12pt fixos e as três caixas.
- Forçar espaço para o ano ausente na epígrafe ("..., DE" sem ano): o trim das pontas, pedido pela chefia, remove o que vier depois de "DE".
- Tratar rótulos fora dos dispositivos (agrupadores são centralizados, sem justificação).
- Regerar a `vFinal` de 48 PDFs: a nova avaliação usa só os 3 PDFs pedidos.

## Decisions

### D1. Alertas: remover as caixas e as flags

- Saem as duas caixas do template, os campos `possuiRevisoes` e `possuiComentarios` de `AlertasDocumentoArticulado` e as entradas correspondentes do contexto do Velocity.
- A classe fica só com as pendências.
- **Alternativa rejeitada:** manter as flags sem uso. É código morto, e o teste continuaria afirmando um comportamento que o PDF não tem.

### D2. Rótulo: espaço interno com largura fixa

Duas formas de tirar a elasticidade, decididas por medição na task 2.1:

- **(a) `word-spacing` de valor fixo no `fo:inline` do rótulo** (ex.: `word-spacing="0pt"`).
  - Hipótese: um valor único dá mínimo = ótimo = máximo à glue dos espaços daquele inline, e a justificação deixa de esticá-los.
  - Não está claro no código do FOP se a justificação ainda soma elasticidade a essa glue. Por isso, medir antes de adotar.
- **(b) `fo:leader` de comprimento fixo no lugar de cada espaço interno** (`leader-pattern="space"`, `leader-length` = largura natural do espaço da fonte), com o rótulo em `keep-together.within-line="always"` para não quebrar a linha dentro dele.
  - Um leader de comprimento único não estica.
  - Não usa nenhum caractere, então não há risco de glifo ausente no PDF/A.

**Critério:** adotar (a) se a medição com PDFBox mostrar a mesma distância "Art." → número em linhas com folgas diferentes. Senão, adotar (b).

**Alternativa rejeitada:** espaços Unicode de largura fixa (U+2002, U+202F etc.). O FOP não os ajusta, mas a fonte pode não ter o glifo, o que traz risco de `.notdef` no PDF/A.

**Adotada: (a) `word-spacing="0pt"`** (task 3.1). Medição no exemplo com fonte 14:

- antes, o espaço entre "Art." e o número variava de 2,79pt a 4,46pt;
- depois, fica em 3,49pt (a largura natural) em todos os artigos, e só o espaço depois do rótulo absorve a folga.

**Efeito nas quebras, aceito pelo usuário:** fixar a elasticidade muda a conta do algoritmo de quebra do FOP. Por isso, em alguns parágrafos com rótulo uma ou duas palavras trocam de linha.

- Na comparação, 16 dos 24 PDFs ficaram idênticos.
- Em 7, só houve palavras trocando de linha dentro do parágrafo, sem mudança vertical.
- Em 1 (`documento-com-capitulo-e-secao`, fonte 14), um parágrafo ganhou uma linha e o resto desceu uma linha, com o mesmo número de páginas.

Isso vale para qualquer forma de fixar o espaço, inclusive (b). O texto e a justificação continuam corretos.

**Quebra das linhas (análise feita antes da medição):** a largura natural do rótulo não muda. Só some a elasticidade de um ou dois espaços na primeira linha do dispositivo. O algoritmo de quebra do FOP pondera a elasticidade, então em tese uma linha muito apertada poderia quebrar diferente. A comparação da task 2.3 confirma no exemplo e nos documentos de teste; qualquer diferença é relatada.

### D3. Tamanho dos alertas: `tamanhoFonte − 2`

- Novo `getTamanhoFonteAlerta()` em `ParametrosImpressaoDocumentoArticulado` ("12pt", "14pt", "16pt"), passado ao contexto como `tamanhoFonteAlerta`.
- Com fonte 14, o PDF fica idêntico ao atual.
- **Alternativas rejeitadas:**
  - igual ao texto: o alerta competiria com a epígrafe;
  - proporção 12/14: dá tamanhos fracionários sem ganho visual.

### D4. Epígrafe: trim só nas pontas e vão do número com mínimo de 8 espaços

Tudo dentro do template `lx:Epigrafe`, em XSLT 1.0:

1. `translate(., '&#10;&#13;&#9;', '   ')`: quebras de linha e tabulações viram espaço.
2. Trim das pontas com o `aparar-fim`, que já existe, e um novo `aparar-inicio`, ambos recursivos.
3. Vão do número: se o texto contém "Nº" (ou "N°"), depois dele existe uma vírgula e o que fica entre os dois tem `normalize-space` vazio, o vão é mantido como gravado quando tem 8 espaços ou mais e completado para 8 quando tem menos.
   - Só a primeira ocorrência é considerada.
   - Com um número entre "Nº" e a vírgula, nada muda.
4. `translate(..., ' ', '&#160;')`, como hoje: uma linha só, sem colapso.

- O teste de "vazio" continua sendo `normalize-space(.) != ''` sobre o texto original.
- A epígrafe é centralizada, então o `&#160;` ajustável não tem efeito.
- O bloco continua com uma linha e a mesma altura, por isso nada abaixo se move.
- **8 espaços:** é a quantidade que a emenda usa nos campos em branco do fecho e fica perto do exemplo da chefia (7).
- **Mínimo de 8, respeitando o editor (decisão do usuário, depois da primeira versão com vão fixo):**
  - o pedido é "vamos gravar espaços no lugar do número": quem decide a largura é o editor;
  - e também "force o espaçamento caso não apareça": o mínimo garante um vão visível mesmo com 1 espaço ou nenhum.
- **Alternativas rejeitadas:**
  - vão sempre de 8 (primeira versão): ignorava um vão maior gravado pelo editor;
  - largura pelo número da URN: no projeto sem número a URN tem um marcador (ex.: `999999;9999`), não o número real, e o PDF imprime o texto da epígrafe, não a URN.

## Risks / Trade-offs

- **[D2 (a) não tirar a elasticidade]** → a medição da task 2.1 decide; (b) é a alternativa já desenhada.
- **[D2 mudar a quebra de alguma linha]** → comparação antes e depois em todos os documentos de teste. Se mudar, relatar e decidir com o usuário.
- **[D4 com epígrafe longa, com o vão do número, passar da largura da página]** → os `&#160;` impedem a quebra, como já acontece hoje. As epígrafes reais são curtas e o vão soma cerca de 2em; o teste da XSLT cobre o texto gerado.
- **[D1 documento com revisões sem a pendência correspondente registrada]** → nenhum aviso é impresso. É uma decisão da chefia: a lista do editor é a fonte dos avisos.
