@echo Running Maven Build WITHOUT tests... For development purposes only !
set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=512M

rem call mvn clean install eclipse:clean eclipse:eclipse -DskipTests -DdownloadSources=true

call mvn clean install -DskipTests -DdownloadSources=true
