@ECHO OFF

IF "%1%"=="" (
SET choice=
) ELSE (
SET choice=%1
GOTO NO_MENU
)

:start
ECHO.
ECHO **************** DevOps tasks *******************
ECHO.
ECHO Choice Action
ECHO.
ECHO 1      gradle clean install
ECHO 2      Clean Tomcat/webapps and Copy new WARs
ECHO 13     Start tomcat
ECHO q      Quit without any action
ECHO.
SET choice=
SET /p choice=Select action from the menu:
IF not '%choice%'=='' SET choice=%choice:~0,2%
SET GOTOSTART=TRUE
:NO_MENU
IF '%choice%'=='1' GOTO run_build_all
IF '%choice%'=='2' GOTO copy_to_tomcat
IF '%choice%'=='13' GOTO run_start_tomcat
IF '%choice%'=='q' GOTO run_quit
IF '%choice%'=='Q' GOTO run_quit
ECHO "%choice%" is not valid please try again
ECHO.
GOTO start

:run_build_all
call gradle clean build
GOTO start
 
:copy_to_tomcat
del C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\webapps\* /q
rmdir C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\webapps\oauth2-auth-server /s /q
ECHO copy artifacts and omit the version (use * /F /Y)
echo https://stackoverflow.com/questions/3018289/xcopy-file-rename-suppress-does-xxx-specify-a-file-name-message
xcopy oauth2-client\build\libs\oauth2-client-2.2-SNAPSHOT.war C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\webapps\oauth2-client.war*
xcopy oauth2-resource-server\build\libs\oauth2-resource-server-2.2-SNAPSHOT.war C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\webapps\oauth2-resource-server.war*
xcopy oauth2-auth-server\build\libs\oauth2-auth-server-2.2-SNAPSHOT.war C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\webapps\oauth2-auth-server.war*

GOTO start


:run_start_tomcat
ECHO ============== run Tomcat ==============
ECHO ============================================
pushd C:\Ohad\Dev\Tools\apache-tomcat-win64-7.0.47\bin
call startup.bat
popd
GOTO start


:run_quit
GOTO quit

:quit