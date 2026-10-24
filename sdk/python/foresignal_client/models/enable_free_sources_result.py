from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

T = TypeVar("T", bound="EnableFreeSourcesResult")


@_attrs_define
class EnableFreeSourcesResult:
    """What `POST /v1/sources/enable-free` did (prompt 145).

    Attributes:
        enabled (list[str]): The source ids enabled by this call, sorted; empty when there was nothing to enable
        count (int): How many sources this call enabled (the length of `enabled`)
        skipped (int): Disabled built-in sources left disabled: they need a credential or a required parameter, are held
            off by default (`wiki-edit-surge`), or their stored parameters are not valid
    """

    enabled: list[str]
    count: int
    skipped: int

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        count = self.count

        skipped = self.skipped

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "enabled": enabled,
                "count": count,
                "skipped": skipped,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = cast(list[str], d.pop("enabled"))

        count = d.pop("count")

        skipped = d.pop("skipped")

        enable_free_sources_result = cls(
            enabled=enabled,
            count=count,
            skipped=skipped,
        )

        return enable_free_sources_result
