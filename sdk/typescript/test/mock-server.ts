/**
 * An in-process HTTP server for the SDK tests: records every request and lets each test script the answers,
 * including Server-Sent Event streams that stay open, end, or go silent.
 */
import { createServer, type IncomingMessage, type Server, type ServerResponse } from "node:http";
import type { AddressInfo } from "node:net";

/** A request the server received. */
export interface RecordedRequest {
  method: string;
  /** Path without the query. */
  path: string;
  query: URLSearchParams;
  headers: IncomingMessage["headers"];
  body: string;
}

/** Answers one request. */
export type Handler = (req: RecordedRequest, res: ServerResponse) => void | Promise<void>;

/** A running mock server. */
export interface MockServer {
  /** Base URL, e.g. `http://127.0.0.1:43123`. */
  url: string;
  requests: RecordedRequest[];
  /** Requests to one path. */
  requestsTo(path: string, method?: string): RecordedRequest[];
  close(): Promise<void>;
}

/** Starts a server on a free loopback port. */
export async function startServer(handler: Handler): Promise<MockServer> {
  const requests: RecordedRequest[] = [];
  const server: Server = createServer((req, res) => {
    const chunks: Buffer[] = [];
    req.on("data", (c: Buffer) => chunks.push(c));
    req.on("end", () => {
      const url = new URL(req.url ?? "/", "http://localhost");
      const recorded: RecordedRequest = {
        method: req.method ?? "GET",
        path: url.pathname,
        query: url.searchParams,
        headers: req.headers,
        body: Buffer.concat(chunks).toString("utf8"),
      };
      requests.push(recorded);
      Promise.resolve(handler(recorded, res)).catch((error: unknown) => {
        if (!res.headersSent) {
          res.writeHead(500, { "Content-Type": "text/plain" });
        }
        res.end(String(error));
      });
    });
  });
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address() as AddressInfo;
  return {
    url: `http://127.0.0.1:${port}`,
    requests,
    requestsTo: (path, method) => requests.filter((r) => r.path === path && (!method || r.method === method)),
    close: () => new Promise<void>((resolve) => {
      server.closeAllConnections();
      server.close(() => resolve());
    }),
  };
}

/** One SSE frame to write. */
export interface Frame {
  event?: string;
  data?: unknown;
  id?: string;
  retry?: number;
  comment?: string;
}

/** Starts an SSE response. */
export function openSse(res: ServerResponse): void {
  res.writeHead(200, { "Content-Type": "text/event-stream", "Cache-Control": "no-cache" });
  res.flushHeaders();
}

/** Writes frames to an open SSE response. */
export function writeFrames(res: ServerResponse, frames: Frame[]): void {
  for (const f of frames) {
    let text = "";
    if (f.comment !== undefined) {
      text += `:${f.comment}\n`;
    }
    if (f.retry !== undefined) {
      text += `retry: ${f.retry}\n`;
    }
    if (f.id !== undefined) {
      text += `id: ${f.id}\n`;
    }
    if (f.event !== undefined) {
      text += `event: ${f.event}\n`;
    }
    if (f.data !== undefined) {
      text += `data: ${typeof f.data === "string" ? f.data : JSON.stringify(f.data)}\n`;
    }
    res.write(text + "\n");
  }
}

/** Answers with an SSE stream of `frames`, then ends it (or keeps it open with `keepOpen`). */
export function sse(res: ServerResponse, frames: Frame[], keepOpen = false): void {
  openSse(res);
  writeFrames(res, frames);
  if (!keepOpen) {
    res.end();
  }
}

/** Answers with JSON. */
export function json(res: ServerResponse, status: number, body: unknown, headers: Record<string, string> = {}): void {
  const contentType = status >= 400 ? "application/problem+json" : "application/json";
  res.writeHead(status, { "Content-Type": contentType, ...headers });
  res.end(JSON.stringify(body));
}

/** Collects stream signals until `until` returns true (or `limit` signals), then closes the stream. */
export async function collect<T extends { type: string }>(
  stream: AsyncIterable<T> & { close(): void },
  until: (signal: T, all: T[]) => boolean,
  limit = 200,
): Promise<T[]> {
  const out: T[] = [];
  for await (const signal of stream) {
    out.push(signal);
    if (until(signal, out) || out.length >= limit) {
      stream.close();
      break;
    }
  }
  return out;
}

/** An instant sleep that records the requested delays. */
export function recordingSleep(): { delays: number[]; sleep: (ms: number, signal: AbortSignal) => Promise<void> } {
  const delays: number[] = [];
  return { delays, sleep: async (ms: number) => { delays.push(ms); } };
}
