#!/bin/sh
#Please change CATALINA_HOME to the right folder
#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>
#JAVA_OPTS="$JAVA_OPTS -Xms4096m -Xmx4096m -Ddomibus.config.location=$CATALINA_HOME/conf/domibus"
#
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.url='${DOMIBUS_DATASOURCE_URL}'"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.driverClassName=${DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.user=${DOMIBUS_DATASOURCE_USER}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.datasource.password=${DOMIBUS_DATASOURCE_PASSWORD}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.config.location=${DOMIBUS_CONFIG_LOCATION}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.work.location=${DOMIBUS_WORK_LOCATION}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.entityManagerFactory.jpaProperty.hibernate.dialect=${DOMIBUS_DATASOURCE_HIBERNATE_DIALECT}"
CATALINA_OPTS="${CATALINA_OPTS} -Ddomibus.security.key.private.alias=${GW_PRIVATE_KEY_ALIAS}"

#CATALINA_OPTS="${CATALINA_OPTS} -Dgw01.url=http://localhost:8080/domibus/services/msh"
