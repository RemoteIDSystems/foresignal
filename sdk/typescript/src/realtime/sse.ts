/**
 * An incremental Server-Sent Events parser (WHATWG HTML "event stream interpretation"), fed with decoded text
 * chunks of any size. It never buffers more than one incomplete line.
 */

/** One dispatched event. */
export interface SseMessage {
  /** The `event:` name (`message` when absent). */
  event: string;
  /** The `data:` lines joined with `\n`. */
  data: string;
  /** The `id:` of this event, only when the event carried one. */
  id: string | undefined;
}

/** Parses an event stream. */
export class SseParser {
  private buffer = "";
  private started = false;
  private eventName = "";
  private dataLines: string[] = [];
  private eventId: string | undefined;
  private hasId = false;

  /** The latest `retry:` value in milliseconds, or undefined when the server sent none. */
  retryMs: number | undefined;

  /**
   * Feeds a chunk of text and returns the events it completed.
   *
   * @param chunk decoded text (UTF-8 already decoded)
   */
  feed(chunk: string): SseMessage[] {
    let text = this.buffer + chunk;
    if (!this.started && text.length > 0) {
      this.started = true;
      if (text.charCodeAt(0) === 0xfeff) {
        text = text.slice(1);
      }
    }
    const out: SseMessage[] = [];
    let pos = 0;
    for (;;) {
      let end = -1;
      for (let i = pos; i < text.length; i++) {
        const c = text.charCodeAt(i);
        if (c === 10 || c === 13) {
          end = i;
          break;
        }
      }
      if (end < 0) {
        break;
      }
      if (text.charCodeAt(end) === 13 && end === text.length - 1) {
        // A CR at the end of the chunk may be the first half of CRLF: wait for the next chunk.
        break;
      }
      const line = text.slice(pos, end);
      pos = end + (text.charCodeAt(end) === 13 && text.charCodeAt(end + 1) === 10 ? 2 : 1);
      this.line(line, out);
    }
    this.buffer = text.slice(pos);
    return out;
  }

  /** Processes one complete line. */
  private line(line: string, out: SseMessage[]): void {
    if (line === "") {
      this.dispatch(out);
      return;
    }
    if (line.startsWith(":")) {
      return;
    }
    const colon = line.indexOf(":");
    const field = colon < 0 ? line : line.slice(0, colon);
    let value = colon < 0 ? "" : line.slice(colon + 1);
    if (value.startsWith(" ")) {
      value = value.slice(1);
    }
    switch (field) {
      case "event":
        this.eventName = value;
        break;
      case "data":
        this.dataLines.push(value);
        break;
      case "id":
        if (!value.includes("\u0000")) {
          this.eventId = value;
          this.hasId = true;
        }
        break;
      case "retry":
        if (/^\d+$/.test(value)) {
          this.retryMs = Number(value);
        }
        break;
      default:
        break;
    }
  }

  /** Dispatches the pending event, if it has data. */
  private dispatch(out: SseMessage[]): void {
    if (this.dataLines.length > 0) {
      out.push({
        event: this.eventName || "message",
        data: this.dataLines.join("\n"),
        id: this.hasId && this.eventId !== "" ? this.eventId : undefined,
      });
    }
    this.eventName = "";
    this.dataLines = [];
    this.eventId = undefined;
    this.hasId = false;
  }
}
