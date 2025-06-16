FROM golang:tip-alpine3.22

WORKDIR /code

COPY src/outlink /code/beyla/outlink

COPY .env .env

RUN cd /code/beyla/outlink && go build -o /code/outlink .

RUN rm -rf /code/beyla/outlink

CMD [ "./outlink" ]
