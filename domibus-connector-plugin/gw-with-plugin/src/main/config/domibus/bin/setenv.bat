

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