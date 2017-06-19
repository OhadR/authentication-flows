common-crypto   [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ohadr/common-crypto/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ohadr/common-crypto)
==================
**TODO Authentication-Flows is a powerful and highly customizable framework that covers all flows that authentication-server 
that is based on Spring-Security needs.**

TODO Authentication-Flows is a framework that focuses on providing all flows of authentication to Java applications built on Spring Security. 
It saves from the application developer all the bother in developing and maintaining the code such as create account, forgot password, etc. 
Like a Spring project, the real power of Authentication-Flows is found in how easily it can be extended to meet custom requirements

it is completely secured. It uses a key-store (KS) to encrypt the passwords. In addition, this KS is used to encrypt the data that is sent 
to the user upon registration, account-locking, etc. This KS is customizable by the customer.




Configuration: 
=======
The client-app is responsible for all configurations. Here are the required configurations:

1. properties TODO
---------------------------
**1.1. Componecan**

the XML should contain to the component-scan path the following paths:
<pre>
com.ohadr.auth_flows.*
com.ohadr.crypto.*
</pre>

**1.2. passworoder**

add bean in the spring XML. it is in use in the `UserActionController`.

```xml
	<sec:authentication-manager alias="authenticationManager">
		<sec:authentication-provider user-service-ref="userDetailsService" >
			<sec:password-encoder hash="sha-256">
				<sec:salt-source user-property="username"/>
			</sec:password-encoder>
		</sec:authentication-provider>
	</sec:authentication-manager>




API
==

** PublicKeyController **

```xml
x-www-form-urlencoded
GET /publicKey HTTP/1.1
```
Return codes:

* the public key.



   +------------------+     +------------------+       +------------------+     
   |  crypto-service  |---->|  cryptoUtilImpl  |---+-->|  cryptoProvider  |	
   +------------------+     +------------------+   |   +------------------+
                                                   |
                                                   |   +-------------------+
                                                   +-->|  cryptoProperties |
                                                       +-------------------+
   
   
        