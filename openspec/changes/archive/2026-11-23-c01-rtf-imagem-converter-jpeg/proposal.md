# Proposal

## Why

O renderer RTF do FOP só aceita imagens JPEG. Hoje, uma imagem PNG (ou de outro formato) na justificação é substituída, no RTF do documento articulado, por um aviso vermelho de "imagem omitida". O usuário perde a imagem num formato muito comum no editor, mesmo ela sendo legível e convertível.

## What Changes

- No RTF, imagens `data:image/*` em qualquer formato que o `ImageIO` consiga ler (PNG, GIF, BMP etc.) passam a ser convertidas em memória para JPEG e incluídas no documento, no lugar do aviso.
- A conversão desenha a imagem sobre fundo branco, porque o JPEG não tem canal alfa e o RTF é um documento de papel.
- O aviso vermelho de imagem omitida continua para o que não pode ser convertido: imagem com endereço externo (não `data:`) ou dados ilegíveis. O RTF continua sem conter o conteúdo da imagem nem mensagens de erro como texto.
- O PDF não muda.

## Capabilities

### New Capabilities

### Modified Capabilities
- `rtf-documento-articulado`: o requisito "Imagens no RTF" passa a incluir imagens não JPEG convertidas para JPEG, mantendo o aviso só para imagens externas ou ilegíveis.

## Impact

- Código: `DocumentoArticuladoFoRtf` (substituição de imagens não JPEG, ajuste do FO para o RTF) e o texto do aviso `AVISO_IMAGEM`.
- Testes: `DocumentoArticuladoFoRtf`/geração de RTF, usando as imagens PNG já presentes na fixture `documento-com-justificacao`.
- Sem novas dependências (usa `javax.imageio` do JDK) nem mudança de API pública.
