Authentication-Flows   [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ohadr/authentication-flows/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ohadr/authentication-flows)
==================

The Authentication-Flows JAR implements all authentication flows: 
* [create account](https://github.com/OhadR/oAuth2-sample/tree/master/authentication-flows#create-account-flow), 
* [forgot password](https://github.com/OhadR/oAuth2-sample/tree/master/authentication-flows#forgot-password-flow), 
* change password by user request, 
* force change password if password is expired,
* locks the accont after pre-configured login failures.

To make it serious, authentication-flows JAR uses cryptography in order to encrypt the data in the links that are sent to the user's email, 
upon user's registration and "forget password" flows.

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