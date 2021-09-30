# Spring Boot + HTTP/2 (h2c)

Just run the server. There is also a test that shows how it works.

If you have the command line tool `curl` with HTTP2 support enabled (check with `curl -V`), then ask the server:
```shell
curl -v --http2 http://localhost:8080
```
