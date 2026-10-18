"""RFC 3339 times, as the protocol writes them (``2026-10-11T18:00:00Z``)."""

from __future__ import annotations

import re
from datetime import date, datetime, timedelta, timezone

_DATE_TIME = re.compile(
    r"^(\d{4})-(\d{2})-(\d{2})[Tt](\d{2}):(\d{2})(?::(\d{2})(\.\d{1,9})?)?([Zz]|([+-])(\d{2}):(\d{2}))$"
)


def is_leap_year(year: int) -> bool:
    """True for a proleptic Gregorian leap year."""
    return year % 4 == 0 and (year % 100 != 0 or year % 400 == 0)


def days_in_month(year: int, month: int) -> int:
    """The number of days of ``month`` (1-12) in ``year``."""
    if month == 2:
        return 29 if is_leap_year(year) else 28
    return 30 if month in (4, 6, 9, 11) else 31


def is_real_date(year: int, month: int, day: int) -> bool:
    """True when the year, month and day name a real calendar date."""
    return 1 <= month <= 12 and 1 <= day <= days_in_month(year, month)


def parse_rfc3339(text: object) -> datetime | None:
    """Parses an RFC 3339 date-time with an offset (seconds optional, as the server accepts).

    Returns an aware ``datetime`` in UTC, or ``None`` when ``text`` is not a valid date-time.
    """
    if not isinstance(text, str):
        return None
    match = _DATE_TIME.match(text)
    if not match:
        return None
    year, month, day, hour, minute = (int(match.group(i)) for i in range(1, 6))
    second = int(match.group(6) or 0)
    fraction = match.group(7)
    if year < 1 or not is_real_date(year, month, day) or hour > 23 or minute > 59 or second > 59:
        return None
    offset = timedelta(0)
    if match.group(9):
        offset_hours, offset_minutes = int(match.group(10)), int(match.group(11))
        if offset_hours > 18 or offset_minutes > 59 or (offset_hours == 18 and offset_minutes > 0):
            return None
        offset = timedelta(hours=offset_hours, minutes=offset_minutes)
        if match.group(9) == "-":
            offset = -offset
    micros = int((fraction[1:] + "000000")[:6]) if fraction else 0
    try:
        local = datetime(year, month, day, hour, minute, second, micros, tzinfo=timezone(offset))
        return local.astimezone(timezone.utc)
    except (ValueError, OverflowError):
        return None


def format_time(value: datetime | date | str | None) -> str | None:
    """Formats a time for the wire.

    A ``str`` is returned unchanged (the caller vouches for it), ``None`` stays ``None``, an aware
    ``datetime`` becomes UTC with a ``Z`` (milliseconds when it has a sub-second part), and a
    ``date`` becomes midnight UTC. A naive ``datetime`` is refused, because its offset is unknown.
    """
    if value is None or isinstance(value, str):
        return value
    if isinstance(value, datetime):
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("A naive datetime has no offset; pass an aware datetime (e.g. tzinfo=timezone.utc)")
        utc = value.astimezone(timezone.utc)
        spec = "milliseconds" if utc.microsecond else "seconds"
        return utc.replace(tzinfo=None).isoformat(timespec=spec) + "Z"
    if isinstance(value, date):
        return value.isoformat() + "T00:00:00Z"
    raise TypeError(f"Cannot format {type(value).__name__} as an RFC 3339 time")


def utc_now() -> datetime:
    """The current time, aware, in UTC."""
    return datetime.now(timezone.utc)
