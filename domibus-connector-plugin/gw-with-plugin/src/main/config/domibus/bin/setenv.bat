

IF NOT DEFINED lab.id SET "lab.id=01"

set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw.self=gw%lab.id%"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlab.id=%lab.id%"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw01.url=http://localhost:8080/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw02.url=http://localhost:8020/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw03.url=http://localhost:8030/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw04.url=http://localhost:8040/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw05.url=http://localhost:8050/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw06.url=http://localhost:8060/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw07.url=http://localhost:8070/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw08.url=http://localhost:8080/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw09.url=http://localhost:8090/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw10.url=http://localhost:8100/domibus/services/msh"

REM 1199
set "CATALINA_OPTS=%CATALINA_OPTS% -DactiveMQ.connectorPort=1199"
REM tcp://localhost:61616"
set "CATALINA_OPTS=%CATALINA_OPTS% -DactiveMQ.transportConnector.uri=tcp://localhost:61616"

REM tomcat server port
set "CATALINA_OPTS=%CATALINA_OPTS% -Dtomcat.port.http=8080"
REM tomcat shutdown port
set "CATALINA_OPTS=%CATALINA_OPTS% -Dtomcat.port.shutdown=8009"

set "CATALINA_OPTS=%CATALINA_OPTS%  -Dorg.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource"