from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CustomSourceFormatsOwntracks")


@_attrs_define
class CustomSourceFormatsOwntracks:
    """OwnTracks options (039)

    Attributes:
        max_accuracy_meters (float | Unset): Fixes whose accuracy is worse than this many meters are rejected with
            reason LOW_ACCURACY (039) Default: 200.0.
    """

    max_accuracy_meters: float | Unset = 200.0
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        max_accuracy_meters = self.max_accuracy_meters

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if max_accuracy_meters is not UNSET:
            field_dict["maxAccuracyMeters"] = max_accuracy_meters

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        max_accuracy_meters = d.pop("maxAccuracyMeters", UNSET)

        custom_source_formats_owntracks = cls(
            max_accuracy_meters=max_accuracy_meters,
        )

        custom_source_formats_owntracks.additional_properties = d
        return custom_source_formats_owntracks

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
