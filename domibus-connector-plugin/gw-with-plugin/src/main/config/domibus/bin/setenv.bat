

IF NOT DEFINED lab.id SET "lab.id=01"

set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw.self=gw%lab.id%"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dlab.id=%lab.id%"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw01.url=http://localhost:8080/domibus/services/msh"
set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw02.url=http://localhost:8020/domibus/services/msh"