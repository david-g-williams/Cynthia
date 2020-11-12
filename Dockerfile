FROM azul/zulu-openjdk:latest

WORKDIR /

ADD lambda.gpg .
RUN apt-get update
RUN apt-get install --yes gnupg
RUN apt-key add lambda.gpg
RUN echo "deb http://archive.lambdalabs.com/ubuntu bionic main" > /etc/apt/sources.list.d/lambda.list
RUN echo "Package: *" > /etc/apt/preferences.d/lambda
RUN echo "Pin: origin archive.lambdalabs.com" >> /etc/apt/preferences.d/lambda
RUN echo "Pin-Priority: 1001" >> /etc/apt/preferences.d/lambda
RUN echo "cudnn cudnn/license_preseed select ACCEPT" | debconf-set-selections
RUN apt-get update
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get install --yes --no-install-recommends \
        --option "Acquire::http::No-Cache=true" \
        --option "Acquire::http::Pipeline-Depth=0" \
		lambda-stack-cuda \
		lambda-server

RUN mkdir -p /logs /models
RUN chmod -R a+rw /logs /models

ADD models /models
ADD target/cynthia-1.0.jar /cynthia-1.0.jar

EXPOSE 32768

CMD java -jar -Xms1G -Xmx256G /cynthia-1.0.jar
