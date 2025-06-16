FROM python:3.9.22-alpine3.22

WORKDIR /code

COPY src/indexer .

COPY .env .env

RUN pip install -r requierements.txt

CMD [ "python3", "/code/indexer.py" ]