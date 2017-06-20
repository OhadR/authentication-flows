common-crypto   [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ohadr/common-crypto/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ohadr/common-crypto)
==================
**Common-Crypto is a powerful and customizable framework that provides encryption/decryption and encoding/decoding.**

It uses a keystore, and it can even create a new one on the fly if such does not exist.


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

2. Spring's Component-Scan
--------------
the XML should contain to the component-scan path the following paths:
<pre>
com.ohadr.crypto.*
</pre>




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
   
        