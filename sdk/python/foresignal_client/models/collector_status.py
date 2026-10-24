from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="CollectorStatus")


@_attrs_define
class CollectorStatus:
    """A live snapshot of the pull governor (prompt 160).

    Attributes:
        cap (int): The governor's current cap: the maxConcurrentPulls in force.
        in_use (int): How many polling fetches hold a permit and are pulling from upstream now. May briefly exceed cap
            just after the cap is lowered.
        waiting (int): How many polling fetches are queued for a permit.
    """

    cap: int
    in_use: int
    waiting: int

    def to_dict(self) -> dict[str, Any]:
        cap = self.cap

        in_use = self.in_use

        waiting = self.waiting

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "cap": cap,
                "inUse": in_use,
                "waiting": waiting,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        cap = d.pop("cap")

        in_use = d.pop("inUse")

        waiting = d.pop("waiting")

        collector_status = cls(
            cap=cap,
            in_use=in_use,
            waiting=waiting,
        )

        return collector_status
