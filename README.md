# 🚗 Vrum Motors — Sistema de Gestão de Concessionária

Sistema web completo para gestão de vendas de veículos, desenvolvido com **Java EE + JSF + JPA + MySQL**, implantado no **GlassFish**.

---

## 📁 Estrutura do Projeto

```
vrum-motors/
├── src/
│   ├── main/
│   │   ├── java/br/com/vrum/
│   │   │   ├── model/          # Entidades JPA
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Vendedor.java
│   │   │   │   ├── Gerente.java
│   │   │   │   ├── AdminEmpresa.java
│   │   │   │   ├── AdminFabrica.java
│   │   │   │   ├── Veiculo.java
│   │   │   │   ├── Pedido.java
│   │   │   │   ├── Concessionaria.java
│   │   │   │   ├── Anexo.java
│   │   │   │   ├── PerfilUsuario.java (enum)
│   │   │   │   ├── StatusPedido.java (enum)
│   │   │   │   └── TipoVeiculo.java  (enum)
│   │   │   ├── dao/            # Acesso ao banco (JPA)
│   │   │   │   ├── GenericDAO.java
│   │   │   │   ├── UsuarioDAO.java
│   │   │   │   ├── VeiculoDAO.java
│   │   │   │   ├── PedidoDAO.java
│   │   │   │   ├── ConcessionariaDAO.java
│   │   │   │   ├── ClienteDAO.java
│   │   │   │   ├── VendedorDAO.java
│   │   │   │   └── AnexoDAO.java
│   │   │   ├── service/        # Regras de negócio
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── PedidoService.java
│   │   │   │   ├── VeiculoService.java
│   │   │   │   └── ConcessionariaService.java
│   │   │   ├── bean/           # Managed Beans JSF
│   │   │   │   ├── LoginBean.java
│   │   │   │   ├── HomeBean.java
│   │   │   │   ├── CadastroBean.java
│   │   │   │   ├── VeiculoDetalheBean.java
│   │   │   │   ├── VendedorBean.java
│   │   │   │   ├── FabricaBean.java
│   │   │   │   ├── ClientePedidoBean.java
│   │   │   │   ├── AdminBean.java
│   │   │   │   └── GerenteBean.java
│   │   │   ├── filter/
│   │   │   │   └── AuthFilter.java  # Controle de acesso por perfil
│   │   │   └── util/
│   │   │       ├── JPAUtil.java
│   │   │       ├── SenhaUtil.java
│   │   │       ├── LocalDateConverter.java
│   │   │       └── DataInicializador.java  # Dados de exemplo
│   │   ├── resources/META-INF/
│   │   │   └── persistence.xml
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   └── faces-config.xml
│   │       ├── resources/css/
│   │       │   └── vrum-style.css
│   │       ├── index.xhtml
│   │       ├── home.xhtml          # Página pública (catálogo)
│   │       ├── login.xhtml
│   │       ├── cadastro.xhtml
│   │       ├── veiculo.xhtml       # Detalhe do veículo
│   │       ├── acesso-negado.xhtml
│   │       └── pages/
│   │           ├── admin/
│   │           │   ├── dashboard.xhtml
│   │           │   ├── usuarios.xhtml
│   │           │   ├── veiculos.xhtml
│   │           │   ├── concessionarias.xhtml
│   │           │   └── pedidos.xhtml
│   │           ├── gerente/
│   │           │   ├── dashboard.xhtml
│   │           │   └── vendedores.xhtml
│   │           ├── fabrica/
│   │           │   └── pedidos.xhtml
│   │           ├── vendedor/
│   │           │   └── pedidos.xhtml
│   │           └── cliente/
│   │               └── meus-pedidos.xhtml
│   └── test/java/br/com/vrum/selenium/
│       └── VrumMotorsSeleniumTest.java  # 25 testes automatizados
└── pom.xml
```

---

## ⚙️ Configuração e Execução

### 1. Pré-requisitos

- Java 11+
- Maven 3.8+
- MySQL 8.0+
- GlassFish 5.1 ou Payara 5
- Google Chrome (para os testes Selenium)
- VSCode com extensões: **Extension Pack for Java**, **Community Server Connectors**

### 2. Criar o banco de dados

```sql
CREATE DATABASE vrum_motors CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON vrum_motors.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

> 💡 Ajuste usuário/senha em `persistence.xml` se necessário.

### 3. Configurar GlassFish no VSCode

1. Instale a extensão **Community Server Connectors** (Red Hat)
2. No painel lateral, clique em **Servers → Create New Server**
3. Selecione **GlassFish 5.1** e aponte para o diretório de instalação
4. Inicie o servidor

### 4. Build e Deploy

```bash
# Na raiz do projeto:
mvn clean package

# O arquivo gerado estará em:
target/vrum-motors.war

# Copie para a pasta autodeploy do GlassFish:
cp target/vrum-motors.war $GLASSFISH_HOME/domains/domain1/autodeploy/
```

Ou use o VSCode: **clique direito no .war → Deploy to Server**

### 5. Acessar a aplicação

| URL | Descrição |
|-----|-----------|
| http://localhost:8080/vrum-motors/ | Home pública (catálogo) |
| http://localhost:8080/vrum-motors/login.xhtml | Login interno |

---

## 👤 Logins de Exemplo

> Criados automaticamente pelo `DataInicializador` na primeira execução.

| Perfil | E-mail | Senha |
|--------|--------|-------|
| Administrador Empresa | admin@vrummotors.com | admin123 |
| Gerente (Recife) | gerente.recife@vrummotors.com | gerente123 |
| Admin Fábrica | fabrica@vrummotors.com | fabrica123 |
| Vendedor | vendedor@vrummotors.com | vendedor123 |
| Cliente | cliente@email.com | cliente123 |

---

## 🔄 Fluxo do Sistema

```
Cliente → Visualiza catálogo → Escolhe veículo → Cadastra-se → Pedido criado
                                                                      ↓
                                          Vendedor assume o pedido ←──┘
                                                   ↓
                              Negociação por WhatsApp (fora do sistema)
                                                   ↓
                              Vendedor envia pedido para fabricação
                                                   ↓
                              Admin Fábrica atualiza: Em Fabricação → Fabricado → Enviado
                                                   ↓
                              Vendedor marca: Pronto para Entrega
                                                   ↓
                              Entrega realizada → Vendedor Finaliza com data de retirada
                                                   ↓
                              Cliente acompanha todo o processo em tempo real
```

---

## 🧪 Executar Testes Selenium

```bash
# Com a aplicação rodando:
mvn test

# Para rodar apenas os testes Selenium:
mvn test -Dtest=VrumMotorsSeleniumTest
```

**25 casos de teste** cobrem:
- ✅ Acesso público à home
- ✅ Login válido/inválido para cada perfil
- ✅ Redirecionamento correto por perfil
- ✅ Controle de acesso (bloqueio de áreas não permitidas)
- ✅ Exibição de veículos e lançamentos
- ✅ Busca de veículos
- ✅ Dashboards e listagens
- ✅ Logout

---

## 🏛️ Arquitetura

```
Camada de Apresentação  →  JSF (XHTML) + CSS customizado
Camada de Controle      →  Managed Beans (@ManagedBean)
Camada de Serviço       →  Services (regras de negócio)
Camada de Acesso        →  DAO (GenericDAO + especializados)
Camada de Persistência  →  JPA / Hibernate
Banco de Dados          →  MySQL 8
Servidor de Aplicação   →  GlassFish 5 / Payara 5
```

---

## 📋 Requisitos Funcionais Implementados

| RF | Descrição | Status |
|----|-----------|--------|
| RF01 | 5 perfis de usuário | ✅ |
| RF02 | Admin da empresa — CRUD completo | ✅ |
| RF03 | Gerente — sua sede + vendedores | ✅ |
| RF04 | Admin fábrica — status de fabricação | ✅ |
| RF05 | Catálogo público de veículos | ✅ |
| RF06 | Fluxo de compra do cliente | ✅ |
| RF07 | Gestão de pedidos com status | ✅ |
| RF08 | Atuação do vendedor (assumir, anexar, finalizar) | ✅ |
| RF09 | Cliente acompanha pedido em tempo real | ✅ |

---

*Desenvolvido como projeto acadêmico — Vrum Motors © 2025*
