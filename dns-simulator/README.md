# DNS Resolution, Caching and Query Analysis System

An interactive, browser-based simulation of DNS for a Computer Networks project.
**Single self-contained file**: open `index.html` in any modern browser. No server, build step or internet connection is needed.

> All DNS records, server names and network delays are **simulated** in JavaScript.
> They are not measurements of real internet DNS servers.

## Features
| Requirement | Where it is in the app |
|---|---|
| DNS hierarchy (Client, Resolver, Cache, Root, `.com`/`.org`/`.in` TLDs, Authoritative) | Animated SVG diagram. Click any server to see its simulated zone data |
| Recursive vs iterative queries | "Query Type" toggle. Recursive: the query is passed down Root → TLD → Auth and the answer comes back up the chain. Iterative: the resolver contacts each level and follows referrals |
| Caching (A records, TLD NS referrals, negative/NXDOMAIN caching) | "Resolver Cache" table: domain, type, IP, TTL, live remaining TTL, timestamp, hits, VALID/EXPIRED status |
| TTL expiry | Configurable TTLs, live countdown, per-entry *Expire* button, *Fast-forward +30 s* clock button, *Clear Cache* |
| Cache hit / miss | Hits skip the hierarchy entirely (~24 ms). Misses run a full lookup (~345 ms with the default delays) and store the result |
| Response time with vs without cache | Shown for every query in "Query Result". *Compare with / without cache* button. Per-query chart. *Caching Benchmark* runs the same random sequence with caching off and on |
| Query recording & analysis | Query Log (exportable as CSV), stats tiles (hit ratio, average hit vs miss time, speed-up, time saved) |
| Unknown domains | NXDOMAIN at the Root (`mysite.xyz`), the TLD (`unknown-site.com`) or the Authoritative server (`ftp.example.com`) |

## Simulated delay model (editable in the UI)
- Client ↔ Resolver RTT 20 ms, cache lookup 2 ms
- Root RTT 100 ms, TLD RTT 80 ms, Authoritative RTT 120 ms
- +5 ms processing each time a server handles a query (in recursive mode, Root/TLD also relay the answer back)
- Optional ±15 % jitter

## Suggested demo script
1. Resolve `example.com` (recursive): **CACHE MISS**. Watch the full chain, the record gets stored.
2. Resolve `example.com` again: **CACHE HIT** in ~24 ms vs ~345 ms.
3. Switch to **Iterative** and resolve `google.com`: you see the referrals from Root and TLD. Then resolve `openai.com`: the cached `.com` NS lets the resolver skip the Root.
4. Press **Expire** on a cache row (or Fast-forward), then query it again: it's a miss and the cache is refreshed.
5. Resolve `unknown-site.com` to see NXDOMAIN and negative caching.
6. Click **Run benchmark** and **Export CSV** for the analysis section of the report.
