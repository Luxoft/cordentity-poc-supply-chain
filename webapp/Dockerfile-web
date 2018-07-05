FROM frolvlad/alpine-oraclejdk8
MAINTAINER Alexey Koren
COPY ./build/libs/webapp.jar webapp.jar
ENTRYPOINT ["/usr/bin/java"]
CMD ["-agentlib:jdwp=transport=dt_socket,suspend=n,server=y", \
     "-jar", \
     "webapp.jar"]