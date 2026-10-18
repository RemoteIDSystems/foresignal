"""StatusBoard: latest health per scope, delivered once per change."""

from __future__ import annotations

import pytest

from foresignal_edp import StatusBoard


def test_report_drain_and_current() -> None:
    board = StatusBoard()
    assert board.drain() == [] and board.current() == []
    board.report("degraded", "slow")
    board.report("blocked", stream="levels")
    drained = board.drain()
    assert {"type": "status", "health": "degraded", "message": "slow"} in drained
    assert {"stream": "levels", "type": "status", "health": "blocked"} in drained
    assert board.drain() == []
    assert len(board.current()) == 2


def test_unchanged_values_are_not_redelivered_and_latest_wins() -> None:
    board = StatusBoard()
    board.report("degraded", "slow")
    board.drain()
    board.report("degraded", "slow")
    assert board.drain() == []
    board.report("degraded", "slower")
    board.ok()
    assert board.drain() == [{"type": "status", "health": "ok"}]


def test_requeue_keeps_newer_values() -> None:
    board = StatusBoard()
    board.report("degraded", stream="a")
    board.report("blocked", stream="b")
    drained = board.drain()
    board.report("ok", stream="b")
    board.requeue(drained)
    again = {i["stream"]: i["health"] for i in board.drain()}
    assert again == {"a": "degraded", "b": "ok"}


def test_unknown_health_is_refused() -> None:
    with pytest.raises(ValueError):
        StatusBoard().report("on-fire")
