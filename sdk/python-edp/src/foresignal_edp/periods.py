"""Observation periods (EDP §3): ``YYYY``, ``YYYY-Qn``, ``YYYY-MM``, ``YYYY-Www``, ``YYYY-MM-DD`` or an
RFC 3339 timestamp with an offset. Periods are UTC intervals ``[start, end)``; an instant is the
degenerate interval ``[t, t]``. One series uses one granularity.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from datetime import date, datetime, timedelta, timezone

from .times import format_time, is_real_date, parse_rfc3339

_YEAR = re.compile(r"^(\d{4})$")
_QUARTER = re.compile(r"^(\d{4})-Q([1-4])$")
_MONTH = re.compile(r"^(\d{4})-(0[1-9]|1[0-2])$")
_WEEK = re.compile(r"^(\d{4})-W(0[1-9]|[1-4]\d|5[0-3])$")
_DAY = re.compile(r"^(\d{4})-(\d{2})-(\d{2})$")
_INSTANT = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?(Z|[+-]\d{2}:\d{2})$")


@dataclass(frozen=True)
class Period:
    """A parsed period: its granularity (``year``, ``quarter``, ``month``, ``week``, ``day`` or
    ``instant``), its inclusive UTC start and its exclusive UTC end (equal to the start for an instant)."""

    granularity: str
    start: datetime
    end: datetime


def _utc(day: date) -> datetime:
    return datetime(day.year, day.month, day.day, tzinfo=timezone.utc)


def _add_months(day: date, months: int) -> date:
    total = day.year * 12 + (day.month - 1) + months
    return date(total // 12, total % 12 + 1, 1)


def iso_weeks_in_year(year: int) -> int:
    """The number of ISO weeks (52 or 53) of ``year``."""
    return date(year, 12, 28).isocalendar()[1]


def parse_period(t: object) -> Period | None:
    """Parses an observation's ``t``; ``None`` when it is not an accepted form or not a real date."""
    if not isinstance(t, str):
        return None
    try:
        if m := _YEAR.match(t):
            start = date(int(m.group(1)), 1, 1)
            return Period("year", _utc(start), _utc(date(start.year + 1, 1, 1)))
        if m := _QUARTER.match(t):
            start = date(int(m.group(1)), (int(m.group(2)) - 1) * 3 + 1, 1)
            return Period("quarter", _utc(start), _utc(_add_months(start, 3)))
        if m := _MONTH.match(t):
            start = date(int(m.group(1)), int(m.group(2)), 1)
            return Period("month", _utc(start), _utc(_add_months(start, 1)))
        if m := _WEEK.match(t):
            year, week = int(m.group(1)), int(m.group(2))
            if week > iso_weeks_in_year(year):
                return None
            start = date.fromisocalendar(year, week, 1)
            return Period("week", _utc(start), _utc(start + timedelta(days=7)))
        if m := _DAY.match(t):
            year, month, day = int(m.group(1)), int(m.group(2)), int(m.group(3))
            if not is_real_date(year, month, day):
                return None
            start = date(year, month, day)
            return Period("day", _utc(start), _utc(start + timedelta(days=1)))
        if _INSTANT.match(t):
            instant = parse_rfc3339(t)
            return None if instant is None else Period("instant", instant, instant)
    except (ValueError, OverflowError):
        return None
    return None


def format_period(value: date | datetime | str) -> str:
    """Formats a ``t`` value: a ``str`` unchanged, a ``datetime`` as an RFC 3339 instant, a ``date`` as
    ``YYYY-MM-DD``."""
    if isinstance(value, str):
        return value
    if isinstance(value, datetime):
        formatted = format_time(value)
        assert formatted is not None
        return formatted
    if isinstance(value, date):
        return value.isoformat()
    raise TypeError(f"Cannot format {type(value).__name__} as a period")


def quarter(year: int, q: int) -> str:
    """``YYYY-Qn``."""
    if not 1 <= q <= 4:
        raise ValueError("A quarter is 1 to 4")
    return f"{year:04d}-Q{q}"


def month(year: int, m: int) -> str:
    """``YYYY-MM``."""
    if not 1 <= m <= 12:
        raise ValueError("A month is 1 to 12")
    return f"{year:04d}-{m:02d}"


def iso_week(year: int, week: int) -> str:
    """``YYYY-Www`` (ISO week)."""
    if not 1 <= week <= iso_weeks_in_year(year):
        raise ValueError(f"{year} has no ISO week {week}")
    return f"{year:04d}-W{week:02d}"
