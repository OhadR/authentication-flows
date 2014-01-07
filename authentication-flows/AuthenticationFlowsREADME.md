Authentication-Flows
==================

The Authentication-Flows JAR implements all authentication flows: 
* create account, 
* forgot password, 
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
POST /j_spring_security_check HTTP/1.1
	j_username=<email>, 
	j_password=<password>
```

**create Account**

```xml
POST /createAccount HTTP/1.1
	email=<email>, 
	password=<password>,
	confirm_password=<confirm_password>
```

**forgot Password**
```xml
POST /forgotPasswordPage HTTP/1.1
	email=<email>
```

**set new Password**
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

JAR: common-crypto
-------------
authentication-flows JAR uses cryptography in order to encrypt the data. Read about it in the [main README](/).
