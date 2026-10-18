from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..models.wx_area_payload_field import WxAreaPayloadField
from ..types import UNSET, Unset

T = TypeVar("T", bound="WxAreaPayload")


@_attrs_define
class WxAreaPayload:
    """Payload of `wx-area` records (externalId `{field}:{threshold}:{aoi}`); geometry null when empty

    Attributes:
        field (WxAreaPayloadField): rotation, qpe1h, ffg added by 053
        threshold (float):
        aoi (str): Place id or `aoi`
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset):
        area_km_2 (float | Unset): 2 significant figures
        max_value (float | Unset):
        coverage_pct (int | Unset):
        automated (bool | Unset):
        not_a_warning (bool | Unset):
    """

    field: WxAreaPayloadField
    threshold: float
    aoi: str
    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    area_km_2: float | Unset = UNSET
    max_value: float | Unset = UNSET
    coverage_pct: int | Unset = UNSET
    automated: bool | Unset = UNSET
    not_a_warning: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field = self.field.value

        threshold = self.threshold

        aoi = self.aoi

        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        area_km_2 = self.area_km_2

        max_value = self.max_value

        coverage_pct = self.coverage_pct

        automated = self.automated

        not_a_warning = self.not_a_warning

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "field": field,
                "threshold": threshold,
                "aoi": aoi,
                "status": status,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if area_km_2 is not UNSET:
            field_dict["areaKm2"] = area_km_2
        if max_value is not UNSET:
            field_dict["maxValue"] = max_value
        if coverage_pct is not UNSET:
            field_dict["coveragePct"] = coverage_pct
        if automated is not UNSET:
            field_dict["automated"] = automated
        if not_a_warning is not UNSET:
            field_dict["notAWarning"] = not_a_warning

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        field = WxAreaPayloadField(d.pop("field"))

        threshold = d.pop("threshold")

        aoi = d.pop("aoi")

        status = WeatherSectionStatus(d.pop("status"))

        _as_of = d.pop("asOf", UNSET)
        as_of: datetime.datetime | Unset
        if isinstance(_as_of, Unset):
            as_of = UNSET
        else:
            as_of = datetime.datetime.fromisoformat(_as_of)

        area_km_2 = d.pop("areaKm2", UNSET)

        max_value = d.pop("maxValue", UNSET)

        coverage_pct = d.pop("coveragePct", UNSET)

        automated = d.pop("automated", UNSET)

        not_a_warning = d.pop("notAWarning", UNSET)

        wx_area_payload = cls(
            field=field,
            threshold=threshold,
            aoi=aoi,
            status=status,
            as_of=as_of,
            area_km_2=area_km_2,
            max_value=max_value,
            coverage_pct=coverage_pct,
            automated=automated,
            not_a_warning=not_a_warning,
        )

        wx_area_payload.additional_properties = d
        return wx_area_payload

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
