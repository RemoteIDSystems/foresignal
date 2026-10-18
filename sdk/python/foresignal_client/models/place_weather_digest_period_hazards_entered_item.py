from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherDigestPeriodHazardsEnteredItem")


@_attrs_define
class PlaceWeatherDigestPeriodHazardsEnteredItem:
    """Hazard that entered the radius

    Attributes:
        kind (str):
        title (str):
        at (datetime.datetime):
        ref (str | Unset):
    """

    kind: str
    title: str
    at: datetime.datetime
    ref: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind

        title = self.title

        at = self.at.isoformat()

        ref = self.ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "title": title,
                "at": at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = d.pop("kind")

        title = d.pop("title")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        ref = d.pop("ref", UNSET)

        place_weather_digest_period_hazards_entered_item = cls(
            kind=kind,
            title=title,
            at=at,
            ref=ref,
        )

        place_weather_digest_period_hazards_entered_item.additional_properties = d
        return place_weather_digest_period_hazards_entered_item

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
