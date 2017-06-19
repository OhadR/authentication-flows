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

1. properties 
--------------
**com.ohadr.crypto.keystore**

path to the keystore file. example: C:/Ohad/Dev/Projects/rest_login/ohad.ks

**com.ohadr.crypto.password**

the keystore file password.

**com.ohadr.crypto.keyAlias**

the keystore file alias.

**com.ohadr.crypto.createKeystoreFileIfNotExist**

a flag indicates whether to auto create a keystore file if such does not already exist.
if this jar is used where files (keystore is a file...) cannot be stored (GAE, for example), then this flag should be set to false. Then,
the framework will not try to store the key-store file, but will work in-mem.





API
====

** PublicKeyController **

```xml
x-www-form-urlencoded
GET /publicKey HTTP/1.1
```
Return codes:

* the public key.


<pre>
   +------------------+     +------------------+       +------------------+     
   |  crypto-service  |---->|  cryptoUtilImpl  |---+-->|  cryptoProvider  |	
   +------------------+     +------------------+   |   +------------------+
                                                   |
                                                   |   +-------------------+
                                                   +-->|  cryptoProperties |
                                                       +-------------------+
   
</pre>
   
        