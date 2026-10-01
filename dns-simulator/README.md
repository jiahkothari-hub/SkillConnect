# DNS Resolution, Caching and Query Analysis System

An interactive, browser-based simulation of DNS for a Computer Networks project.
**Single self-contained file**: open `index.html` in any modern browser. No server, build step or internet connection is needed.

> The resolver, cache, hierarchy walk and all **network delays are simulated** in JavaScript.
> They are not measurements of real internet DNS servers.

## Works for any domain
Two data sources, selectable in the UI:
- **Live DNS data (default):** for any domain you type (e.g. `nmims.in`, `bershka.in`, `amazon.co.uk`), the app fetches the
  real TLD name server, the domain's authoritative name server and its A record over DNS-over-HTTPS (Google Public DNS,
  with Cloudflare as a fallback). These real values are then used in the simulated hierarchy walk. CNAME chains, multiple A
  records and the domain's real TTL are shown too. Needs an internet connection.
- **Offline simulated:** built-in example records, plus deterministic generated records (RFC 5737 documentation IPs)
  for any other domain under a known TLD. If a live lookup fails, the app automatically uses this for that query.

Domains that really don't exist return NXDOMAIN at the correct level: Root (unknown TLD such as `mysite.fake`),
TLD (unregistered domain) or Authoritative (unknown host in an existing zone).

## Features
| Requirement | Where it is in the app |
|---|---|
| DNS hierarchy (Client, Resolver, Cache, Root, `.com`/`.org`/`.in` TLDs plus a slot for any other TLD, Authoritative) | Animated SVG diagram. Click any server to see its simulated zone data |
| Recursive vs iterative queries | "Query Type" toggle. Recursive: the query is passed down Root → TLD → Auth and the answer comes back up the chain. Iterative: the resolver contacts each level and follows referrals |
| Caching (A records, TLD NS referrals, negative/NXDOMAIN caching) | "Resolver Cache" table: domain, type, IP, TTL, live remaining TTL, timestamp, hits, VALID/EXPIRED status |
| TTL expiry | Configurable TTLs, live countdown, per-entry *Expire* button, *Fast-forward +30 s* clock button, *Clear Cache* |
| Cache hit / miss | Hits skip the hierarchy entirely (~24 ms). Misses run a full lookup (~345 ms with the default delays) and store the result |
| Response time with vs without cache | Shown for every query in "Query Result". *Compare with / without cache* button. Per-query chart. *Caching Benchmark* runs the same random sequence with caching off and on |
| Query recording & analysis | Query Log (exportable as CSV), stats tiles (hit ratio, average hit vs miss time, speed-up, time saved) |
| Unknown domains | NXDOMAIN at the Root (`mysite.fake`), the TLD (`no-such-site-dns-demo.com`) or the Authoritative server (`ftp.example.com`) |

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
5. Resolve `no-such-site-dns-demo.com` to see NXDOMAIN and negative caching. Type any real domain (e.g. `nmims.in`) to resolve it with live data.
6. Click **Run benchmark** and **Export CSV** for the analysis section of the report.
