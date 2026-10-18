/** StatusBoard: latest health per scope, delivered once per change. */
import { describe, expect, it } from "vitest";
import { StatusBoard } from "../src/index.ts";

describe("StatusBoard", () => {
  it("reports, drains and lists current health", () => {
    const board = new StatusBoard();
    expect(board.drain()).toEqual([]);
    expect(board.current()).toEqual([]);
    board.report("degraded", "slow");
    board.report("blocked", undefined, { stream: "levels" });
    const drained = board.drain();
    expect(drained).toContainEqual({ type: "status", health: "degraded", message: "slow" });
    expect(drained).toContainEqual({ stream: "levels", type: "status", health: "blocked" });
    expect(board.drain()).toEqual([]);
    expect(board.current()).toHaveLength(2);
  });

  it("does not redeliver unchanged values and the latest wins", () => {
    const board = new StatusBoard();
    board.report("degraded", "slow");
    board.drain();
    board.report("degraded", "slow");
    expect(board.drain()).toEqual([]);
    board.report("degraded", "slower");
    board.ok();
    expect(board.drain()).toEqual([{ type: "status", health: "ok" }]);
  });

  it("requeues drained items unless a newer value arrived", () => {
    const board = new StatusBoard();
    board.report("degraded", undefined, { stream: "a" });
    board.report("blocked", undefined, { stream: "b" });
    const drained = board.drain();
    board.ok({ stream: "b" });
    board.requeue(drained);
    expect(Object.fromEntries(board.drain().map((i) => [i.stream, i.health]))).toEqual({ a: "degraded", b: "ok" });
  });

  it("refuses unknown health values", () => {
    expect(() => new StatusBoard().report("on-fire" as never)).toThrow(RangeError);
  });
});
