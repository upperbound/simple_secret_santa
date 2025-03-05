[![en](src/main/resources/static/images/en.png)](README.md)

# Простая реализация офисной версии Тайного Деда-Мороза

### Описание
Приложение, которое позволяет принять участие в игре "Тайный Дед-Мороз". Предоставляет возможности для создания групп,
приглашения участников в эти группы и розыгрыша результатов для определения подопечных всем участникам.

Версия Java - 21\
[Параметры приложения](./src/main/resources/application.properties) - содержит все основные настройки приложения\
[Сообщения для локализации](./src/main/resources/messages.properties) - используются для локализации приложения.\
Шаблоны страниц лежат в папке **resources/templates** (используется Thymeleaf для server-side генерации html страниц)

Так как используется Spring Boot, то при автоконфигурации Spring уже знает, что:
- шаблоны находятся в **resources/templates** (и доступны **view** уже с этими именами в контроллерах).
- статичные ресурсы находятся в **resources/static** (которые он публикует сразу по нужному адресу -
по структуре папок внутри, например **http://localhost:8080/css/common.css**).
- sql скрипты находятся в корне приложения.

### Сборка
Сборка подготавливает `zip` архив, который содержит [скрипт запуска](./image/run.sh), основной `jar` файл + зависимости,
а также каталог с необходимым ресурсами и файлом конфигурации.
Переменные окружения для запуска приложения:
- `JAVA_HOME` - путь до java, например `/opt/jdk-21.0.6`
- `HEAP_INIT` (не обязательно) - размер выделенной оперативной памяти при старте приложения, `1G` по умолчанию
- `HEAP_MAX` (не обязательно) - максимальное количество выделенной оперативной памяти, `1G` по умолчанию
- `JMX_REMOTE_PORT` (не обязательно) - порт для jmx, `отключено` по умолчанию

Чтобы запустить демо версию с предзаполненной БД (superadmin пользователь `admin@example.org` с паролем `0`) нужно выполнить:
```bash
export JAVA_HOME=/path/to/java && bash run.sh example
```

### Настройки приложения
Все основные настройки содержатся в каталоге `config` - содержит все файлы `messages**.properties` для локализации, файл
[настроек](./src/main/resources/config.properties) и ресурсы, доступные в контексте приложения. Для того
чтобы добавить дополнительную локализацию, необходимо создать новый файл `messages_%LOCALE_NAME%.properties`,
где [%LOCALE_NAME%](https://en.wikipedia.org/wiki/IETF_language_tag) это имя локализации,
а также добавить картинку для выпадающего списка в `resources/images`.\
Для запуска приложения доступны несколько профилей:
- `ignore-notifications` - отключает отправку уведомлений по почте и использование настроек для почтового сервера.
- `no-password` - отключает пользовательский ввод пароля и задает его всегда одним значением (для теста/демонстрации).
- `uuid-password` - отключает пользовательский ввод пароля и генерирует его случайно на основе **UUID**, после чего
отправляет его в уведомлении (если отправка уведомлений не отключена).

Файл [настроек](./src/main/resources/config.properties) **НЕОБХОДИМО** отредактировать в зависимости от того
в рамках какого окружения будет запущенно данное приложение.\
Описание основных и обязательных настроек:
```properties
# Профили через запятую для управления поведением приложения. Описаны выше. 
spring,profiles,active=
# Время действия токена для сброса пароля
app.action-token-duration=2H
# keystore с секретным ключом для шифрования данных в БД
app.crypto.ks-location=classpath:keystore-secret.jks
# пароль от 'ks-location'
app.crypto.ks-password=default
# алиас секретного ключа, содержащегося в 'ks-location'
app.crypto.ks-key-alias=key-secret
# пароль для ключа 'ks-key-alias'
app.crypto.ks-key-password=default
# если не предоставлен ключ выше, тогда эта фраза будет использована в качестве 256 битного AES ключа
app.crypto.key-salt=any_value
# пароль, который будет проставляться всем пользователям если включен профиль 'no-password'
app.default-pass=0
# адрес, от имени которого будут отправляться уведомления
app.mail.from=secret_santa_no_reply@my-office.com
# задержка в секундах между попытками отправить недоставленные уведомления, по умолчанию 30
app.mail.resend-delay=30
# количество попыток для отправки недоставленных уведомлений (если -1, то бесконечно), по умолчанию 1
app.mail.resend-attempts=1
# шаблон регулярного выражения для проверки валидности email, по умолчанию .*
app.mail.allowed-regex=.*
# сообщение об ошибке при нарушении 'allowed-regex'
app.mail.allowed-err-message=email '%s' does not belong to any corporate domain @my-office.com / @my-office.org
# DNS или IP данного приложения (например https://secret-santa.my-office.com) - будет использован в различных уведомлениях
app.server.url=http://${server.address}:${server.port}
# единственный способ установить/убрать роль 'superadmin' только при старте приложения:
# список пользователей которым установить роль 'superadmin'
app.superadmins="{'user1@my-office.com','user2@my-office.com'}"
# список пользователей которым установить роль 'user'
app.users="{'user3@my-office.com','user4@my-office.com'}"
# Настройки для запуска контейнера сервлетов. SSL по умолчанию отключен, если необходимо использовать
# TLS соединение на уровне приложения, а не в рамках внешнего сервера, тогда необходимо установить `enabled: true`
# и добавить сертификат и private key
server.address=localhost
server.port=8080
server.ssl.enabled=false
server.ssl.certificate=classpath:certificate-ec.crt
server.ssl.certificate-private-key=classpath:private-ec-key.pem
# Название БД и пользователь, под которым будет создана новая база данных и все необходимые таблицы.
spring.datasource.db-name=santa-db
spring.datasource.username=admin
spring.datasource.password=secretpass
# Настройки для подключения к почтовому серверу.
# Будут использоваться для отправки уведомлений.
spring.mail.protocol=smtps
spring.mail.host=mail.my-office.com
spring.mail.port=465
spring.mail.username=user
spring.mail.password=userpass
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.protocols=TLSv1.2
spring.mail.properties.mail.smtp.ssl.trust=my-office.com
```

### Структура приложения
**com.github.upperbound.secret_santa.**
- **config** - все настройки для приложения и
  [PostInitializer](./src/main/java/com/github/upperbound/secret_santa/config/PostInitializer.java) (для установки роли супер админа)
- **model** - слой ORM
- **repository** - слой DAO
- **service** - вся бизнес-логика приложения, где происходит создание пользователей,
  проведение розыгрыша, отправка уведомлений и т.д.
- **util** - общие утилитарные классы
- **web** - все веб-контроллеры, которые обрабатывают **GET** и **POST** запросы вызывая бизнес-логику

Информация по ролям:
- **USER** - роль задается по умолчанию. При создании новой группы создающий ее пользователем становится в ней
администратором (имеет возможность редактировать группу, состав участников и проведение розыгрыша). Администратор
имеет возможность установить/убрать эту роль любому другому пользователю в данной группе.
- **SUPERADMIN** - можно установить только при запуске приложения, доступны все вкладки и все действия во всех группах
(убрать эту роль можно также только при запуске, передав этого пользователя в качестве **app.users**)
