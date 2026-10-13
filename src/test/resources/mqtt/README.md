# MQTT test broker (prompt 060)

`mosquitto.conf` configures the `eclipse-mosquitto:2` container of `MosquittoSupport`: an anonymous listener
(1883), a password listener (1884, user `fs`, password `s3cret`, hashed into `/tmp/passwd` when the container
starts) and a TLS listener (8883).

`certs/` holds a throwaway test CA (`ca.crt`, key discarded) and a server certificate for `localhost` and
`127.0.0.1` signed by it (`server.crt`, `server.key`), valid until 2046. They were generated with OpenSSL
3.0.13 for these tests only; the key protects nothing and must never be used outside the test broker.
