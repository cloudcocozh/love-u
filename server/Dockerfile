FROM python:3.13-alpine
WORKDIR /app
COPY service.py /app/service.py
RUN addgroup -g 10001 relay && adduser -D -u 10001 -G relay relay && mkdir /data && chown relay:relay /data
USER relay
EXPOSE 8878
VOLUME ["/data"]
HEALTHCHECK --interval=30s --timeout=5s CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8878/health',timeout=3)" || exit 1
CMD ["python", "service.py", "--host", "0.0.0.0", "--port", "8878", "--db", "/data/relay.sqlite"]
