
# Aplicação Spring com serviços gRPC e autenticação JWT

O gRPC (Google Remote Procedure Call) é um framework de código aberto usado para conectar sistemas de maneira eficiente, seja através da internet ou em data centers. Seu uso se dá em aplicações web, mobile, dispositivos externos e onde mais precisar.
Seu desenvolvimento acontece de maneira similar ao SOAP, tendo um arquivo modelo para os serviços, requisições e respostas, e toda a geração de código é feita através do Protocol Buffer, permitindo que o desenvolvedor foque apenas nas regras de negócio.
Veremos abaixo comparações de desempenho entre o gRPC e outros modelos, como o REST e o SOAP. Spoiler: gRPC é muito mais rápido.


## Sumário

1. [Visão geral](#viso-geral)
2. [Por que usar gRPC?](#por-que-usar-grpc)
3. [Desenvolvimento entre sistemas (cliente-servidor)](#desenvolvimento-entre-sistemas-cliente-servidor)
4. [Como funciona a interação entre sistemas?](#como-funciona-a-interação-entre-sistemas)
5. [Protocol Buffers](#protocol-buffers)
6. [Tipos de serviço](#tipos-de-serviço)
   - [Envio único, resposta única (unário)](#envio-único-resposta-única-unário)
   - [Envio único, sequência de respostas](#--envio-único-sequência-de-respostas)
   - [Sequência de envios, única resposta](#--sequência-de-envios-única-resposta)
   - [Sequência de envios, sequência de respostas](#--sequência-de-envios-sequência-de-respostas)
7. [Tempo limite, cancelamento e finalização](#tempo-limite-cancelamento-e-finalização)
8. [Metadados](#metadados)
9. [Canais](#canais)
10. [Na prática](#na-prática)
    - [Depencências](#dependências)
    - [Banco de dados](#banco-de-dados)
    - [Classes do projeto](#classes-do-projeto)
    - [Arquivos *.proto](#arquivos-proto)
    - [Geração das classes de gRPC](#gerao-das-classes-de-grpc)
    - [Implementando a lógica de negócio nos serviços gRPC](#implementando-a-lógica-de-negócio-nos-serviços-grpc)
    - [Segurança com JWT](#segurança-com-jwt)
    - [Criando um cliente para testes](#criando-um-cliente-para-testes)

### Visão geral

Ao iniciar o desenvolvimento de uma aplicação gRPC, o desenvolvedor cria um arquivo .proto especificando os serviços (endpoints) que o sistema responderá, bem como o modelo das requisições esperadas pelos serviços e as respostas enviadas.
Tendo o modelo pronto, já é possível gerar os arquivos na linguagem que você quiser através do terminal ou mesmo através de um gerenciador de dependências, como o Maven. Ao gerar todos os arquivos, o sistema já está pronto para começar a ter suas regras de negócio desenvolvidas.

### Por que usar gRPC?

- Baixa latência, alta escalabilidade, sistemas distribuídos;
- Permite desenvolvimento em aplicações mobile;
- Preciso, eficiente e independente de linguagens de programação;
- Permite autenticação, balanceamento de carga, logs, monitoramento e mais.

### Desenvolvimento entre sistemas (cliente-servidor)

Assumindo que o sistema será utilizado por outros sistemas, para que outro time desenvolva uma aplicação cliente, basta compartilhar o arquivo .proto e os mesmos poderão gerar todos os arquivos com base no mesmo, sem dor de cabeça.
A geração de arquivos com base nos Protocol Buffers funciona em diversas linguagens, então é perfeitamente possível criar um servidor em Java por exemplo e ter uma aplicação PHP, Go, Python ou outra linguagem qualquer como cliente.

### Como funciona a interação entre sistemas?

A interação entre sistemas funciona através de métodos, como se estivesse em uma aplicação só.
Ao chamar um método do servidor na aplicação cliente, o sistema automaticamente faz a requisição via HTTP/2, fazendo com que o servidor responda de acordo.
No lado do servidor, o desenvolvedor implementa métodos declarados pelos serviços e roda um servidor gRPC para responder as requisições dos clientes.
No lado do cliente, o código gerado possui um objeto chamado de stub (também conhecido como client em algumas linguagens) que implementa os mesmos métodos que o serviço.

### Protocol Buffers

Por padrão, o GRPC usa o chamado Protocol Buffers (Protobuf para os íntimos). O Protobuf é um mecanismo de serialização de dados onde você define os serviços e objetos envolvidos na aplicação servidor e cliente.
Na definição de serviços, temos o seguinte formato:

```protobuf
// Declaração de um serviço
service Greeter {
 // Método do serviço que poderá ser chamado pela aplicação cliente
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// Definição do formato de mensagem que será esperado pelo método "SayHello"
message HelloRequest {
  // Campo esperado dentro da requisição
  string name = 1;
}

// Definição do formato de retorno que o servidor
// enviará ao finalizar a chamada do método "SayHello"
message HelloReply {
  // Campo retornado pelo servidor para o cliente
  string message = 1;
}
```
### Tipos de serviço

O gRPC permite especificar quatro tipos de serviços:

#### Envio único, resposta única (unário):
O cliente envia uma única requisição para o servidor e o servidor retorna uma única resposta.
```protobuf
rpc SayHello(HelloRequest) returns (HelloResponse);
```    
Quando o cliente chama o método stub, o servidor é notificado que o RPC foi invocado, enviando os metadados do clientes para identificação. O servidor então ou envia seus dados antes da resposta ou então espera pela requisição do cliente. O que acontece primeiro depende da aplicação. O servidor então recebe a mensagem, realiza as regras de negócio definidas pelo desenvolvedor e envia uma resposta para o cliente, seja ela de sucesso (contendo os dados definidos na resposta e um status code OK) ou então de erro, enviando algum código de retorno especificando o tipo de erro ocorrido.
___
#### - Envio único, sequência de respostas:
O cliente envia uma única requisição e o servidor retorna uma sequência (stream) de respostas. O cliente lê a sequência de respostas até a sequência acabar.
```protobuf
rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);
```    
Similar ao tipo unário, há também uma única requisição, mas com uma sequência de respostas. O servidor envia todos os dados conforme implementado na regra de negócio pelo desenvolvedor e então envia um código de retorno (status code), significando o fim da sequência de mensagens.
___
#### - Sequência de envios, única resposta:
O cliente envia uma sequência (stream) de requisições e o servidor retorna uma única resposta.
```protobuf
rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);
```
Também similar ao tipo unário, mas aqui temos uma sequência de envios e uma única resposta. O servidor envia uma única mensagem de retorno com o status code, geralmente depois de receber todas as mensagens.
___
#### - Sequência de envios, sequência de respostas:
Tanto o cliente quanto o servidor atuam enviando e recebendo sequências de mensagens.
```protobuf
rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);
````
Aqui temos o envio e recebimento de sequências de mensagens, mas de maneira independente. O cliente inicia a conexão e o servidor escolhe já enviar suas mensagens ou esperar pelo início da sequência de mensagens do cliente. Como as duas sequências de mensagens são independentes, o cliente e o servidor podem ler/escrever mensagens em qualquer ordem. O servidor pode esperar até que todas as mensagens tenham sido enviadas pelo cliente, por exemplo.
___
### Tempo limite, cancelamento e finalização

O gRPC permite que clientes especifiquem quanto tempo eles querem esperar um RPC completar até que aconteça um erro do tipo DEADLINE_EXCEEDED. Dependendo da linguagem, o tempo limite é especificação através de uma duração (ex: 5 segundos), enquanto em outras é especificado através de um ponto fixo no tempo (data/hora).
Também é possível cancelar um RPC a qualquer momento, seja no lado do cliente ou do servidor. Ao fazer isso, mudanças feitas antes do cancelamento não são desfeitas, então cuidado ao cancelar um RPC em andamento.
Além disso, é possível ocorrer divergências na finalização de RPCs, onde o servidor pode retornar um sucesso dizendo que ele enviou as mensagens conforme esperado, mas no lado do cliente essas mensagens podem chegar depois do tempo limite, divergindo no sucesso/erro da requisição.

### Metadados

Os metadados são informações sobre uma chamada específica de RPC no formato de lista chave-valor, onde as chaves geralmente são strings e os valores podem ser dados binários. Um exemplo de dados seriam as informações de autenticação.
### Canais

Um canal gRPC provê uma conexão com um servidor em um host e porta específicos. É utilizado na hora criar um cliente gRPC.
## Na prática

### Dependências
Para entendermos melhor, vamos observar o [pom.xml](./pom.xml) da aplicação:
- Nas linhas 23~47, adicionamos as dependências do Spring;
- Nas linhas 49~64, adicionamos as dependências relacionadas à segurança do sistema, permitindo que configuremos os serviços gRPC para serem validados de acordo com as roles do usuário logado, que por sua vez são informadas via token JWT;
- Nas linhas 66~77, temos as dependências utilizadas pelo Maven para gerar as classes do gRPC de acordo com os arquivos .proto; 
- Nas linhas 79~94, temos as dependências do banco de dados PostgreSQL, commons-lang3 e testes unitários;
- Nas linhas 97~129, temos as configurações de build, onde informamos ao Maven que queremos buildar todo o sistema, incluindo a geração das classes Java a partir dos arquivos [usuario.proto](./src/main/proto/usuario.proto) e [login.proto](./src/main/proto/login.proto);
- Nas linhas 131~166, temos a declaração de alguns repositórios utilizados pelo Spring.

### Banco de dados
Para conectarmos no banco de dados, adicionamos algumas propriedades no arquivo [application.properties](./resources/application.properties):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/java_grpc
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```
Essas configurações nos conectam ao banco de dados de nome java_grpc (se não existir ainda, crie), que é onde vamos armazenar os dados de usuários criados via gRPC.

### Classes do projeto
Ao observarmos o pacote [scheper.mateus](src/main/java/scheper/mateus), vemos alguns diretórios e suas classes:
- [builder](src/main/java/scheper/mateus/builder): contém a classe responsável por auxiliar a criação do objeto [Usuario](src/main/java/scheper/mateus/entity/Usuario.java) do banco de dados;
- [config](src/main/java/scheper/mateus/config): contém a classe de configuração do Spring;
- [dto](src/main/java/scheper/mateus/dto): contém os DTOs que utilizaremos nas requisições REST;
- [entity](src/main/java/scheper/mateus/entity): contém a classe que representa o usuário no banco de dados, bem como a classe representante das funções (roles) do usuário;
- [exception](src/main/java/scheper/mateus/exception): contém as classes de exceções e seus tratamentos;
- [grpc](src/main/java/scheper/mateus/grpc): contém a implementação do serviço de gRPC no sistema;
- [repository](src/main/java/scheper/mateus/repository): contém a interface do repositório de banco de dados ([UsuarioRepository](src/main/java/scheper/mateus/repository/UsuarioRepository.java)) de usuário e sua implementação ([UsuarioRepositoryImpl](src/main/java/scheper/mateus/repository/UsuarioRepositoryImpl.java)). Também temos o [RoleRepository](src/main/java/scheper/mateus/repository/RoleRepository.java), que apenas extende o JpaRepository;
- [rest](src/main/java/scheper/mateus/rest): contém o controller [UsuarioController](src/main/java/scheper/mateus/rest/UsuarioController.java), responsável por responder às requisições REST que faremos para simular um cliente gRPC (veremos mais abaixo);
- [service](src/main/java/scheper/mateus/service): contém a classe de serviço [UsuarioService](src/main/java/scheper/mateus/service/UsuarioService.java) responsável por realizar as regras de negócio do sistema, bem como invocar o repositório de usuário para realizar operações de banco. Também temos a classe [LoginService](src/main/java/scheper/mateus/service/LoginService.java), responsável por gerar o token JWT;
- [utils](src/main/java/scheper/mateus/utils): contém a classe de utilizade [ConstantUtils](src/main/java/scheper/mateus/utils/ConstantUtils.java), responsável por manter constantes do sistema para serem utilizadas onde precisarmos.

### Arquivos *.proto
Observando o arquivo [usuario.proto](src/main/proto/usuario.proto), vemos:
- Linha 1: informamos para o Protobuf que estamos utilizando a versão 3. Caso não informado, o padrão é a versão 2;
- Linha 3: definimos que queremos criar múltiplos arquivos em vez de um só;
- Linha 5: definimos o nome do pacote como "grpc";
- Linha 7: declaramos um serviço de gRPC chamado UsuarioService, com dois métodos: criarUsuario e listarUsuarios;
- Linha 8: declaramos o método criarUsuario, informando que esperamos receber uma mensagem do tipo NovoUsuarioRequest e que vamos retornar uma mensagem do tipo NovoUsuarioResponse;
- Linha 9: declaramos o método listarUsuarios, mas especificando que esperamos receber uma mensagem do tipo FiltroListaUsuarioRequest e que vamos retornar uma mensagem do tipo ListaUsuarioResponse;
- Linhas 12~18: aqui especificamos o formato da mensagem NovoUsuarioRequest, dizendo que a mesma terá quatro campos do tipo string e um do tipo long (uint64);
- Linhas 20~22: aqui especificamos o formato da mensagem de retorno após a criação do usuário ser bem sucedida. Poderíamos apenas retornar uma mensagem vazia também;
- Linhas 24~29: mensagem contendo os campos que serão informados pelo cliente gRPC ao chamar o método listarUsuarios. Cada campo será utilizado para montar o SQL que buscará os usuários no banco de dados;
- Linhas 31~33: definimos uma mensagem de nome ListaUsuarioResponse e informamos que retornaremos uma lista de usuários (observe a palavra "repeated" informando que um ou mais usuários serão retornados).

As mesmas definições valem para o arquivo [login.proto](src/main/proto/login.proto) (mudando os serviço, claro).

### Geração das classes de gRPC
Para gerar as classes utilizando o Maven, basta rodar o maven compile:
```bash
 mvn compile
 ```
Após rodar o comando, as classes geradas a partir dos arquivos .proto estarão dentro de [target/generated-sources/protobuf](target/generated-sources/protobuf). 

### Implementando a lógica de negócio nos serviços gRPC
Dentro de [grpc](src/main/java/scheper/mateus/grpc), temos a classe [UsuarioServiceImpl](src/main/java/scheper/mateus/grpc/UsuarioServiceImpl.java), responsável por responder às requisições gRPC do serviço de usuário. Nela, vemos os seguintes pontos:
- Linha 16: Anotamos a classe com @GRpcService (sim, com "R" maiúsculo), definindo a classe como um serviço gRPC;
- Linha 17: na declaração da classe, extendemos a classe _UsuarioServiceGrpc.UsuarioServiceImplBase_ gerada pelo Protobuf através do Maven, definida no arquivo [usuario.proto](src/main/proto/usuario.proto). Ao extender a classe, podemos sobrescrever os métodos _criarUsuario_ e _listarUsuarios_;
- Linhas 19 e 21: Injetamos o serviço de usuário, que é onde faremos realmente a nossa lógica de negócio (criar e listar usuários);
- Linhas 26~51: Aqui sobrescrevemos o método _criarUsuario_, sendo:
  - Linha 27: Anotação @Allow para que a implementação de segurança do sistema permita apenas que usuários logados que possuam a função ADMIN consigam acessar o recurso;
  - Linha 28: Recebemos por parâmetro os objetos _NovoUsuarioRequest_ e _StreamObserver<NovoUsuarioResponse>_, sendo os dados passados no request pelo usuário e o objeto que utilizaremos para controlar a resposta (sucesso/erro);
  - Linha 30: Aqui chamamos o serviço _UsuarioService_ passando o request como parâmetro e obtendo o response já populado com os dados do usuário criado;
  - Linhas 32 e 33: Sendo que já temos o response, inserimos ele no objeto de retorno (linha 32) e completamos a requisição (linha 33), entregando os dados ao usuário;
  - Linhas 37~45: Caso aconteça alguma exceção, cairemos no catch, permitindo retornar um código de erro para o usuário. Aqui criamos o código de erro INVALID_ARGUMENT (3), detalhamos o erro, adicionamos na resposta (linha 32) e finalmente lançamos a exceção, fazendo com que a resposta para o usuário seja um erro. 
- Linhas 59~66: Aqui fazemos algo parecido com o método anterior, mas para a listagem de usuários.

### Segurança com JWT
Nossa aplicação está segurada pela biblioteca [grpc-jwt-spring-boot-starter](https://github.com/majusko/grpc-jwt-spring-boot-starter), responsável por proteger todos os endpoints anotados com @Allow e nos permitir que geremos os tokens JWT através do _JwtService_.

Dentro de [grpc](src/main/java/scheper/mateus/grpc), temos a classe [LoginServiceImpl](src/main/java/scheper/mateus/grpc/LoginServiceImpl.java), responsável por responder às requisições de login. 
Nela, chamamos o serviço _LoginService_ para criar o token JWT (linha 25), adicionamos o token na resposta da requisição (linha 26), adicionamos a requisição no retorno (linha 27) e finalmente completamos o request (linha 28).

Daqui em diante, o sistema já mantém informações sobre o token recém criado, pois dentro de _LoginService_, mais especificamente na linha 37, o serviço do gRPC _JwtService_ já gerou o token com o código de assinatura especificado no application.properties e 
adicionou um tempo de expiração (também especificado lá).

A partir do momento em que o usuário obtém o token do corpo da requisição de login, será possível acessar 
os recursos protegidos passando o mesmo no header Authorization (desde que ele tenha a role necessária, claro).


### Criando um cliente para testes
Como somos ~~preguiçosos~~ espertos, em vez de criarmos uma outra aplicação somente para implementarmos um cliente gRPC,
vamos utilizar endpoints REST para chamar os endpoints gRPC:

Dentro de [rest](src/main/java/scheper/mateus/rest), temos um controller responsável por atender às requisições REST em [/usuario](http://localhost:8080/usuario.
Nas requisições, chamamos o endpoint /usuario tanto em GET quanto em POST, podendo também passar parâmetros. 
Quando um endpoint for chamado, em vez de acessarmos um serviço para cadastrar/listar usuário no banco de dados como normalmente faríamos, vamos na verdade chamar o serviço de login do gRPC para obter um token, inserir o token em uma nova chamada gRPC (_criarUsuario_ ou _listarUsuarios_, dependendo do endpoint REST que for chamado) e aí sim fazer a requisição gRPC que queremos. 
Dessa forma, é possível ver que conseguimos ter serviços gRPC e REST na mesma aplicação, podendo ou não interligá-los.

Vemos um exemplo:
- O usuário faz uma requisição REST do tipo POST em /usuario, passando os dados do usuário que será cadastrado;
- A aplicação recebe os dados e chama o método _usuarioService.criarUsuario(usuarioDTO)_;
- Dentro do método, um outro método chamado _criarStub_ é invocado, onde:
    - O canal é criado (linha 65. É onde especificamos o HOST:PORTA que o serviço gRPC está rodando);
    - O stub (cliente) é criado (linha 66) e vinculado ao canal;
    - O token é obtido do serviço de login do gRPC e adicionado no stub (cliente).
- Após a criação do stub (cliente) com o token JWT, um objeto do tipo NovoUsuarioRequest (gerado pelo Protobuf) é criado com os dados enviados via POST (linha 41);
- O método gRPC é chamado (linha 42), retornando um objeto do tipo NovoUsuarioResponse (gerado pelo Protobuf);
- O método então retorna o DTO novamente para o usuário observar os dados do usuário recém criado (linha 45).
