# Tasks

## 1. Conversão de imagens para JPEG no RTF

- [x] 1.1 Em `DocumentoArticuladoFoRtf.substituirImagensNaoJpeg`, converter `fo:external-graphic` com `data:image/*;base64` legível para JPEG em memória, sobre fundo branco, trocando o `src` por `data:image/jpeg;base64`
- [x] 1.2 Manter imagens JPEG sem recodificar e manter o aviso vermelho para endereço externo, base64 inválido, imagem ilegível ou falha de escrita (sem exceção nem texto de erro no RTF)
- [x] 1.3 Ajustar `AVISO_IMAGEM`, que não deve mais dizer que o RTF só aceita JPEG, e registrar falhas no log sem o conteúdo da imagem

## 2. Testes automatizados (JUnit 5)

- [x] 2.1 Teste com PNG opaco (fixture `documento-com-justificacao`): o RTF contém a imagem e não contém o aviso
- [x] 2.2 Teste com PNG com transparência (gerado no teste): a conversão sai com fundo branco nas áreas transparentes
- [x] 2.3 Teste com outro formato legível (GIF ou BMP gerado no teste): o RTF contém a imagem
- [x] 2.4 Testes de fallback: imagem externa e dados ilegíveis geram o aviso vermelho, sem o conteúdo da imagem nem texto de erro
- [x] 2.5 Rodar `mvn test` sem rede nem `jsonix-lexml` e ajustar testes existentes que esperavam o aviso para PNG

## 3. Documentação

- [ ] 3.1 Atualizar o `CLAUDE.md` e `docs/api-documento-articulado.md` onde descrevem imagens no RTF (se houver menção)
