# Base image from (http://phusion.github.io/baseimage-docker)
FROM teamblockchain/libindyjava:1.5.0

ENV CORDA_VERSION=${BUILDTIME_CORDA_VERSION}

# Set image labels
LABEL net.corda.version = ${CORDA_VERSION}

# Install Java
# RUN apt-get update && \
#    apt-get install -y openjdk-8-jdk

# Add user
RUN addgroup corda && \
    useradd -g corda corda && \
    # Create /opt/corda directory && \
    mkdir -p /opt/corda/plugins && \
    mkdir -p /opt/corda/logs

# Copy corda files
ADD corda.jar               /opt/corda/corda.jar
ADD node.conf               /opt/corda/node.conf
ADD network-parameters      /opt/corda/
ADD cordapps/               /opt/corda/cordapps
ADD additional-node-infos/  /opt/corda/additional-node-infos
ADD certificates/           /opt/corda/certificates
ADD drivers/                /opt/corda/drivers
ADD persistence*            /opt/corda/

# Working directory for Corda
WORKDIR /opt/corda
ENV HOME=/opt/corda

# ENV RUST_LOG=trace

# Start it
ENTRYPOINT ["/usr/bin/java"]
CMD ["-Xmx1024m", \
     "-jar", \
     "corda.jar"]