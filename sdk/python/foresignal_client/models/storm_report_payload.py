from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_report_payload_magnitude_units import (
    StormReportPayloadMagnitudeUnits,
)
from ..models.storm_report_payload_type import StormReportPayloadType
from ..types import UNSET, Unset

T = TypeVar("T", bound="StormReportPayload")


@_attrs_define
class StormReportPayload:
    """Payload of `storm-report` records (spc-storm-reports; never tombstoned on absence)

    Attributes:
        type_ (StormReportPayloadType):
        location (str):
        title (str):
        magnitude (float | str | Unset): Tornado rating string (EF0 to EF5), wind mph or hail inches as numbers; omitted
            when UNK
        magnitude_units (StormReportPayloadMagnitudeUnits | Unset): EF (tornado), mph (wind) or in (hail)
        county (str | Unset):
        state (str | Unset):
        wfo (str | Unset):
        comments (str | Unset): Untrusted upstream text, ≤ 500 chars
    """

    type_: StormReportPayloadType
    location: str
    title: str
    magnitude: float | str | Unset = UNSET
    magnitude_units: StormReportPayloadMagnitudeUnits | Unset = UNSET
    county: str | Unset = UNSET
    state: str | Unset = UNSET
    wfo: str | Unset = UNSET
    comments: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        location = self.location

        title = self.title

        magnitude: float | str | Unset
        if isinstance(self.magnitude, Unset):
            magnitude = UNSET
        else:
            magnitude = self.magnitude

        magnitude_units: str | Unset = UNSET
        if not isinstance(self.magnitude_units, Unset):
            magnitude_units = self.magnitude_units.value

        county = self.county

        state = self.state

        wfo = self.wfo

        comments = self.comments

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "location": location,
                "title": title,
            }
        )
        if magnitude is not UNSET:
            field_dict["magnitude"] = magnitude
        if magnitude_units is not UNSET:
            field_dict["magnitudeUnits"] = magnitude_units
        if county is not UNSET:
            field_dict["county"] = county
        if state is not UNSET:
            field_dict["state"] = state
        if wfo is not UNSET:
            field_dict["wfo"] = wfo
        if comments is not UNSET:
            field_dict["comments"] = comments

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = StormReportPayloadType(d.pop("type"))

        location = d.pop("location")

        title = d.pop("title")

        def _parse_magnitude(data: object) -> float | str | Unset:
            if isinstance(data, Unset):
                return data
            return cast(float | str | Unset, data)

        magnitude = _parse_magnitude(d.pop("magnitude", UNSET))

        _magnitude_units = d.pop("magnitudeUnits", UNSET)
        magnitude_units: StormReportPayloadMagnitudeUnits | Unset
        if isinstance(_magnitude_units, Unset):
            magnitude_units = UNSET
        else:
            magnitude_units = StormReportPayloadMagnitudeUnits(_magnitude_units)

        county = d.pop("county", UNSET)

        state = d.pop("state", UNSET)

        wfo = d.pop("wfo", UNSET)

        comments = d.pop("comments", UNSET)

        storm_report_payload = cls(
            type_=type_,
            location=location,
            title=title,
            magnitude=magnitude,
            magnitude_units=magnitude_units,
            county=county,
            state=state,
            wfo=wfo,
            comments=comments,
        )

        storm_report_payload.additional_properties = d
        return storm_report_payload

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
