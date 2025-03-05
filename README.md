[![ru](src/main/resources/static/images/ru.png)](README.ru.md)

# Simple implementation of a self-hosting office-driven Secret Santa

### Description
An application that allows you to participate in the intriguing game "Secret Santa". It provides the ability
to create groups, invite participants to these groups and then draw the results to determine the "giftee" for each participant.

Java version - 21\
[Application properties](./src/main/resources/application.properties) - contains all common application properties\
[Localization bundles](./src/main/resources/messages.properties) - used for localization of the application.\
The html-page templates are located in **resources/templates** (Thymeleaf has been used for server-side html generation)

Since Spring Boot is used, during auto-configuration Spring already knows that:
- the templates are located in **resources/templates** (and **view** is already available with these names in the controllers).
- static resources are located in **resources/static** (which will be published to the specific address
according to the folder structure inside, e.g. **http://localhost:8080/css/common.css**).
- sql are located in the root classpath.

### Build
The build prepares a `zip` archive that contains the [startup script](./image/run.sh), main `jar` + dependencies,
as well as a directory with the necessary resources and a configuration file.\
Environment variables to run this application:
- `JAVA_HOME` - path to java, e.g. `/opt/jdk-21.0.6`
- `HEAP_INIT` (optional) - available amount of physical memory at startup, `1G` by default
- `HEAP_MAX` (optional) - maximum available amount of physical memory, `1G` by default
- `JMX_REMOTE_PORT` (optional) - port for jmx, `disabled` by default

To run a demo version with pre-initialized DB (`admin@example.org` as a superadmin user with password `0`) just run as:
```bash
export JAVA_HOME=/path/to/java && bash run.sh example
```

### Application settings
All the basic settings are located in the `config` folder - it contains all the `messages**.properties` files
for localization, the [settings file](./src/main/resources/config.properties), and the resources available
in the application context.
In order to add additional localization, you need to create a new file `messages_%LOCALE_NAME%.properties`,
where [%LOCALE_NAME%](https://en.wikipedia.org/wiki/IETF_language_tag) is the localization name,
and also add an image for the drop-down list into `resources/images`.\
There are several profiles available for launching the application:
- `ignore-notifications` - disables sending notifications by mail and disables usage of the mail server settings.
- `no-password` - disables custom input of a password and always sets it with a default value (for test/demo)
- `uuid-password` - disables custom input of a password and generates it randomly based on **UUID**, after which
sends it within a notification (if notification sending is not disabled)

[Settings file](./src/main/resources/config.properties) **MUST** be edited depends on environment where this application
will be started.\
Description of all necessary settings:
```properties
# Comma separated profiles for application behavior. Described above. 
spring.profiles.active=
# Duration for participant's 'password reset' action
app.action-token-duration=2H
# keystore with a secret key to encrypt/decrypt any data within DB
app.crypto.ks-location=classpath:keystore-secret.jks
# password to 'ks-location'
app.crypto.ks-password=default
# alias of the key contained in 'ks-location'
app.crypto.ks-key-alias=key-secret
# key password for a 'ks-key-alias'
app.crypto.ks-key-password=default
# if no suitable key provided above, then this salt will be used as an AES key to encrypt/decrypt any data within DB
app.crypto.key-salt=any_value
# password which will be set for all participants if 'no-password' spring profile is active
app.default-pass=0
# email sender for notifications
app.mail.from=secret_santa_no_reply@my-office.com
# delay in seconds between attempts to send failed mail notifications, default 30
app.mail.resend-delay=30
# number of attempts to send failed mail notifications (if set to -1 the attempts will be infinite), default 1
app.mail.resend-attempts=1
# regex-pattern for allowed participant's email format, default .*
app.mail.allowed-regex=.*
# validation error message for participant's email
app.mail.allowed-err-message=email '%s' does not belong to any corporate domain @my-office.com / @my-office.org
# DNS or IP of this app (e.g. https://secret-santa.my-office.com) - will be used within notifications
app.server.url=http://${server.address}:${server.port}
# the only way to grant/revoke 'superadmin' role is through the following options during startup:
# list of users which role will be set to 'superadmin'
app.superadmins="{'user1@my-office.com','user2@my-office.com'}"
# list of users which role will be set to 'user'
app.users="{'user3@my-office.com','user4@my-office.com'}"
# Settings for servlet container. SSL is disabled by default. If TLS connection is needed
# then `enabled: true` must be enabled and certificate+private key should be added to 'config' dir
server.address=localhost
server.port=8080
server.ssl.enabled=false
server.ssl.certificate=classpath:certificate-ec.crt
server.ssl.certificate-private-key=classpath:private-ec-key.pem
# The name of the database and the user under whom the new database and all the necessary tables will be created.
spring.datasource.db-name=santa-db
spring.datasource.username=admin
spring.datasource.password=secretpass
# Settings for connecting to the mail server.
# They will be used to send notifications.
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

### Application structure
**com.github.upperbound.secret_santa.**
- **config** - all application settings and
  [PostInitializer](./src/main/java/com/github/upperbound/secret_santa/config/PostInitializer.java) (for superadmin role)
- **model** - ORM layer
- **repository** - DAO layer
- **service** - all application's business logic, where users are being created,
results are being drawn, notifications are being sent, etc.
- **util** - common utility classes
- **web** - all web controllers that process **GET** and **POST** requests by invoking business logic

Roles info:
- **USER** - it is set by default. When creating a new group, the user who creates it becomes its administrator
(has the ability to edit the group, the number of the participants and can draw the results). Administrator
has the ability to grant/revoke this role to any other user in this group.
- **SUPERADMIN** - it can only be installed when the application is launched, all tabs and all actions
in all groups are available (you can also remove this role only at startup by passing this user as **app.users**)
