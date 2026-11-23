# Spec Delta

## MODIFIED Requirements

### Requirement: Imagens no RTF
O RTF SHALL incluir as imagens da justificação embutidas no documento (`data:image/*`) que puderem ser lidas, convertendo-as para JPEG quando necessário e apresentando-as sobre fundo branco, sem transparência. Uma imagem que não puder ser incluída (endereço externo ou dados ilegíveis) MUST ser substituída, no mesmo lugar, por um aviso em vermelho informando que foi omitida, e o RTF MUST NOT conter o conteúdo da imagem nem mensagens de erro como texto.

#### Scenario: Imagem JPEG
- **WHEN** a justificação tem uma imagem JPEG
- **THEN** o RTF contém a imagem

#### Scenario: Imagem PNG
- **WHEN** a justificação tem uma imagem PNG
- **THEN** o RTF contém a imagem convertida para JPEG, e não contém o aviso de imagem omitida

#### Scenario: Imagem PNG com transparência
- **WHEN** a justificação tem uma imagem PNG com áreas transparentes
- **THEN** o RTF contém a imagem com o fundo branco nessas áreas

#### Scenario: Imagem de outro formato legível
- **WHEN** a justificação tem uma imagem embutida em outro formato legível, como GIF ou BMP
- **THEN** o RTF contém a imagem convertida para JPEG, e não contém o aviso de imagem omitida

#### Scenario: Imagem externa
- **WHEN** a justificação tem uma imagem cujo endereço não é embutido no documento
- **THEN** o RTF contém, no lugar da imagem, um aviso em vermelho de imagem omitida, e não contém o endereço nem texto de erro

#### Scenario: Imagem ilegível
- **WHEN** a justificação tem uma imagem embutida cujos dados não podem ser lidos como imagem
- **THEN** o RTF contém, no lugar da imagem, um aviso em vermelho de imagem omitida, e não contém o conteúdo da imagem nem texto de erro
