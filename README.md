
# Serviços para back-end do lexml-emenda e lexml-eta

Principais funcionalidades:
- Geração de PDF de emenda e parecere a partir do JSON e arquivos anexados;
- Recuperação do JSON de emenda e parecer a partir do PDF.

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

## Deploy

Fazer o deploy no repositório central usando o [central-publishing-maven-plugin](https://central.sonatype.org/publish/publish-portal-maven) de forma independente da gestão de releases do gitflow.

Lembrar que o ambiente local deve estar preparado para o deplo (settings.xml e chave pgp do desenvolvedor).

Gerar a release/tage pelo gitflow e depois fazer checkout da tage e rodar o comando abaixo.

`mvn -Pdeploy deploy`