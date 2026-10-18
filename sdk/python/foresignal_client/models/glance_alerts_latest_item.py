from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.glance_alerts_latest_item_severity import GlanceAlertsLatestItemSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="GlanceAlertsLatestItem")


@_attrs_define
class GlanceAlertsLatestItem:
    """Latest alert

    Attributes:
        id (int):
        severity (GlanceAlertsLatestItemSeverity):
        fired_at (datetime.datetime):
        short_title (str | Unset):
        near (None | str | Unset): e.g. 12 km NNE of Austin, TX (040)
    """

    id: int
    severity: GlanceAlertsLatestItemSeverity
    fired_at: datetime.datetime
    short_title: str | Unset = UNSET
    near: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        severity = self.severity.value

        fired_at = self.fired_at.isoformat()

        short_title = self.short_title

        near: None | str | Unset
        if isinstance(self.near, Unset):
            near = UNSET
        else:
            near = self.near

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "severity": severity,
                "firedAt": fired_at,
            }
        )
        if short_title is not UNSET:
            field_dict["shortTitle"] = short_title
        if near is not UNSET:
            field_dict["near"] = near

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        severity = GlanceAlertsLatestItemSeverity(d.pop("severity"))

        fired_at = datetime.datetime.fromisoformat(d.pop("firedAt"))

        short_title = d.pop("shortTitle", UNSET)

        def _parse_near(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        near = _parse_near(d.pop("near", UNSET))

        glance_alerts_latest_item = cls(
            id=id,
            severity=severity,
            fired_at=fired_at,
            short_title=short_title,
            near=near,
        )

        glance_alerts_latest_item.additional_properties = d
        return glance_alerts_latest_item

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
