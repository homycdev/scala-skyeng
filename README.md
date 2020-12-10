# Scala Course Project 2020 - Skyeng platform

Сервис для изучения иностранных языков типа skyeng.ru

Ролевая модель: клиенты, преподаватели, администраторы
- Назначение уроков для клиентов
- Биллинг для клиентов и преподавателей


**Service for learning foreign languages like skyeng.ru**

Role model: clients, educators, administrators
- Assigning Clients Lessons
- Billing for clients and teachers

## Architecture 
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
- PostgreSQL - as a main database
- Tagless Final: For the core domain

### Onion Architectural Pattern:
The project is being made following up [Onion Architectural design](https://medium.com/@shivendraodean/software-architecture-the-onion-architecture-1b235bec1dec#:~:text=The%20Onion%20Architecture%20is%20an,at%20a%20Solution%2FSystem%20level.) principles.
Using this approach we are decoupling our application modules into separate parts:

We are combining DDD with Onion together through following mechanisms:

**Domain package**. We tried to keep by dividing our modules in reasonable way. 
We use `Services` as straight forward interface to our domain. You can see quite a lot of boiler plate related to CRUD 
operations. We tried to keep one to one mapping between our `Services` and `Endpoints`.

In the domain module you will find following concepts:
1. `Service` - serves. Use cases that work with other domain conpets to realise your use-cases
2. `Repository` - ways to access to the data. 
3. `Models` - stuff like `classes`, `lessons`, `Student Profile` and many many more... All the behaviour comes using `Validation`
and `Services`
#### Modules:
- **Controllers**: Handle the logic of the incoming requests to our endpoints
- **Domain**: Module where we describe the core business logic. It consists of following:
    - Authentication - helps with authentication
    - Courses - course back-end logics, with related entities
    - Payment - payment service
    - Results - helper module, consists of entities which needed to evaluate Students' performance
    - Schedule - scheduling service that helps to students to choose the Classes
    - Users - has user, teacher, student services 
- **Infrastructure**: contains the HTTP endpoints that we surface via http4s. You will also typically see JSON things in here via circe
- **Configuration**:  contains the JDBC code, implementations of our Repositories. We have 2 implementations, an in-memory version as well as a doobie version.

The config package The config package could be considered infrastructure, as it has nothing to do with the domain. We use Circe Config to load configuration objects when the application starts up. circe config Provides a neat mapping of config file to case classes for us, so we really do not have to do any code.

### DataBase scheme:
You can observe the scheme in the following [link](https://drive.google.com/file/d/1sknFvJ0BTB3cFnHtQf6PakkdaXu1NTkh/view?usp=sharing)


## Branch creation policy
1. Create Issue as following: SCP-###: Issue Title
2. Create Branch in issue as following: scp-###-issue-title

## How to run?

Run following bash scripts to run the server application:

1. Built the project:
```
sbt docker:publishLocal
```

Once app is loaded, run second step to run the app:
```
docker-compose up -d --build
```
then 
```
docker-compose logs -f
```

## Testing
Application preruns the written into `test` package tests on gitlab CI/CD

In the test module of the applicaiton we are running tests basing on the 

Application tests cover the endpoints behaviour of the application.
It consists of typical command to see whether the services function as supposed to be.




















