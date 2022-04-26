FROM java:openjdk-8-ubi8
ENV CATALINA_HOME=/app/domibus JAVA_EXT_OPTS=-XX:MetaspaceSize=256M JAVA_OPTS=""
ENV DOMIBUS_CONFIG_LOCATION=/app/domibus/conf/domibus
ENV DOMIBUS_WORK_LOCATION=/app/data/work
COPY app /app/
USER root
RUN echo "export JAVA_OPTS=\"$JAVA_OPTS -Ddomibus.config.location=$DOMIBUS_CONFIG_LOCATION -Ddomibus.work.location=$DOMIBUS_WORK_LOCATION\"" >> /app/run.sh && \
    echo "/bin/bash /app/domibus/bin/catalina.sh run" >> /app/run.sh \
RUN mkdir /app/work
RUN mkdir /app/logs && rm -rf /app/domibus/logs && ln -s /app/logs /app/domibus/logs
RUN mkdir /app/temp && rm -rf /app/domibus/temp && mkdir /app/temp/temp && ln -s /app/temp/temp /app/domibus/temp
RUN mkdir /app/data && mv /app/domibus/webapps /app/domibus/deployments && ln -s /app/temp/webapps /app/domibus/webapps && ln -s /app/domibus/deployments/domibus.war /app/temp/webapps/domibus.war
RUN groupadd --system domibus && useradd --system -g domibus domibus && chown -R domibus:domibus /app
WORKDIR /app
USER domibus
EXPOSE 8080
VOLUME /app/data
VOLUME /app/logs
VOLUME /app/temp
CMD ["/bin/sh","/app/run.sh"]