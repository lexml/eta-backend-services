
# Serviços de back-end para os componentes lexml-emenda, lexml-eta e lexml-parecer

Principais funcionalidades:
- Geração de PDF de emenda e parecere a partir do JSON e arquivos anexados;
- Recuperação do JSON de emenda e parecer a partir do PDF;
- Listagem de proposições para emendamento;
- Recuperação do texto da proposição no formato jsonix para emendamento.

Um dos serviços faz a conversão de textos de proposições no formato LexML para o formato jsonix recorendo ao conversor jsonix-lexml.

Maiores informações sobre o conversor jsonix-lexml podem ser encontradas em [https://github.com/lexml/jsonix-lexml](https://github.com/lexml/jsonix-lexml#jsonix-lexml).

## Como usar

Configurar a propriedade `jsonix.cli` no arquivo `application.properties` ou `application.yaml` da sua aplicação Java, informando a localização do executável do conversor lexml-jsonix.

Exemplo:

```yaml
lexml-jsonix.cli=c:\\temp\\jsonix-lexml-win.exe
```

  Releases dos executáveis para os três ambientes suportados (Linux, MacOS X e Windows) podem ser encontrados em [https://github.com/lexml/jsonix-lexml/releases/](https://github.com/lexml/jsonix-lexml/releases/).

### Para converter de xml para json e vice-versa

Instanciar um bean que implementa a interface `ConversorLexmlJsonix`. Essa interface define os métodos para conversão de um texto em formato Lexml para json e vice-versa. A classe `ConversorLexmlJsonixImpl` implementa essa interface e utiliza o executável mencionado no item anterior para realizar a conversão. Exemplo de uso:

```java
    package com.lexmljsonixteste.jsonix;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import br.gov.lexml.eta.lexmljsonix.conversor.ConversorLexmlJsonix;

    @RestController
    @RequestMapping("lexml-jsonix")
    public class ConversorController {
        
        @Autowired
        private ConversorLexmlJsonix conversorLexmlJsonix;
        
        @RequestMapping(value = "xml-to-json", produces = MediaType.APPLICATION_JSON_VALUE)
        public String xmlToJson(@RequestBody String xml) {
            return conversorLexmlJsonix.xmlToJson(xml);
        }	
        
        @RequestMapping(value = "json-to-xml", produces = MediaType.APPLICATION_XML_VALUE)
        public String jsonToXml(@RequestBody String json) {
            return conversorLexmlJsonix.jsonToXml(json);
        }	
        
    }
```

### Para pesquisar proposições e obter texto em formato LexML

A classe `LexmlJsonixServiceImpl` implementa a interface `LexmlJsonixService` e disponibiliza métodos para listar proposições, bem como método para obter o texto de uma proposição em formato Lexml. Exemplo de uso:

```java
    package com.lexmljsonixteste.lex;

    import java.util.List;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.CrossOrigin;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    import br.gov.lexml.eta.lexmljsonix.service.LexmlJsonixService;
    import br.gov.lexml.eta.lexmljsonix.service.Proposicao;

    @RestController
    @CrossOrigin(origins = "*")
    public class ProposicoesController {
        
        @Autowired
        private LexmlJsonixService lexmlJsonixService;
            
        @GetMapping("proposicoes")
        public List<Proposicao> getProposicoes(@RequestParam String sigla, @RequestParam Integer ano, String numero) {
            return lexmlJsonixService.getProposicoes(sigla, ano, numero);
        }

        @GetMapping("proposicao")
        public Proposicao getProposicao(@RequestParam String sigla, @RequestParam Integer ano, @RequestParam String numero) {
            return lexmlJsonixService.getProposicao(sigla, ano, numero);
        }
        
        @RequestMapping(value = "proposicao/texto-lexml/xml", produces = MediaType.APPLICATION_XML_VALUE)
        public String getTextoProposicaoAsXml(@RequestParam String sigla, @RequestParam Integer ano, @RequestParam String numero) {
            return lexmlJsonixService.getTextoProposicaoAsXml(sigla, ano, numero);
        }	

        @RequestMapping(value = "proposicao/texto-lexml/xml/sdleg", produces = MediaType.APPLICATION_XML_VALUE)
        public String getTextoProposicaoAsXml(@RequestParam String idSdlegDocumentoItemDigital) {
            return lexmlJsonixService.getTextoProposicaoAsXml(idSdlegDocumentoItemDigital);
        }	
                
        @RequestMapping(value = "proposicao/texto-lexml/json", produces = MediaType.APPLICATION_JSON_VALUE)
        public String getTextoProposicaoAsJson(@RequestParam String sigla, @RequestParam Integer ano, @RequestParam String numero) {
            return lexmlJsonixService.getTextoProposicaoAsJson(sigla, ano, numero);
        }	

        @RequestMapping(value = "proposicao/texto-lexml/json/sdleg", produces = MediaType.APPLICATION_JSON_VALUE)
        public String getTextoProposicaoAsJson(@RequestParam String idSdlegDocumentoItemDigital) {
            return lexmlJsonixService.getTextoProposicaoAsJson(idSdlegDocumentoItemDigital);
        }	
                
    }
```

A classe `LexmlJsonixServiceImpl` utiliza um serviço disponibilizado pelo Senado Federal para realizar a pesquisa de proposições e para obter o texto em formato Lexml da proposição.
- A url para pesquisar proposições é: https://legis.senado.gov.br/legis/resources/lex/proposicoes
- A url para obter o arquivo zip que contém o texto em formato lexml da proposição é: https://legis.senado.gov.br/sdleg-getter/documento/download

Essas urls podem ser alteradas a partir do arquivo `application.properties` da sua aplicação, conforme o exemplo abaixo:

```yaml
lexml-jsonix.url-proposicoes=http://nova-url-do-servico-de-pesquisa-de-proposicoes
lexml-jsonix.url-sdleg=http://nova-url-do-servico-que-retorna-texto-lexml
```

**Observação:** As urls acima podem ser alteradas, mas o formato do retorno deve permanecer o mesmo para que a classe `LexmlJsonixServiceImpl` funcione corretamente. Caso você possua outros serviços que retornem dados equivalentes, você poderá criar uma nova classe que implementa a interface `LexmlJsonixService` e construir o código para tratar os dados retornados nos novos serviços.

## Fluxo Gitflow

Está configurado o plugin [gitflow-maven-plugin](https://github.com/aleksandr-m/gitflow-maven-plugin) para gestão das branches gitflow.

**Goals**

- gitflow:release-start - Starts a release branch and updates version(s) to release version.
- gitflow:release-finish - Merges a release branch and updates version(s) to next development version.
- gitflow:release - Releases project w/o creating a release branch.
- gitflow:feature-start - Starts a feature branch and optionally updates version(s).
- gitflow:feature-finish - Merges a feature branch.
- gitflow:hotfix-start - Starts a hotfix branch and updates version(s) to hotfix version.
- gitflow:hotfix-finish - Merges a hotfix branch.
- gitflow:support-start - Starts a support branch from the production tag.
- gitflow:version-update - Updates version in release or support branch, optionally tagging and pushing it to the remote repository.
- gitflow:help - Displays help information.

## Deploy no repositório do Senado

É possível fazer o deploy no repositório Maven do Senado sincronizando os fontes com o git interno e comandando o deploy via Jenkins.

## Deploy no repositório Maven Central

Fazer o deploy no repositório central usando o [central-publishing-maven-plugin](https://central.sonatype.org/publish/publish-portal-maven) de forma independente da gestão de releases do gitflow.

Lembrar que o ambiente local deve estar preparado para o deplo (settings.xml e chave gpg do desenvolvedor).

Gerar a release/tag pelo gitflow e depois fazer checkout da tage e rodar o comando abaixo.

`mvn -Pdeploy deploy`
