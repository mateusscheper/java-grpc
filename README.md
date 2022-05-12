
# Java gRPC

O gRPC (Google Remote Procedure Call) é um framework de código aberto usado para conectar sistemas de maneira eficiente, seja através da internet ou em data centers. Seu uso se dá em aplicações web, mobile, dispositivos externos e onde mais precisar.
Seu desenvolvimento acontece de maneira similar ao SOAP, tendo um arquivo modelo para os serviços, requisições e respostas, e toda a geração de código é feita através do Protocol Buffer, permitindo que o desenvolvedor foque apenas nas regras de negócio.
Veremos abaixo comparações de desempenho entre o gRPC e outros modelos, como o REST e o SOAP. Spoiler: gRPC é muito mais rápido.



### Visão geral

Ao iniciar o desenvolvimento de uma aplicação gRPC, o desenvolvedor cria um arquivo .proto especificando os serviços (endpoints) que o sistema responderá, bem como o modelo das requisições esperadas pelos serviços e as respostas enviadas.
Tendo o modelo pronto, já é possível gerar os arquivos na linguagem que você quiser através do terminal ou mesmo através de um gerenciador de dependências, como o Maven. Ao gerar todos os arquivos, o sistema já está pronto para começar a ter suas regras de negócio desenvolvidas.

### Por que usar gRPC?

- Baixa latência, alta escalabilidade, sistemas distribuídos;
- Permite desenvolvimento em aplicações mobile;
- Preciso, eficiente e independente de linguagens de programação;
- Permite autenticação, balanceamento de carga, logs, monitoramento e mais.

### Desenvolvimento cliente-servidor entre sistemas

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
- Nas linhas 23~46, adicionamos as dependências necessárias para rodar o gRPC na aplicação;
- Nas linhas 48~83, adicionamos as dependências do Spring;
- Nas linhas 86~118, informamos ao Maven que queremos buildar o sistema, incluindo a geração das classes Java a partir do arquivo [usuario.proto](./src/main/proto/usuario.proto);
- Nas linhas 120~155, temos a declaração de alguns repositórios utilizados pelo Spring.

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
### Classes para cadastro de usuário

Ao observarmos o pacote [br.senai.sc](./java/br/senai/sc/), vemos alguns diretórios e suas classes:
- [builder](./java/br/senai/sc/builder/): contém a classe responsável por auxiliar a criação do objeto [Usuario](./java/br/senai/sc/entity/Usuario.java) do banco de dados;
- [entity](./java/br/senai/sc/entity/): contém a classe que representa o usuário no banco de dados;
- [grpc](./java/br/senai/sc/grpc/): contém a implementação do serviço de gRPC no sistema;
- [repository](./java/br/senai/sc/repository/): contém a interface do repositório de banco de dados ([UsuarioRepository](./java/br/senai/sc/repository/UsuarioRepository.java)) de usuário e sua implementação ([UsuarioRepositoryImpl](./java/br/senai/sc/repository/UsuarioRepositoryImpl.java));
- [service](./java/br/senai/sc/service/): contém a classe de serviço responsável por realizar as regras de negócio do sistema, bem como invocar o repositório de usuário para realizar operações de banco.
### Arquivo usuario.proto

Observando o arquivo [usuario.proto](./br/senai/sc/src/main/proto/usuario.proto), observamos:
- Linha 1: informamos para o Protobuf que estamos utilizando a versão 3. Caso não informado, o padrão é a versão 2;
- Linha 3: definimos que queremos criar múltiplos arquivos em vez de um só;
- Linha 4: especificamos que o nome do pacote dentro da pasta onde os arquivos serão gerados se chama "br.senai.sc.grpc";
- Linha 6: definimos o nome do pacote como "grpc";
- Linha 8: declaramos um serviço de gRPC chamado UsuarioService, com dois métodos: criarUsuario e listarUsuarios;
- Linha 9: declaramos o método criarUsuario, informando que esperamos receber uma mensagem do tipo NovoUsuarioRequest e que vamos retornar uma mensagem do tipo NovoUsuarioResponse;
- Linha 10: declaramos o método listarUsuarios, mas especificando que esperamos receber uma mensagem do tipo Usuario (que vamos utilizar como filtro) e que vamos retornar uma mensagem do tipo ListaUsuarioResponse;
- Linhas 13~16: aqui especificamos o formato da mensagem NovoUsuarioRequest, dizendo que a mesma terá três campos do tipo string;
- Linhas 19~21: aqui especificamos o formato da mensagem de retorno após a criação do usuário ser bem sucedida. Poderíamos apenas retornar uma mensagem vazia também;
- Linhas 23~28: mensagem contendo os campos que serão informados pelo cliente gRPC ao chamar o método listarUsuarios. Cada campo será utilizado para montar o SQL que buscará os usuários no banco de dados;
- Linhas 30~32: definimos uma mensagem de nome ListaUsuarioResponse, informamos que retornaremos uma lista de usuários (observe a palavra "repeated" informando que um ou mais usuários serão retornados).
### Geração das classes de gRPC

