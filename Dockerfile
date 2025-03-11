FROM openjdk:24-ea-17-bookworm AS build

# Install mongodb
RUN apt-get update && apt-get install -y gnupg curl
RUN curl -fsSL https://www.mongodb.org/static/pgp/server-8.0.asc | \
   gpg -o /usr/share/keyrings/mongodb-server-8.0.gpg \
   --dearmor
RUN echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] http://repo.mongodb.org/apt/debian bookworm/mongodb-org/8.0 main" | tee /etc/apt/sources.list.d/mongodb-org-8.0.list
RUN apt-get update
RUN apt-get install -y mongodb-org

# Install SpringBoot service
COPY target/*.jar app.jar
RUN echo "#!/bin/bash" > /start.sh
RUN echo "/usr/bin/mongod --config /etc/mongod.conf &" >> /start.sh
RUN echo "java -jar /app.jar" >> /start.sh
RUN ["chmod", "+x", "/start.sh"]
ENTRYPOINT ["/start.sh"]
EXPOSE 8080
