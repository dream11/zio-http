# ZIO-HTTP
## How to start ZIO-Http server
1. Clone the repository
   `git clone https://github.com/dream11/zio-http.git`
2. Start the server `sbt run example/run`
3. Server starts `http://127.0.0.1:8090/text` 

## Environment
**We have used 2 machines**
1. EC2(C5.4xLarge) 16 vCPUs 32 GB RAM as server
2. EC2(C5.4xLarge) 16 vCPUs 32 GB RAM as client with wrk setup

## Benchmarks

### ZIO-HTTP
#### Plain Text
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.105.8:8090/text
Running 10s test @ http://10.10.109.3:8090
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.37ms  844.59us 206.84ms   97.72%
    Req/Sec    60.42k     2.20k   74.51k    70.22%
  Latency Distribution
     50%    1.28ms
     75%    1.48ms
     90%    1.72ms
     99%    2.55ms
  7267713 requests in 10.10s, 346.55MB read
Requests/sec: 719576.04
Transfer/sec:     34.31MB
```
#### JSON
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.105.8:8090/json
Running 10s test @ http://10.10.109.3:8090/json
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.40ms  421.62us  32.84ms   90.14%
    Req/Sec    58.73k     2.81k   68.19k    68.51%
  Latency Distribution
     50%    1.32ms
     75%    1.53ms
     90%    1.80ms
     99%    2.49ms
  7070158 requests in 10.10s, 660.78MB read
Requests/sec: 700073.31
```
### Vertx
#### Plain Text
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:8080/plaintext
Running 10s test @ http://10.10.109.3:8080/plaintext
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.86ms   20.51ms 455.47ms   96.42%
    Req/Sec    59.89k    17.38k   82.30k    82.17%
  Latency Distribution
     50%    1.12ms
     75%    1.46ms
     90%    4.20ms
     99%  103.73ms
  7150937 requests in 10.10s, 0.87GB read
Requests/sec: 707991.69
Transfer/sec:     87.78MB
```
#### JSON
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:8080/json
Running 10s test @ http://10.10.109.3:8080/json
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.98ms   20.68ms 331.64ms   94.99%
    Req/Sec    55.01k    19.93k   93.01k    78.66%
  Latency Distribution
     50%    1.18ms
     75%    1.69ms
     90%    9.91ms
     99%  114.11ms
  6513121 requests in 10.10s, 0.92GB read
Requests/sec: 644854.27
Transfer/sec:     92.86MB
```
### Http4s
#### Plain Text
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:8080/plaintext
Running 10s test @ http://10.10.109.3:8080/plaintext
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    78.36ms  455.43ms   6.74s    97.14%
    Req/Sec    11.76k     3.34k   47.87k    79.03%
  Latency Distribution
     50%    5.35ms
     75%    9.36ms
     90%   45.89ms
     99%    2.46s
  1406517 requests in 10.08s, 202.55MB read
Requests/sec: 139573.98
Transfer/sec:     20.10MB
```
#### JSON
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:8080/json
Running 10s test @ http://10.10.109.3:8080/json
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    87.89ms  500.30ms   6.71s    97.02%
    Req/Sec    11.43k     3.52k   36.27k    74.58%
  Latency Distribution
     50%    5.37ms
     75%    9.45ms
     90%   47.89ms
     99%    2.78s
  1369098 requests in 10.10s, 203.69MB read
Requests/sec: 135565.22
Transfer/sec:     20.17MB
```
### Play2
#### Plain text
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:9000/plaintext
Running 10s test @ http://10.10.109.3:9000/plaintext
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    54.78ms  320.71ms   6.72s    96.49%
    Req/Sec    22.46k     8.60k   81.67k    84.98%
  Latency Distribution
     50%    3.31ms
     75%    3.84ms
     90%    4.60ms
     99%    1.59s
  2664591 requests in 10.10s, 292.23MB read
Requests/sec: 263819.25
Transfer/sec:     28.93MB
```
#### JSON
```dtd
./wrk -t12 -c1000 --latency --timeout=10s --duration=10s http://10.10.109.3:9000/json
Running 10s test @ http://10.10.109.3:9000/json
  12 threads and 1000 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   124.11ms  568.53ms   6.68s    95.06%
    Req/Sec    22.21k     7.34k   60.60k    84.84%
  Latency Distribution
     50%    3.39ms
     75%    3.99ms
     90%    8.18ms
     99%    3.17s
  2638210 requests in 10.10s, 339.66MB read
Requests/sec: 261223.68
Transfer/sec:     33.63MB
```