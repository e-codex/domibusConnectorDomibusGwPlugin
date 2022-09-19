FROM java:openjdk-8-ubi8
ENV CATALINA_HOME=/app/domibus
ENV DOMIBUS_CONFIG_LOCATION=/app/domibus/conf/domibus
ENV DOMIBUS_WORK_LOCATION=/app/data/work
ENV CATALINA_TMPDIR=/app/temp/temp
COPY app /app/
USER root
RUN echo "export CATALINA_OPTS=\"-Ddomibus.config.location=$DOMIBUS_CONFIG_LOCATION -Ddomibus.work.location=$DOMIBUS_WORK_LOCATION\"" >> /app/run.sh && \
    echo "mkdir -p /app/temp/webapps && mkdir -p /app/temp/tomcat && mkdir -p /app/temp/conf/Catalina" >> /app/run.sh && \
    echo "ln -s /app/domibus/deployments/domibus.war /app/temp/webapps/domibus.war" >> /app/run.sh && \
    echo "java -jar /app/tools/ecodex-lab-box-gw-liquibase-init.jar" >> /app/run.sh && \
    echo "exec /app/domibus/bin/catalina.sh \$@" >> /app/run.sh
RUN chmod +x /app/run.sh
RUN chmod +x /app/domibus/bin/*.sh
RUN mkdir /app/data && mkdir /app/data/work
RUN mkdir /app/logs && rm -rf /app/domibus/logs && ln -s /app/logs /app/domibus/logs
RUN mkdir /app/temp && rm -rf /app/domibus/temp && mkdir /app/temp/temp && ln -s /app/temp/temp /app/domibus/temp
RUN mv /app/domibus/webapps /app/domibus/deployments && ln -s /app/temp/webapps /app/domibus/webapps
RUN mkdir -p /app/temp/tomcat && ln -s /app/temp/tomcat /app/domibus/work
RUN ln -s /app/temp/conf/Catalina /app/domibus/conf/Catalina
RUN groupadd --system domibus && useradd --system -g domibus domibus && chown -R domibus:domibus /app
WORKDIR /app
USER domibus
EXPOSE 8080
VOLUME /app/data
VOLUME /app/logs
VOLUME /app/temp
CMD ["/bin/sh","/app/run.sh", "run"]