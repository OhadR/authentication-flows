Authentication-Flows   [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ohadr/authentication-flows/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ohadr/authentication-flows)
==================
**Authentication-Flows is a powerful and highly customizable framework that covers all flows that authentication-server 
that is based on Spring-Security needs.**

Authentication-Flows is a framework that focuses on providing all flows of authentication to Java applications built on Spring Security. 
It saves from the application developer all the bother in developing and maintaining the code such as create account, forgot password, etc. 
Like a Spring project, the real power of Authentication-Flows is found in how easily it can be extended to meet custom requirements

it is completely secured. It uses a key-store (KS) to encrypt the passwords. In addition, this KS is used to encrypt the data that is sent 
to the user upon registration, account-locking, etc. This KS is customizable by the customer.




The Authentication-Flows JAR implements all authentication flows: 
* [create account](https://github.com/OhadR/oAuth2-sample/tree/master/authentication-flows#create-account-flow), 
* [forgot password](https://github.com/OhadR/oAuth2-sample/tree/master/authentication-flows#forgot-password-flow), 
* change password by user request, 
* force change password if password is expired,
* locks the accont after pre-configured login failures.

To make it serious, authentication-flows JAR uses cryptography in order to encrypt the data in the links that are sent to the user's email, 
upon user's registration and "forget password" flows.

23-02-2016: Spring Versions Updated
---------------------------
On 23-02-2016, we have updated Spring versions to the newest!
* Spring Security: 4.0.3.RELEASE
* Spring: 4.2.4.RELEASE

In addition, we have changed the build tool from Maven to **Gradle**. If you wish to use the older version, i.e. Maven and older Spring versions (3.1.X, oAuth 1.0.5), you can find
it on a separated branch. The version in that branch is 1.6.2-SNAPSHOT (you can find in Maven Central the latest release, 1.6.2). The version on Master is 2.0.0-SNAPSHOT.


Configuration
=============
The client-app is responsible for all configurations. Here are the required configurations:

1. Client's [Spring-Beans.XML](client/src/main/webapp/WEB-INF/spring-servlet.xml)
---------------------------
**1.1. Component-Scan**

the XML should contain to the component-scan path the following paths:
<pre>
com.ohadr.auth_flows.*
com.ohadr.crypto.*
</pre>

**1.2. password encoder**

add bean in the spring XML. it is in use in the `UserActionController`.

```xml
	<sec:authentication-manager alias="authenticationManager">
		<sec:authentication-provider user-service-ref="userDetailsService" >
			<sec:password-encoder hash="sha-256">
				<sec:salt-source user-property="username"/>
			</sec:password-encoder>
		</sec:authentication-provider>
	</sec:authentication-manager>

	<bean id="passwordEncoder" 	class="org.springframework.security.authentication.encoding.ShaPasswordEncoder">
		<constructor-arg value="256"/>
	</bean>
```

```xml
	<sec:form-login 
			login-page="/login/login.htm" 
			authentication-success-handler-ref="authenticationSuccessHandler"
			authentication-failure-handler-ref="authenticationFailureHandler" />
```


**1.3. authentication success handler**

add this to the `<form-login>` block:
<pre>
	authentication-success-handler-ref="authenticationSuccessHandler"
</pre>
after a successful login, we need to check whether the user has to change hos password (if it is expired).

**1.4. authentication failure handler**

add this to the `<form-login>` block:
<pre>
	authentication-failure-handler-ref="authenticationFailureHandler"
</pre>

and this bean:

```xml
	<bean id="authenticationFailureHandler" class="com.ohadr.auth_flows.core.AuthenticationFailureHandler">
		<constructor-arg value="/login/login.htm?login_error=1"/>
		<property name="accountLockedUrl" value="/login/accountLocked.htm" />
	</bean>
```

**1.5. velocity - for better emails...**

issue https://github.com/OhadR/oAuth2-sample/issues/31 : read content of emails from a file. For this, we use [velocity](http://velocity.apache.org/).

```xml
    <bean id="velocityEngine" class="org.springframework.ui.velocity.VelocityEngineFactoryBean">
        <property name="velocityProperties">
            <value>
                resource.loader=class
                class.resource.loader.class=org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
            </value>
        </property>
    </bean>
```


2. Database
----------
need to declare on dataSource bean, that is the connection to the DB.
The connection properties are in client.properties.
The client is responsible for creating a schema named 'auth-flows' in the DB (The schema name can be different, of course, but it should
be changed in client.properties respectively). In this schema, there are tables, created using the following scripts:

<pre>
CREATE SCHEMA `auth-flows`
</pre>

**2.1. TABLE: policy**

<pre>
CREATE TABLE `auth-flows`.`policy` (
  `POLICY_ID` int(10) unsigned NOT NULL,
  `PASSWORD_MIN_LENGTH` int(11) DEFAULT NULL,
  `PASSWORD_MAX_LENGTH` int(11) DEFAULT NULL,
  `PASSWORD_MIN_UPCASE_CHARS` int(11) DEFAULT NULL,
  `PASSWORD_MIN_LOCASE_CHARS` int(11) DEFAULT NULL,
  `PASSWORD_MIN_NUMERALS` int(11) DEFAULT NULL,
  `PASSWORD_MIN_SPECIAL_SYMBOLS` int(11) DEFAULT NULL,
  `PASSWORD_BLACKLIST` longtext,
  `MAX_PASSWORD_ENTRY_ATTEMPTS` int(11) DEFAULT NULL,
  `PASSWORD_LIFE_IN_DAYS` int(11) DEFAULT NULL,
  `REMEMBER_ME_VALIDITY_IN_DAYS` int(11) DEFAULT NULL,
  PRIMARY KEY (`POLICY_ID`)
)
</pre>

Note: in order to get the project up and running, you will have to insert an initial data to the `policy` table. The minimal set that is essential can 
be set using the below statement:

<pre>
INSERT INTO `auth-flows`.`policy` (`POLICY_ID`, `PASSWORD_MAX_LENGTH`, `PASSWORD_BLACKLIST`, `PASSWORD_LIFE_IN_DAYS`) VALUES (1, 10, "", -1);
</pre>

Needless to mention, you can add more "columns" (which means adding more constraints on the password the user enters), but the above is the minimum required. 

If you leave `PASSWORD_LIFE_IN_DAYS` NULL, after each successful login the framework will redirect the user to "change password" page, as it detects his current 
password as expired. Note that value of -1 means ETERNAL_PASSWORD.

If you leave `MAX_PASSWORD_ENTRY_ATTEMPTS` NULL, the user will have only a single attempt to login before he gets locked up.


**2.2. TABLE: users**

<pre>
CREATE  TABLE `auth-flows`.`users` (
  `USERNAME` VARCHAR(50) NOT NULL ,
  `PASSWORD` VARCHAR(100) NOT NULL ,
  `ENABLED` TINYINT(1)  NOT NULL DEFAULT 1 ,
  `LOGIN_ATTEMPTS_COUNTER` INT NOT NULL DEFAULT 0,
  `LAST_PSWD_CHANGE_DATE` DATETIME NOT NULL, 
  `FIRSTNAME` VARCHAR(30) ,
  `LASTNAME` VARCHAR(30) ,
  `AUTHORITIES` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`USERNAME`) ,
  UNIQUE INDEX `idusers_UNIQUE` (`USERNAME` ASC) 
  )
</pre>
  
It is used by `JdbcAuthenticationAccountRepositoryImpl` class.

Important note: in `JdbcUserDetailsManager`, Spring expects the table-name is 'user', and that 
a column called 'username' for the authentication exists. Unless a derived class changes it, these values
must remain.
In my solution I try to keep it simple and stay as close as I can to Spring' implementation, so even though
I wanted the column name to be 'email' - I had to rename it back to 'username' in order to stay as close
as possible to Spring.

**2.3. TABLE: links** (from version 2.1-RELEASE)

<pre>
CREATE TABLE `auth-flows`.`links` (
  `LINK` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`LINK`)
);
</pre>

In init-db.sql there are all the scripts above.

API
---------

The Authentication-Flows JAR is as generic as it can be, yet it is coupled with UI, because it gets its inputs from UI forms, and sends data
to UI pages. Here is a description of the interface of the module. The client application (that implements the UI) can send the data in any
way it wants - forms, REST, etc. 

**login**

As Spring requires, the login form should include j_username and j_password:
```xml
x-www-form-urlencoded
POST /j_spring_security_check HTTP/1.1
	j_username=<email>, 
	j_password=<password>
```
Return codes:

* After successful login, Spring redirects the user-agent to the desired resource, with return code of 301.
* Failed Authentication returns 302.

If REST capabilities are on (see appendix), then return values are different, since redirects are not in use.
* successful authentication returns 200.
* Failed Authentication returns 401 (Unauthorized).

**logout**

```xml
/j_spring_security_logout HTTP/1.1
```

**create Account**

```xml
x-www-form-urlencoded
POST /createAccount HTTP/1.1
	email=<email>, 
	password=<password>,
	confirm_password=<confirm_password>,
	firstName=<firstName>,     //optional
	lastName=<lastName>        //optional
```
* if successful, redirects user-agent to "accountCreatedSuccess.jsp", with return code of 301.
* if failed, redirects back to the same page (createAccount.jsp), with return code of 301.

```xml
x-www-form-urlencoded
POST /rest/createAccount HTTP/1.1
	email=<email>, 
	password=<password>,
	confirm_password=<confirm_password>,
	firstName=<firstName>,     //optional
	lastName=<lastName>        //optional
```
* if successful, returns code 201 (Created).
* if failed, returns 400 (Bad request).


**forgot Password**
```xml
POST /forgotPasswordPage HTTP/1.1
	email=<email>
```
* if successful, redirects user-agent to "passwordRestoreEmailSent.jsp", with return code of 301.
* if failed, redirects user-agent to "error.jsp", with return code of 301.

**set new Password**

Default file name: setNewPassword.jsp

This JSP contains a form, that upon submission generates the following:

```xml
POST /setNewPassword HTTP/1.1
	password=<password>,
	confirm_password=<confirm_password>
```

**change Password** 
```xml
POST /setNewPassword HTTP/1.1
	current_password=<current_password>,
	new_password=<password>,
	confirm_password=<confirm_password>
```


**accountCreatedSuccess.jsp**

**AccountActivated.htm**

**passwordRestoreEmailSent.jsp**

**passwordSetSuccess.jsp**

JAR: common-crypto
-------------
authentication-flows JAR uses cryptography in order to encrypt the data. Read about it in the [main README](/).


Create Account Flow
-------------
1. On the login form, there is a link for "create account" AKA register.

2. The 'create account' screen in a simple form, where the user can enter his email address and password. In our implementation, 
the email address is the username, so each user has a valid email address attached to their account. 

3. User presses "Submit". There are several validations:
      1. validate email address (e.g. '@' must exist, and a domain). Other validations
      2. validate password matches the policy.
      3. validate retype password.
      4. custom validations - the framework allows to customize and add extra validations. For example, validate that the registered
      email address is from "nice.com" domain.

4. If all validations passed, we check in the DB whether this email address already exists. 
    1. If it does exists, and the account is active, a "user already exists" exception is raised. 
    2. If it is exists but inactive, the account is deleted.

5. Account is created in the DB.

6. The framework calls to custom post-create-account endpoint, if exists.

7. We generate a link using a crypto-library, and send the user an email containing this link. This link consists of the link-creation 
time and the username. Hacker that intercepts this link cannot decrypt it so he cannot set a new password for another user.

8. Account creation is successful.
    1. If it is a REST flow, return 201.
    2. If it is MVC flow, redirect the client to a page with a message of "account was created successfully, an email was sent to your inbox".

9. The user receives the email and clicks the link. If a configurable expiration time has not elapsed, and if the link is valid,
this takes them to a "account activated successfully" page, with link to login page.

Forgot Password Flow
-------------
1. On the login form, there is a link for "forgot password"

2. The forgot password screen in a simple textbox, where the user can enter his email address. In our implementation, 
the email address is the username, but it shouldn't matter as long as each user has a valid email address attached to their account.

3. User enters an email address and presses "Submit".

  1. IF the email address is associated with a valid account, we generate a link using a crypto-library, and send the user an email
containing this link. This link consists of the link-creation time and the username. 
Hacker that intercepts this link cannot decrypt it so he cannot set a new password for another user.

  2. IF the email address does not exist, we do nothing. No email is sent to anyone. Server returns message "account is locked
or does not exist". Even though the server distinguishes between these cases, we do not want to specify the exact reason of
failure, in order to avoid account-harvesting by hackers. (hacker will not know whether the account does not exist, or exist but locked.
From this reason, maybe a better way is to show the same output as in 3.1. - that email was sent to the given address).

4. Regardless if the email address is correct or not, we always show a "Thanks, if the email address you entered is correct, 
you will be receiving an email shortly with instructions on how to reset your password". This is important as you don't want a bad user 
using this form to try and discover user names.

5. The user receives the email and clicks the link. If a configurable expiration time has not elapsed, and if the link is valid,
this takes them to a reset password screen (with a new password/confirm new password textboxes). If it is not, we show a 
"this reset link is no longer valid" if the key is expired or does not exist.

6. After reset, user is redirected to login screen to login to the application.