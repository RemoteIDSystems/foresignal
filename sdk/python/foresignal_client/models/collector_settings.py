from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

T = TypeVar("T", bound="CollectorSettings")


@_attrs_define
class CollectorSettings:
    """The collector's runtime settings (prompt 150).

    Attributes:
        max_concurrent_pulls (int): How many polling fetches may pull from upstream at once across every source: the
            size of the global pull governor. A stored or submitted value outside 1 to 16 is clamped. Streams are not
            counted. Default: 2.
    """

    max_concurrent_pulls: int = 2

    def to_dict(self) -> dict[str, Any]:
        max_concurrent_pulls = self.max_concurrent_pulls

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "maxConcurrentPulls": max_concurrent_pulls,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        max_concurrent_pulls = d.pop("maxConcurrentPulls")

        collector_settings = cls(
            max_concurrent_pulls=max_concurrent_pulls,
        )

        return collector_settings
