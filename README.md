oAuth2-sample
=============

oAuth2 sample: auth-server, resource server and client.

Here you can see 3 WARs: the authentication server, a resource server, and a client app.

Works over spring-security-oauth version M6 (milestone-6).
later on i will upgrade to a release-candidate.



Deploy all 3 WARs on a servlet container, e.g. tomcat.

Browse http://localhost:8080/oauth2-client/hello

The client needs a login by itsealf : admin/admin (future release will avoid this thing).

Then it will try to call the resource-server url http://localhost:8080/oauth2-resource-server/welcome

This will redirect to oauth2.0 authentication server.

Login to authentication-server, 
- currently it is from mem: demo@watchdox.com/demo
in future releases it will read from a DB.

Then the client should access the resource server using the access-token, and print a message.


KeyStore things to know:
========================
1. a keystore shall be created, both for SSL and for signing the tokens.
2. its alias and password should be updated in the prop file as well as in the tomcat's server.xml
3. algorithm should be DSA (because in the access-token signature my code expects it to be "SHA1withDSA"
4. if you want to work with "localhost", you should make the name "localhost": 
5. http://stackoverflow.com/questions/6908948/java-sun-security-provider-certpath-suncertpathbuilderexception-unable-to-find/12146838#12146838

creating a token using Java's keytool:
keytool.exe -genkeypair -alias <alias> -keypass <key-password> -keyalg DSA -keystore <file-name> -storepass <ks-password> -storetype JCEKS -v


HTML forms:
onSubmit vs action