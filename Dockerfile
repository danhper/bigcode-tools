FROM ubuntu:16.04

RUN apt-get update -qq
RUN apt-get install -y openjdk-8-jdk git build-essential curl wget apt-transport-https \
                       libncursesw5-dev libreadline-dev libssl-dev libgdbm-dev \
                       libc-dev libsqlite3-dev tk-dev libbz2-dev graphviz bc

RUN echo "deb https://dl.bintray.com/sbt/debian /" >> /etc/apt/sources.list.d/sbt.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
RUN apt-get update -qq
RUN apt-get install -y sbt

ENV ASDF_DIR /root/.asdf
RUN git clone https://github.com/asdf-vm/asdf.git $ASDF_DIR --branch v0.4.0
ENV PATH $ASDF_DIR/bin:$ASDF_DIR/shims:$PATH

RUN mkdir /bigcode-tools
WORKDIR /bigcode-tools

RUN asdf plugin-add nodejs
RUN asdf plugin-add python

RUN bash $ASDF_DIR/plugins/nodejs/bin/import-release-team-keyring

RUN asdf install python 3.6.6
RUN asdf install python 2.7.14
RUN asdf install nodejs 8.11.4
# XXX: postinstall hook creates permission issue
RUN rm /root/.asdf/installs/nodejs/8.11.4/.npm/lib/node_modules/.hooks/postinstall

COPY .tool-versions .tool-versions

RUN pip install tensorflow

# matplotlib will not work in Docker with TkAgg backend
RUN mkdir -p /root/.config/matplotlib
RUN echo "backend: Agg" > /root/.config/matplotlib/matplotlibrc

COPY . /bigcode-tools

RUN cd bigcode-fetcher && pip install .
RUN cd bigcode-astgen/python && pip install .
RUN cd bigcode-astgen/python && pip2 install .
RUN cd bigcode-embeddings && pip install .
RUN asdf reshim python
RUN cd bigcode-astgen/javascript && npm install -g .
RUN asdf reshim nodejs
RUN cd bigcode-astgen/java && ./gradlew build
RUN ln -s /bigcode-tools/bigcode-astgen/java/bin/bigcode-astgen-java /usr/local/bin/bigcode-astgen-java
RUN cd bigcode-ast-tools && sbt assembly
RUN ln -s /bigcode-tools/bigcode-ast-tools/bin/bigcode-ast-tools /usr/local/bin/bigcode-ast-tools
