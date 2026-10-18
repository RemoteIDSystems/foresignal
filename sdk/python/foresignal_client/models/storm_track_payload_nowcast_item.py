from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_track_payload_nowcast_item_lead_minutes import (
    StormTrackPayloadNowcastItemLeadMinutes,
)

T = TypeVar("T", bound="StormTrackPayloadNowcastItem")


@_attrs_define
class StormTrackPayloadNowcastItem:
    """Nowcast polygon (geometry carried in the record GeometryCollection)

    Attributes:
        lead_minutes (StormTrackPayloadNowcastItemLeadMinutes):
        method (Literal['linear-advection']):
    """

    lead_minutes: StormTrackPayloadNowcastItemLeadMinutes
    method: Literal["linear-advection"]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lead_minutes = self.lead_minutes.value

        method = self.method

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "leadMinutes": lead_minutes,
                "method": method,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lead_minutes = StormTrackPayloadNowcastItemLeadMinutes(d.pop("leadMinutes"))

        method = cast(Literal["linear-advection"], d.pop("method"))
        if method != "linear-advection":
            raise ValueError(
                f"method must match const 'linear-advection', got '{method}'"
            )

        storm_track_payload_nowcast_item = cls(
            lead_minutes=lead_minutes,
            method=method,
        )

        storm_track_payload_nowcast_item.additional_properties = d
        return storm_track_payload_nowcast_item

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
