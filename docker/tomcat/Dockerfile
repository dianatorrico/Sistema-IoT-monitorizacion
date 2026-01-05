FROM tomcat:10.1-jdk17

# Borrar apps por defecto
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el contexto (pool de conexiones JDBC)
COPY conf/context.xml /usr/local/tomcat/conf/context.xml

# Copiar tu aplicación Java (WAR)
COPY app/ROOT.war /usr/local/tomcat/webapps/ROOT.war
