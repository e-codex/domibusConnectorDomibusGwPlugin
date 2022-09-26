FROM java:openjdk-8-ubi8
ENV CATALINA_HOME=/app/domibus
ENV DOMIBUS_CONFIG_LOCATION=/app/domibus/conf/domibus
ENV DOMIBUS_WORK_LOCATION=/app/data/work
ENV CATALINA_TMPDIR=/app/temp/catalina_temp

ENV DOMIBUS_DATASOURCE_URL='jdbc:h2:file:/app/data/work/database;AUTO_SERVER=TRUE;MODE=LEGACY;NON_KEYWORDS=KEY,VALUE'
ENV DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
ENV DOMIBUS_DATASOURCE_USER=edelivery
ENV DOMIBUS_DATASOURCE_PASSWORD=edelivery
ENV DOMIBUS_DATASOURCE_HIBERNATE_DIALECT=org.hibernate.dialect.H2Dialect

COPY app /app/
USER root

RUN mkdir /app/data && mkdir /app/data/work && \
    mkdir /app/logs && rm -rf /app/domibus/logs && ln -s /app/logs /app/domibus/logs && \
    mkdir /app/temp && rm -rf /app/domibus/temp && mkdir /app/temp/temp && ln -s /app/temp/temp /app/domibus/temp && \
    mv /app/domibus/webapps /app/domibus/deployments && \
    ln -s /app/temp/webapps /app/domibus/webapps && \
    mkdir -p /app/temp/tomcat && ln -s /app/temp/tomcat /app/domibus/work
#    ln -s /app/temp/conf/Catalina /app/domibus/conf/Catalina

RUN groupadd --system domibus && useradd --system -g domibus domibus && chown -R domibus:domibus /app


RUN echo "#!/bin/bash" >> /app/run.sh && \
    echo "mkdir -p /app/temp/webapps && mkdir -p /app/temp/tomcat && mkdir -p /app/temp/conf/Catalina" >> /app/run.sh && \
#    echo "ln -s /app/domibus/deployments/domibus.war /app/temp/webapps/domibus.war" >> /app/run.sh && \
    echo "java -jar /app/tools/gw-liquibase-db-init.jar" >> /app/run.sh && \
    echo "exec /app/domibus/bin/catalina.sh \$@" >> /app/run.sh
RUN chmod +x /app/run.sh
RUN chmod +x /app/domibus/bin/*.sh

WORKDIR /app
USER domibus
EXPOSE 8080
VOLUME /app/data /app/logs /app/temp

CMD ["/bin/sh","/app/run.sh", "run"]