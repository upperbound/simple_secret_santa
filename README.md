# Тайный дед мороз

### Описание
Версия JDK - 21\
[Параметры приложения](./src/main/resources/application.yaml) - содержит все основные настройки приложения\
[Параметры html шаблонов](./src/main/resources/templates.properties) - используется для установки значений в html шаблоны
мавеном при сборке и не включается в итоговую сборку. Сами шаблоны лежат в папке
**resources/templates** (используется Thymeleaf для server-side генерации html страниц)\
\
Так как используется Spring Boot, то при автоконфигурации Spring уже знает, что:
- шаблоны находятся в **resources/templates** (и доступны **view** уже с этими именами в контроллерах)
- статичные ресурсы находятся в **resources/static** (которые он паблишит сразу по нужному адресу -
по структуре папок внутри, например **http://localhost:8080/css/common.css**)
- sql скрипты находятся в корне приложения 

### Структура приложения

**com.github.upperbound.secret_santa.**
- **config** - все конфиги для приложения и
[PostInitializer](src/main/java/com/github/upperbound/secret_santa/config/PostInitializer.java) (для установки роли супер админа)
- **model** - слой ORM
- **repository** - слой DAO
- **service** - вся бизнес-логика приложения, где происходит создание пользователей,
проведение розыгрыша, отправка писем и т.д.
- **util** - общие утилитарные классы
- **web** - все веб-контроллеры которые связаны с html-шаблонами и которые обрабатывают **GET** и **POST** запросы
вызывая бизнес-логику

Небольшие уточнения:
- [MvcConfig](src/main/java/com/github/upperbound/secret_santa/config/MvcConfig.java) - реализует **ApplicationListener**
чтобы запомнить порт на котором мы стартанули, нужно будет для полного адреса приложения
совместно с параметром **app.server.hostname**, при условии, что приложение доступно не по **https**
- [SecurityConfig](src/main/java/com/github/upperbound/secret_santa/config/SecurityConfig.java) -
прописаны все правила доступа к ресурсам приложения. Для того, чтобы Spring Security работал как мы хотим,
интерфейс [ParticipantDetails](src/main/java/com/github/upperbound/secret_santa/model/ParticipantDetails.java)
экстендит **UserDetails**,
а [ParticipantService](src/main/java/com/github/upperbound/secret_santa/service/ParticipantService.java)
экстендит **UserDetailsService**,
чтобы спринг знал наших пользователей и использовал наши методы для получения этой информации и авторизации
- [логирование](./src/main/resources/log4j2.xml) не настроено в файл, следовательно делать через **nohup**

Информация по ролям:
- **USER** - роль задается по умолчанию, пользователю доступна вкладка **Профиль**
- **ADMIN** - можно установить из интерфейса (либо при запуске через **-Dapp.admins**)
доступны вкладки **Информация об участниках** и **Профиль** (возможность проводить конкурс
только в своем подразделении и из действий только **Удалить участника**)
- **SUPERADMIN** - можно установить только при запуске приложения, доступны все вкладки и все действия
(убрать эту роль можно также только при запуске, передав этого пользователя в качестве **-Dapp.admins**,
то есть сделав просто **ADMIN**, а потом в интерфейсе можно поменять роль на **USER**)

Информация по действиям:
- **Переместить в другое подразделение** - перемещает участника в выбранную группу.
Если в группе, **из которой** перемещается участник, **проведен конкурс**, то подопечный перемещаемого участника переходит по наследству
тайному деду морозу этого участника. Если в группе, **в которою** перемещается участник, **проведен конкурс**, то
выбирается случайный участник из этой группы, подопечный этого случайного участника достается перемещаемому участнику,
а сам перемещаемый участник достается в качестве подопечного случайно выбранному. Во всех этих случаях происходит соответствующая
рассылка писем о том, что поменялся подопечный (при условии, что включено "получать уведомления")
- **Удалить участника** - удаляет участника и его аккаунт, если в группе проведен конкурс, то подопечный
удаляемого участника переходит по наследству тайному деду морозу этого участника
- **Поменять роль** - меняет роль с ADMIN на USER или наоборот

### Запуск приложения
Основные параметры которые **НУЖНО** передавать при запуске:
- **-Dapp.mail.do-send=true** - включить отправку писем пользователям, по умолчанию **false** и письма не отправляются
- **-Dserver.port=2024** - порт, на котором запустится приложение
- **-Dspring.datasource.password=1234** - задать пароль для БД. При первом запуске он создает новую БД используя скрипты
[схемы](./src/main/resources/schema.sql) и [данных](./src/main/resources/data.sql), а также устанавливает указанный пароль.
Для последующего входа в БД будет использоваться этот пароль
- **-Dapp.superadmins="{'i.ivanov@e-soft.ru','i.petrov@e-soft.ru'}"** - список пользователей, которым будет проставлена роль
супер админа. При первом запуске БД пустая, следовательно нужно запустить приложение, зарегистрироваться,
и перезапустить для установки роли данным пользователям

Итоговый скрипт будет выглядеть примерно так:
```
#!/bin/bash
nohup /opt/jdk-17.0.2/bin/java \
-Dapp.mail.do-send=true \
-Dserver.port=2024 \
-Dspring.datasource.password=secretpass \
-Dapp.superadmins="{'i.ivanov@e-soft.ru','i.petrov@e-soft.ru'}" \
-jar anonymous_grandfather_frost-1.0.jar &
```

## Дополнительная информация по Spring, которую предлагает сам Spring

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.1-SNAPSHOT/maven-plugin/reference/html/)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.2.1-SNAPSHOT/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Java Mail Sender](https://docs.spring.io/spring-boot/docs/3.2.1-SNAPSHOT/reference/htmlsingle/index.html#io.email)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.1-SNAPSHOT/reference/htmlsingle/index.html#web)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.2.1-SNAPSHOT/reference/htmlsingle/index.html#web.security)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)

