FROM  openjdk:11-jdk-slim

USER root

EXPOSE 8981 8085 80 9999

COPY ./target/pricing-dist-assembly.tar.gz /tmp/

RUN rm -rf /tmp/PRICING && mkdir -p /tmp/PRICING && cd /tmp/PRICING && tar -xvf /tmp/pricing-dist-assembly.tar.gz

RUN whoami && ls -l /tmp/PRICING/pricing-startup.sh && chmod +x /tmp/PRICING/pricing-startup.sh

HEALTHCHECK CMD curl --fail http://localhost:9999 || exit 1

ENTRYPOINT [ "/tmp/PRICING/pricing-startup.sh" ]