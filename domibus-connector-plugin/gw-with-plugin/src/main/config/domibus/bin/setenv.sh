#!/bin/sh
#Please change CATALINA_HOME to the right folder
#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>
#JAVA_OPTS="$JAVA_OPTS -Xms4096m -Xmx4096m -Ddomibus.config.location=$CATALINA_HOME/conf/domibus"
#
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.url='${DOMIBUS_DATASOURCE_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.driverClassName='${DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.user='${DOMIBUS_DATASOURCE_USER}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.password='${DOMIBUS_DATASOURCE_PASSWORD}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.config.location='${DOMIBUS_CONFIG_LOCATION}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.work.location='${DOMIBUS_WORK_LOCATION}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.entityManagerFactory.jpaProperty.hibernate.dialect='${DOMIBUS_DATASOURCE_HIBERNATE_DIALECT}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.security.key.private.alias='${GW_PRIVATE_KEY_ALIAS}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw.self='${GW_SELF}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dlab.id='${LAB_ID}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw01.url='${GW01_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw02.url='${GW02_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw03.url='${GW03_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw04.url='${GW04_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw05.url='${GW05_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw06.url='${GW06_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw07.url='${GW07_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw08.url='${GW08_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw09.url='${GW09_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dgw10.url='${GW10_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Dconnector.delivery.service.address='${CONNECTOR_DELIVERY_SERVICE_ADDRESS}'"



#CATALINA_OPTS="${CATALINA_OPTS} -Dgw01.url=http://localhost:8080/domibus/services/msh"


#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw.self=gw%lab.id%"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dlab.id=%lab.id%"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw01.url=http://localhost:8080/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw02.url=http://localhost:8020/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw03.url=http://localhost:8030/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw04.url=http://localhost:8040/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw05.url=http://localhost:8050/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw06.url=http://localhost:8060/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw07.url=http://localhost:8070/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw08.url=http://localhost:8080/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw09.url=http://localhost:8090/domibus/services/msh"
#set "CATALINA_OPTS=%CATALINA_OPTS% -Dgw10.url=http://localhost:8100/domibus/services/msh"