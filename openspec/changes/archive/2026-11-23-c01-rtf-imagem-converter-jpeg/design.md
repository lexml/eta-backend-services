# Design

## Context

O RTF do documento articulado é gerado pelo FOP a partir do XSL-FO, adaptado por `DocumentoArticuladoFoRtf.adaptar` (ver proposal.md - Why). Hoje `substituirImagensNaoJpeg` troca todo `fo:external-graphic` cujo `src` não é `data:image/jpeg` por um `fo:inline` vermelho com `AVISO_IMAGEM`, porque o renderer RTF joga exceção e escreve o base64 como texto com outros formatos. A XSLT já entrega a imagem como `url('data:image/...;base64,...')`, sem arquivo temporário.

## Goals / Non-Goals

**Goals:**
- Converter imagens `data:` legíveis para JPEG antes de o FOP renderizar o RTF.
- Manter o aviso vermelho como fallback seguro.

**Non-Goals:**
- Alterar o PDF, a XSLT ou o template Velocity.
- Buscar imagens externas (http/arquivo) para convertê-las.
- Reutilizar `VelocityExtension.trataImagens`, do fluxo de emenda (o fluxo do documento articulado é isolado).

## Decisions

- **Conversão no FO, em memória.** Em `substituirImagensNaoJpeg`, para cada `fo:external-graphic` não JPEG com `data:image/*;base64`, decodificar, ler com `ImageIO.read` e regravar como JPEG, trocando o `src` por `url('data:image/jpeg;base64,...')`. Alternativa descartada: arquivo temporário (como o fluxo de emenda), por exigir limpeza e não ser necessário, já que o FOP aceita `data:`.
- **Fundo branco.** A imagem é desenhada sobre um `BufferedImage` `TYPE_INT_RGB` preenchido de branco, porque o JPEG não tem alfa e `ImageIO` falha ou sai preto com ARGB.
- **Qualquer formato legível.** Não filtrar por PNG: o `ImageIO` do JDK lê PNG, GIF, BMP e JPEG. `ImageIO.read` devolvendo `null` (formato sem leitor) ou exceção cai no aviso.
- **Fallback.** Endereço não `data:`, base64 inválido, imagem ilegível ou falha de escrita mantêm o `fo:inline` vermelho existente, sem propagar exceção nem texto de erro. Falhas são registradas no log, sem o conteúdo da imagem.
- **JPEG original intacto.** Imagens já JPEG não são relidas nem recodificadas, para não perder qualidade.
- **Texto do aviso.** `AVISO_IMAGEM` deixa de dizer que "o RTF só aceita imagens JPEG", já que o aviso passa a valer para imagens externas ou ilegíveis; o texto novo é definido na implementação e coberto pelos testes.

## Risks / Trade-offs

- [Perda de qualidade por compressão JPEG em imagens com texto ou linhas finas] → Qualidade de gravação alta (ex.: 0,9) em vez do padrão do `ImageIO`.
- [Imagem grande aumenta o RTF e o uso de memória] → Aceito: a imagem já está inteira em memória no JSON e a conversão é pontual por imagem.
- [Formato sem leitor no JDK (ex.: WebP, ICO) vira aviso] → Comportamento esperado, igual ao atual.
