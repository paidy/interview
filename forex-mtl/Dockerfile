FROM openjdk:11-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Install Scala 2.13.12
RUN \
  curl -L https://github.com/sbt/sbt/releases/download/v1.9.0/sbt-1.9.0.tgz | tar xz -C /usr/local && \
  ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
                                                      sbt assembly


# Expose port 9090 for your service
EXPOSE 9090

COPY src/main/resources/application.conf ./application.conf

# Build your project
CMD ["java", "-Dconfig.file=/app/application.conf", "-jar", "target/scala-2.13/forex-assembly-1.0.1.jar"]