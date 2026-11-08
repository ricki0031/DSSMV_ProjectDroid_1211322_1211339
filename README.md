
# SpotifyClone — Aplicação Android

Projeto desenvolvido no âmbito da unidade curricular de Desenvolvimento de Software para Sistemas Móveis.

## Descrição

O SpotifyClone é uma aplicação Android desenvolvida em Java que utiliza a Spotify Web API para permitir ao utilizador explorar músicas, artistas, álbuns e playlists, bem como controlar a reprodução no Spotify.

## Funcionalidades

- Autenticação com conta Spotify através de OAuth 2.0 com PKCE.
- Página inicial com recomendações e conteúdos recentes.
- Pesquisa de músicas, artistas, álbuns e playlists.
- Exploração de música por géneros.
- Biblioteca com playlists, álbuns guardados e músicas favoritas.
- Alternância entre visualização em lista e grelha.
- Criação de playlists.
- Ecrã de reprodução com controlos de música e fila de reprodução.
- Miniplayer para acesso rápido aos controlos.
- Deteção de ausência de ligação à Internet.
- Interface em português.

## Tecnologias utilizadas

- Java
- Android SDK
- Gradle (Kotlin DSL)
- Spotify Web API
- OkHttp
- Gson
- Glide

## Requisitos

- Android Studio ou IntelliJ IDEA com Android SDK configurado.
- Dispositivo Android ou emulador compatível.
- Ligação à Internet.
- Conta Spotify.
- Conta Spotify Premium e dispositivo de reprodução Spotify ativo para funcionalidades de controlo da reprodução.

## Configuração

1. Clonar o repositório e abrir o projeto no Android Studio ou IntelliJ IDEA.
2. Aguardar a sincronização do Gradle.
3. Criar uma aplicação no Spotify Developer Dashboard:
   https://developer.spotify.com/dashboard
4. Configurar o seguinte Redirect URI na aplicação Spotify:

   spotifyclone://callback

5. Adicionar as seguintes propriedades ao ficheiro `local.properties`, na raiz do projeto:

   SPOTIFY_CLIENT_ID=COLOCAR_AQUI_O_CLIENT_ID
   REDIRECT_URI=spotifyclone://callback

6. Substituir `COLOCAR_AQUI_O_CLIENT_ID` pelo Client ID da aplicação criada no Spotify Developer Dashboard.
7. Compilar e executar a aplicação num dispositivo Android.

**Nota:** O ficheiro `local.properties` contém configurações locais e não deve ser incluído no repositório Git.

## Utilização

1. Abrir a aplicação.
2. Iniciar sessão com uma conta Spotify autorizada na aplicação criada no Spotify Developer Dashboard.
3. Explorar conteúdos através dos separadores Início, Pesquisar e Biblioteca.
4. Para controlar a reprodução, iniciar o Spotify num dispositivo compatível e manter uma sessão ativa.

## Observações

A reprodução é controlada através da Spotify Web API e depende das permissões, limitações e disponibilidade do serviço Spotify.

O projeto destina-se a fins académicos.
