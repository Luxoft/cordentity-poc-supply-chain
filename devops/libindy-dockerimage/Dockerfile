FROM ubuntu:16.04
MAINTAINER Alexey Koren


# This is required for https Sovrin repo and Luxoft certs update
RUN apt-get update
RUN apt-get install apt-transport-https software-properties-common -y

# Install Luxoft certificates
COPY luxoft /usr/local/share/ca-certificates/
RUN update-ca-certificates
RUN apt-get update

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer


# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Install libindy from Sovrin repo
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88
RUN add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial stable"
RUN apt-get update -y
RUN apt-get install -y libindy=1.5.0


# Define default command.
CMD ["bash"]