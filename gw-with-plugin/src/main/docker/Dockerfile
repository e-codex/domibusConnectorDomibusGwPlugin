FROM tomcat:9-jdk17


ENV DOMIBUS_CONFIG_LOCATION=${CATALINA_HOME}/conf/domibus/
ENV DOMIBUS_WORK_LOCATION=/app/domibus/work


ENV DOMIBUS_DATASOURCE_URL='jdbc:h2:file:/app/domibus/work/database;AUTO_SERVER=TRUE;MODE=LEGACY;NON_KEYWORDS=KEY,VALUE'
ENV DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
ENV DOMIBUS_DATASOURCE_USER=edelivery
ENV DOMIBUS_DATASOURCE_PASSWORD=edelivery
ENV DOMIBUS_DATASOURCE_HIBERNATE_DIALECT=org.hibernate.dialect.H2Dialect

ENV GW_PRIVATE_KEY_ALIAS=''

VOLUME /app/domibus/conf /app/domibus/work /usr/local/tomcat/temp /usr/local/tomcat/logs

COPY domibus/conf/Catalina/localhost/domibus.xml ${CATALINA_HOME}/conf/Catalina/localhost/domibus.xml
COPY domibus/bin/setenv.sh ${CATALINA_HOME}/bin
COPY domibus/deployments ${CATALINA_HOME}/deployments
COPY domibus/conf/domibus/ ${DOMIBUS_CONFIG_LOCATION}
COPY domibus/additional-libs ${CATALINA_HOME}/additional-libs
COPY domibus/lib/mariadb-java-client.jar ${CATALINA_HOME}/lib
COPY domibus/lib/h2.jar ${CATALINA_HOME}/lib
COPY domibus/lib/gw-tomcat-db-init-lifecycle-hook.jar ${CATALINA_HOME}/lib
COPY domibus/lib/mysql-connector-java.jar ${CATALINA_HOME}/lib
COPY domibus/lib/tomcat-lifecyclelistener.jar ${CATALINA_HOME}/lib




