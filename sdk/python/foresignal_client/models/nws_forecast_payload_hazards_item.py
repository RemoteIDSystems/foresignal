from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NwsForecastPayloadHazardsItem")


@_attrs_define
class NwsForecastPayloadHazardsItem:
    """NWS hazard

    Attributes:
        type_ (str): phenomenon.significance, for example FZ.W
        valid_time (str | Unset): ISO-8601 interval
    """

    type_: str
    valid_time: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        valid_time = self.valid_time

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = d.pop("type")

        valid_time = d.pop("validTime", UNSET)

        nws_forecast_payload_hazards_item = cls(
            type_=type_,
            valid_time=valid_time,
        )

        nws_forecast_payload_hazards_item.additional_properties = d
        return nws_forecast_payload_hazards_item

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
