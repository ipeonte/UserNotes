# User Notes API

Used Framework: SpringBoot
Database: MongoDB
Reason for Framework: SpringBoot contains all required libraries for REST, Security and DB including one that can control rate limiting ([Resilience4J RateLimiter](https://resilience4j.readme.io/docs/ratelimiter)). SpringBoot MongoDB JPA library supports text indexing via model annotation and has a good wrapper around all MongoDB search functionality.
Reason for Database: performance and build in text indexed search.
Default request throttling: 1 request per 2 seconds for /api/notes and /api/search API. 1 request per 1 second for /login
Request throttling parameters can be changed via command line start parameters

## Test
mvn clean test

## Test & Build
mvn clean package

## Run with connection to local MongodB

```
java -jar target/notes-1.0.0.jar
```

## Run with connection to remote MongodB
```
java -jar target/notes-1.0.0.jar --spring.data.mongodb.uri=mongodb://<server_address>:<server_port>/<database_name>
```

## Swagger
After service successfully started the Swagger UI is available at http://localhost:8080/swagger-ui.html

## curl

Below is the list of curl command and expected output

### Sign up new user

```
curl -v --location 'http://localhost:8080/api/auth/signup' \
--header 'Content-Type: application/json' \
--data '{
    "name":"user",
    "password":"test"
}'
```
Expected output

```
*   Trying 127.0.0.1:8080...\* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /api/auth/signup HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 44
> 
< HTTP/1.1 401 
< Set-Cookie: JSESSIONID=0141BF3D92BFE45D3B7ABD53CD5436A6; Path=/; HttpOnly
< Content-Length: 0
< Date: Mon, 10 Mar 2025 06:23:57 GMT
< 
* Connection #0 to host localhost left intact
```

### Login
Save cookie in external file sso.txt to use later

```
curl -v -c sso.txt --location 'http://localhost:8080/api/auth/login' \
--form 'name="user"' \
--form 'password="test"'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /api/auth/login HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Content-Length: 244
> Content-Type: multipart/form-data; boundary=------------------------0630c0487e378a5b
> 
* We are completely uploaded and fine
< HTTP/1.1 200 
* Added cookie JSESSIONID="9E69AE339FDE59B20254FC044F03F24D" for domain localhost, path /, expire 0
< Set-Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D; Path=/; HttpOnly
< Content-Length: 0
< Date: Mon, 10 Mar 2025 07:54:05 GMT
< 
* Connection #0 to host localhost left intact
```

### Post New Notes

```
curl  -v -b sso.txt --location 'http://localhost:8080/api/notes' \
--header 'Content-Type: application/json' \
--data '{
    "note": "test123"
}'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /api/notes HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> Content-Type: application/json
> Content-Length: 25
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Mon, 10 Mar 2025 22:03:03 GMT
< 
* Connection #0 to host localhost left intact
{"id":"67cf608bd8ad9325895a94ed","note":"test123"}
```

### Get All Notes

```
curl -v -b sso.txt --location 'http://localhost:8080/api/notes'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/notes HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Mon, 10 Mar 2025 22:01:19 GMT
< 
* Connection #0 to host localhost left intact
[{"id":"67cf608bd8ad9325895a94ed","note":"test123"}]
```

### Find note by id

```
curl -v -b sso.txt --location 'http://localhost:8080/api/notes/67cf608bd8ad9325895a94ed'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/notes/67cfdac63accd9330a6fb36e HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 11 Mar 2025 06:42:34 GMT
< 
* Connection #0 to host localhost left intact
{"id":"67cf608bd8ad9325895a94ed","note":"test123"}
```

### Update note by id

```
curl -v -b sso.txt --location --request PUT 'http://localhost:8080/api/notes' \
--header 'Content-Type: application/json' \
--data '{
    "id":"67cf608bd8ad9325895a94ed",
    "note": "test456"
}'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> PUT /api/notes HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> Content-Type: application/json
> Content-Length: 62
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 11 Mar 2025 06:48:14 GMT
< 
* Connection #0 to host localhost left intact
{"id":"67cf608bd8ad9325895a94ed","note":"test456"}
```

### Share note with another user

```
curl -v -b sso.txt --location --request POST 'http://localhost:8080/api/notes/67cf608bd8ad9325895a94ed/share/67cfde363accd9330a6fb36f'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /api/notes/67cfdac63accd9330a6fb36e/share/67cfde363accd9330a6fb36f HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> 
< HTTP/1.1 200 
< Content-Length: 0
< Date: Tue, 11 Mar 2025 06:59:00 GMT
< 
* Connection #0 to host localhost left intact
```

### Search by query

```
curl -v -b sso.txt 'http://localhost:8080/api/search?query=t4'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /api/search?query=t4 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 11 Mar 2025 06:53:44 GMT
< 
* Connection #0 to host localhost left intact
[{"id":"67cfdac63accd9330a6fb36e","note":"test456"}]
```

### Delete note by id

```
curl -v -b sso.txt --location --request DELETE 'http://localhost:8080/api/notes/67cf608bd8ad9325895a94ed'
```
Expected output

```
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> DELETE /api/notes/67cfe06e3accd9330a6fb370 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.88.1
> Accept: */*
> Cookie: JSESSIONID=9E69AE339FDE59B20254FC044F03F24D
> 
< HTTP/1.1 200 
< Content-Length: 0
< Date: Tue, 11 Mar 2025 07:04:38 GMT
< 
* Connection #0 to host localhost left intact

```
