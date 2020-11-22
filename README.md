# Scala Course Project 2020 - Skyeng platform

Сервис для изучения иностранных языков типа skyeng.ru

Ролевая модель: клиенты, преподаватели, администраторы
- Назначение уроков для клиентов
- Биллинг для клиентов и преподавателей


**Service for learning foreign languages like skyeng.ru**

Role model: clients, educators, administrators
- Assigning Clients Lessons
- Billing for clients and teachers

## Architecture V 0.1
### Technology Stack
In the project given we will try to work with TypeLevel stack and see whether is it going to be possible to keep it as close to practices of FP 
programming. 
We are using:
- [Http4s](https://http4s.org/) - as a web server
- [Flyway](https://github.com/flyway/flyway-sbt) - db migration service
- [Doobie](https://github.com/tpolecat/doobie) - for db access
- [Circe](https://circe.github.io/circe/) - JSON serialisation
- [Cats](https://typelevel.org/cats/) - FP data structures
- [Circe Config](https://github.com/circe/circe-config) - for app config
- [Scala Check](https://www.scalacheck.org/) - for property based testing
- Tagless Final 

### Onion Architectural Pattern:
The project is being made following up [Onion Architectural design](https://medium.com/@shivendraodean/software-architecture-the-onion-architecture-1b235bec1dec#:~:text=The%20Onion%20Architecture%20is%20an,at%20a%20Solution%2FSystem%20level.) principles.
Using this approach we are decoupling our application modules into separate parts:
`todo1: insert graph of arch`

#### Modules:
- **Domain**: Module where we describe the core business logic
- **Infrastructure**: Module where we describe the endpoints and db operating functions
- **Configuration**: Module where we describe configurations of the app(which db to use, on which ports to run the server, etc.)

`todo2: insert the detailed description of each module`

#### DataBase scheme:
For the current version V 0.1 , there are some entities related to `User` Business logic.
!DISCLAIMER! DB Scheme is still in development. Many things may change later on :)
You can observe the scheme in the following [link](https://drive.google.com/file/d/1sknFvJ0BTB3cFnHtQf6PakkdaXu1NTkh/view?usp=sharing)



## How to run?
`todo3: place detailed running instructions`
Startup sbt:
```
sbt --java-home {your.java.11.location}
```

Once app is loaded, start the app:
```
> ~reStart
```

## Testing
Application preruns the written into `test` package tests on gitlab CI/CD






















