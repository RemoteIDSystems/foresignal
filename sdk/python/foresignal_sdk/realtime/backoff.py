"""Jittered exponential reconnect backoff: 1 s doubling to 60 s, each delay multiplied by a random factor in
[0.8, 1.2) and clamped to [initial, max], never below a server-requested floor (``retry:`` or ``Retry-After``)."""
from __future__ import annotations

import random as _random
from collections.abc import Callable


class Backoff:
    """Computes successive reconnect delays in seconds."""

    def __init__(self, initial: float = 1.0, maximum: float = 60.0,
                 random: Callable[[], float] | None = None) -> None:
        """Creates a backoff with a first delay, a largest delay and a random source in [0, 1)."""
        self.initial = initial
        self.maximum = maximum
        self._random = random or _random.random
        self.attempt = 0
        """Delays handed out since the last reset."""

    def next(self, minimum: float = 0.0) -> float:
        """The next delay in seconds, never below ``minimum`` (which may exceed the largest delay)."""
        base = min(self.maximum, self.initial * (2 ** min(self.attempt, 30)))
        self.attempt += 1
        jittered = min(self.maximum, max(self.initial, base * (0.8 + 0.4 * self._random())))
        return max(round(jittered, 3), minimum)

    def reset(self) -> None:
        """Starts over at the first delay (after a connection that worked)."""
        self.attempt = 0
