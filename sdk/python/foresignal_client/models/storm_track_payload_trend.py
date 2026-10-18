from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_track_payload_trend_max_dbz import StormTrackPayloadTrendMaxDbz
from ..types import UNSET, Unset

T = TypeVar("T", bound="StormTrackPayloadTrend")


@_attrs_define
class StormTrackPayloadTrend:
    """Trend

    Attributes:
        max_dbz (StormTrackPayloadTrendMaxDbz | Unset):
    """

    max_dbz: StormTrackPayloadTrendMaxDbz | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        max_dbz: str | Unset = UNSET
        if not isinstance(self.max_dbz, Unset):
            max_dbz = self.max_dbz.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if max_dbz is not UNSET:
            field_dict["maxDbz"] = max_dbz

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _max_dbz = d.pop("maxDbz", UNSET)
        max_dbz: StormTrackPayloadTrendMaxDbz | Unset
        if isinstance(_max_dbz, Unset):
            max_dbz = UNSET
        else:
            max_dbz = StormTrackPayloadTrendMaxDbz(_max_dbz)

        storm_track_payload_trend = cls(
            max_dbz=max_dbz,
        )

        storm_track_payload_trend.additional_properties = d
        return storm_track_payload_trend

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
