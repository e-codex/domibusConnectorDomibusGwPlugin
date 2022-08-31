#!/bin/sh
#Please change CATALINA_HOME to the right folder
#export CATALINA_HOME=<YOUR_INSTALLATION_PATH>
#JAVA_OPTS="$JAVA_OPTS -Xms4096m -Xmx4096m -Ddomibus.config.location=$CATALINA_HOME/conf/domibus"

JAVA_OPTS="$JAVA_OPTS -Ddomibus.database.serverName=$DOMIBUS_DATABASE_SERVER_NAME -Ddomibus.database.port=$DOMIBUS_DATABASE_PORT -Ddomibus.database.schema=$DOMIBUS_DATABASE_SCHEMA"
