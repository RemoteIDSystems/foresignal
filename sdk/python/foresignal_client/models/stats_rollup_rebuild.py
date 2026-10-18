from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stats_rollup_rebuild_status import StatsRollupRebuildStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="StatsRollupRebuild")


@_attrs_define
class StatsRollupRebuild:
    """Accepted rollup rebuild (038)

    Attributes:
        from_ (datetime.datetime):
        to (datetime.datetime):
        status (StatsRollupRebuildStatus): Always queued: the rebuild runs in the background
        hours (int | Unset):
        days (int | Unset):
    """

    from_: datetime.datetime
    to: datetime.datetime
    status: StatsRollupRebuildStatus
    hours: int | Unset = UNSET
    days: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        status = self.status.value

        hours = self.hours

        days = self.days

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "status": status,
            }
        )
        if hours is not UNSET:
            field_dict["hours"] = hours
        if days is not UNSET:
            field_dict["days"] = days

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        status = StatsRollupRebuildStatus(d.pop("status"))

        hours = d.pop("hours", UNSET)

        days = d.pop("days", UNSET)

        stats_rollup_rebuild = cls(
            from_=from_,
            to=to,
            status=status,
            hours=hours,
            days=days,
        )

        stats_rollup_rebuild.additional_properties = d
        return stats_rollup_rebuild

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
