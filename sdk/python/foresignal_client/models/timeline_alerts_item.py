from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TimelineAlertsItem")


@_attrs_define
class TimelineAlertsItem:
    """Alert counts per bucket

    Attributes:
        t (datetime.datetime):
        info (int | Unset):
        warning (int | Unset):
        critical (int | Unset):
    """

    t: datetime.datetime
    info: int | Unset = UNSET
    warning: int | Unset = UNSET
    critical: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        info = self.info

        warning = self.warning

        critical = self.critical

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
            }
        )
        if info is not UNSET:
            field_dict["info"] = info
        if warning is not UNSET:
            field_dict["warning"] = warning
        if critical is not UNSET:
            field_dict["critical"] = critical

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        info = d.pop("info", UNSET)

        warning = d.pop("warning", UNSET)

        critical = d.pop("critical", UNSET)

        timeline_alerts_item = cls(
            t=t,
            info=info,
            warning=warning,
            critical=critical,
        )

        timeline_alerts_item.additional_properties = d
        return timeline_alerts_item

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
