# Host memory safety

The guardrails that keep the development host from locking up when memory runs out. This is host
configuration, not part of Foresignal: it lives in `/etc`, is applied by hand with sudo, and is
recorded here so the next operator knows what is in place and why.

Recorded 2026-10-25. The live state is what is true; check it with the commands under
[Checking it](#checking-it) before relying on this page.

## Why

The host became unusable twice from memory exhaustion. CPU stayed 60–77% idle; the box was
thrashing swap, not computing. It had 23 GiB of RAM and about 24 GiB of swap in two files, and
earlyoom was running with its defaults, which act only when available RAM **and** free swap are
both below 10%. With that much swap, about 20 GiB had to be paged out before earlyoom would kill
anything, so the host thrashed for a long time first. On 2026-10-25 two uncapped 8 GB Gradle
daemons helped fill the swap and the host had to be power-cycled.

## The layers

| Layer | What it does | Where |
|---|---|---|
| Capped server launcher | Runs the server JVM in the systemd user scope `foresignal-server.scope` with `MemoryMax` (default `10G`, `FORESIGNAL_MEM_MAX`) and `MemorySwapMax=0`. If it outgrows the cap it is OOM-killed rather than swapping. It refuses to start uncapped. | `scripts/run-server.sh` |
| Capped Android build | Runs a build in `foresignal-admin-build.scope` with `MemoryMax` (default `8G`, `FORESIGNAL_BUILD_MEM_MAX`) and `MemorySwapMax=0`, and stops the Gradle and Kotlin daemons before and after. | `foresignal-admin/tool/capped_build.sh` |
| Compose containers | `mem_limit` equals `memswap_limit` for the broker and the database, so neither can swap. | `docs/Architecture.md`, `FORESIGNAL_MEM_LIMIT`, `FORESIGNAL_DB_MEM_LIMIT` |
| GCLocker retries (Java 21 only) | `-XX:GCLockerRetryAllocationCount=100` on the launcher's JVM, so G1 does not declare a false heap OOM while the JDK `Inflater` holds the GCLocker. | `scripts/run-server.sh`; see [The GCLocker flag](#the-gclocker-flag) |
| Small swap | 4 GiB, so earlyoom acts after a little swapping instead of after many gigabytes. | `/swap.img`, `/etc/fstab` |
| Low swappiness | `vm.swappiness = 10`, so the kernel reclaims page cache before swapping anonymous memory. | `/etc/sysctl.d/99-swappiness.conf` |
| earlyoom | Kills the process with the highest OOM score once available RAM is below 10% and free swap is below 50%. Covers anything run uncapped. | `/etc/default/earlyoom`, `earlyoom.service` |

## The GCLocker flag

On 2026-10-25 the capped server, started with `scripts/run-server.sh`, exited 2.5 minutes after
start:

```
[159.281s][warning][gc,alloc] raster-decode-1: Retried waiting for GCLocker too often allocating 12250002 words
Terminating due to java.lang.OutOfMemoryError: Java heap space
```

It was not out of memory: the scope peaked at 3.4 GB of its 10 GB cap with 0 B of swap, and a
minute earlier the heap had 1,090 MB used of 3,018 MB committed. The raster decode had asked for
one 98,000,000-byte array, a `float[]` of the 7000 × 3500 MRMS grid that NetCDF-Java's
`Grib2Record.readData` returns (`src/main/java/ai/foresignal/raster/decode/Grib2Decoder.java:161`).
While any thread is inside a JNI critical region, such as the JDK `Inflater` the raster code
decompresses with (`GzipInputs`, `ElgCodec`, `FieldStore`), G1 on Java 21 cannot collect; it
retries such an allocation `GCLockerRetryAllocationCount` times, 2 by default, then reports a heap
OOM, and `-XX:+ExitOnOutOfMemoryError` ends the server.

The launcher's JVM therefore carries
`-XX:+UnlockDiagnosticVMOptions -XX:GCLockerRetryAllocationCount=100`. With it, the server ran
422 seconds with no GCLocker warning and no OOM, `/v1/system` answering 200, before it was
stopped by hand.

**Compose does not carry the flag.** Its runtime image is JDK 25 (`eclipse-temurin:25-jre`, see
the `Dockerfile`), where G1 pins regions instead of using the GCLocker (JDK 22+), so the failure
cannot happen there, and JDK 25 rejects the flag:

```
Unrecognized VM option 'GCLockerRetryAllocationCount=100'
Error: Could not create the Java Virtual Machine.
```

Adding it to compose's `JAVA_TOOL_OPTIONS` would stop the container from starting. The launcher
runs the only local JDK, 21, which the `Dockerfile` also warns pins virtual threads blocked in
`synchronized`; `docker compose up -d` on JDK 25 is the preferred way to run the broker for any
length of time.

The flag makes the false OOM rare; it does not remove the 98 MB allocation. Decoding into tiles
instead of one array is roadmap item 162 (`docs/Roadmap.md`).

## Host configuration

### Swap

One swap file, `/swap.img`, 4 GiB, mode `600`, owned by root. `/etc/fstab` has exactly one swap
line:

```
/swap.img	none	swap	sw	0	0
```

The second swap file, the 16 GiB `/swapfile`, was deleted and its fstab line removed. Do not
remove swap entirely: a little lets the kernel page out memory that is genuinely idle and keep
more RAM for cache.

To resize it again:

```sh
sudo swapoff /swap.img
sudo rm /swap.img
sudo fallocate -l 4G /swap.img
sudo chmod 600 /swap.img
sudo mkswap /swap.img
sudo swapon /swap.img
```

`swapoff` moves whatever is in swap back into RAM, so first check that the `used` figure from
`swapon --show` fits comfortably in `available` from `free -h`.

### Swappiness

`/etc/sysctl.d/99-swappiness.conf`:

```
# Reclaim page cache before swapping anonymous memory (host memory safety, 2026-10-25).
vm.swappiness = 10
```

No other file in `/etc/sysctl.conf`, `/etc/sysctl.d/` or `/usr/lib/sysctl.d/` sets
`vm.swappiness`, so this one is in effect. Apply a change without rebooting with
`sudo sysctl --system`.

### earlyoom

`/etc/default/earlyoom`:

```
EARLYOOM_ARGS="-r 3600 -m 10 -s 50"
```

`earlyoom.service` reads this through `EnvironmentFile=-/etc/default/earlyoom` and runs
`/usr/bin/earlyoom $EARLYOOM_ARGS` (earlyoom v1.7). `-m 10` is the minimum available RAM, `-s 50`
the minimum free swap; it acts only when both are below their thresholds, so with 4 GiB of swap it
acts once about 2 GiB has been swapped out while RAM is short. `-r 3600` prints a memory report
once an hour. After editing, run `sudo systemctl restart earlyoom.service`.

### Backups

The files as they were before this change:

- `/etc/fstab.bak-2026-10-25`
- `/etc/default/earlyoom.bak-2026-10-25`

## Checking it

```sh
swapon --show
free -h
grep -nE '[[:space:]]swap[[:space:]]' /etc/fstab
cat /proc/sys/vm/swappiness
systemctl is-active earlyoom
ps -o args= -C earlyoom
```

Output on 2026-10-25 after the change, before any reboot:

```
NAME      TYPE SIZE USED PRIO
/swap.img file   4G   0B   -1
               total        used        free      shared  buff/cache   available
Mem:            23Gi        20Gi       256Mi       1.9Gi       4.8Gi       2.8Gi
Swap:          4.0Gi          0B       4.0Gi
12:/swap.img	none	swap	sw	0	0
10
active
/usr/bin/earlyoom -r 3600 -m 10 -s 50
```

The fstab and sysctl files make the change permanent, but it has not been confirmed across a
reboot. After the next one, run the commands above again.
