FROM golang:tip-alpine3.22

WORKDIR /code

COPY src/crawler /code/beyla/crawler

COPY .env .env

RUN cd /code/beyla/crawler && go build -o /code/crawler .

RUN rm -rf /code/beyla/crawler

CMD [ "/code/crawler" ]
