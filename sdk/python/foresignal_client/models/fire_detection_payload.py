from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.fire_detection_payload_confidence_class import (
    FireDetectionPayloadConfidenceClass,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="FireDetectionPayload")


@_attrs_define
class FireDetectionPayload:
    """Payload of `fire-detection` records (goes-fdc `g19:{col}:{row}`, hms-fire `hms:…`, nasa-firms
    `{SOURCE}:{acq_date}:{HHMM}:{lat}:{lon}`)

        Attributes:
            satellite (str):
            mask_code (int | Unset):
            confidence (int | str | Unset): goes-fdc: `processed`, `saturated`, `cloudy` or `high`. nasa-firms: the raw
                FIRMS value, `l`, `n` or `h` (VIIRS) or an integer 0-100 (MODIS)
            confidence_class (FireDetectionPayloadConfidenceClass | Unset): nasa-firms: VIIRS letters mapped; MODIS below 30
                low, 30-79 nominal, from 80 high
            instrument (str | Unset): nasa-firms: VIIRS, MODIS or OLI, from the product
            bright_k (float | Unset): nasa-firms: bright_ti4 (VIIRS) or brightness (MODIS), K
            bright_2k (float | Unset): nasa-firms: bright_ti5 (VIIRS) or bright_t31 (MODIS), K
            scan_km (float | Unset):
            track_km (float | Unset):
            daynight (str | Unset): nasa-firms: D or N
            version (str | Unset): nasa-firms: collection and processing, for example 2.0NRT
            type_ (int | Unset): nasa-firms MODIS only, when present: 0 vegetation fire, 1 active volcano, 2 other static
                land source, 3 offshore
            temporally_filtered (bool | Unset):
            frp_mw (float | Unset): Fire radiative power, MW. goes-fdc and hms-fire: 2 significant figures; nasa-firms: the
                FIRMS frp column as sent
            temp_k (float | Unset): Rounded to 10 K
            area_km_2 (float | Unset):
            method (str | Unset):
            ecosystem (str | Unset): hms-fire only
            source (str | Unset): `HMS` for hms-fire; the FIRMS product (for example `VIIRS_NOAA20_NRT`) for nasa-firms
            label (str | Unset):
    """

    satellite: str
    mask_code: int | Unset = UNSET
    confidence: int | str | Unset = UNSET
    confidence_class: FireDetectionPayloadConfidenceClass | Unset = UNSET
    instrument: str | Unset = UNSET
    bright_k: float | Unset = UNSET
    bright_2k: float | Unset = UNSET
    scan_km: float | Unset = UNSET
    track_km: float | Unset = UNSET
    daynight: str | Unset = UNSET
    version: str | Unset = UNSET
    type_: int | Unset = UNSET
    temporally_filtered: bool | Unset = UNSET
    frp_mw: float | Unset = UNSET
    temp_k: float | Unset = UNSET
    area_km_2: float | Unset = UNSET
    method: str | Unset = UNSET
    ecosystem: str | Unset = UNSET
    source: str | Unset = UNSET
    label: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        satellite = self.satellite

        mask_code = self.mask_code

        confidence: int | str | Unset
        if isinstance(self.confidence, Unset):
            confidence = UNSET
        else:
            confidence = self.confidence

        confidence_class: str | Unset = UNSET
        if not isinstance(self.confidence_class, Unset):
            confidence_class = self.confidence_class.value

        instrument = self.instrument

        bright_k = self.bright_k

        bright_2k = self.bright_2k

        scan_km = self.scan_km

        track_km = self.track_km

        daynight = self.daynight

        version = self.version

        type_ = self.type_

        temporally_filtered = self.temporally_filtered

        frp_mw = self.frp_mw

        temp_k = self.temp_k

        area_km_2 = self.area_km_2

        method = self.method

        ecosystem = self.ecosystem

        source = self.source

        label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "satellite": satellite,
            }
        )
        if mask_code is not UNSET:
            field_dict["maskCode"] = mask_code
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if confidence_class is not UNSET:
            field_dict["confidenceClass"] = confidence_class
        if instrument is not UNSET:
            field_dict["instrument"] = instrument
        if bright_k is not UNSET:
            field_dict["brightK"] = bright_k
        if bright_2k is not UNSET:
            field_dict["bright2K"] = bright_2k
        if scan_km is not UNSET:
            field_dict["scanKm"] = scan_km
        if track_km is not UNSET:
            field_dict["trackKm"] = track_km
        if daynight is not UNSET:
            field_dict["daynight"] = daynight
        if version is not UNSET:
            field_dict["version"] = version
        if type_ is not UNSET:
            field_dict["type"] = type_
        if temporally_filtered is not UNSET:
            field_dict["temporallyFiltered"] = temporally_filtered
        if frp_mw is not UNSET:
            field_dict["frpMw"] = frp_mw
        if temp_k is not UNSET:
            field_dict["tempK"] = temp_k
        if area_km_2 is not UNSET:
            field_dict["areaKm2"] = area_km_2
        if method is not UNSET:
            field_dict["method"] = method
        if ecosystem is not UNSET:
            field_dict["ecosystem"] = ecosystem
        if source is not UNSET:
            field_dict["source"] = source
        if label is not UNSET:
            field_dict["label"] = label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        satellite = d.pop("satellite")

        mask_code = d.pop("maskCode", UNSET)

        def _parse_confidence(data: object) -> int | str | Unset:
            if isinstance(data, Unset):
                return data
            return cast(int | str | Unset, data)

        confidence = _parse_confidence(d.pop("confidence", UNSET))

        _confidence_class = d.pop("confidenceClass", UNSET)
        confidence_class: FireDetectionPayloadConfidenceClass | Unset
        if isinstance(_confidence_class, Unset):
            confidence_class = UNSET
        else:
            confidence_class = FireDetectionPayloadConfidenceClass(_confidence_class)

        instrument = d.pop("instrument", UNSET)

        bright_k = d.pop("brightK", UNSET)

        bright_2k = d.pop("bright2K", UNSET)

        scan_km = d.pop("scanKm", UNSET)

        track_km = d.pop("trackKm", UNSET)

        daynight = d.pop("daynight", UNSET)

        version = d.pop("version", UNSET)

        type_ = d.pop("type", UNSET)

        temporally_filtered = d.pop("temporallyFiltered", UNSET)

        frp_mw = d.pop("frpMw", UNSET)

        temp_k = d.pop("tempK", UNSET)

        area_km_2 = d.pop("areaKm2", UNSET)

        method = d.pop("method", UNSET)

        ecosystem = d.pop("ecosystem", UNSET)

        source = d.pop("source", UNSET)

        label = d.pop("label", UNSET)

        fire_detection_payload = cls(
            satellite=satellite,
            mask_code=mask_code,
            confidence=confidence,
            confidence_class=confidence_class,
            instrument=instrument,
            bright_k=bright_k,
            bright_2k=bright_2k,
            scan_km=scan_km,
            track_km=track_km,
            daynight=daynight,
            version=version,
            type_=type_,
            temporally_filtered=temporally_filtered,
            frp_mw=frp_mw,
            temp_k=temp_k,
            area_km_2=area_km_2,
            method=method,
            ecosystem=ecosystem,
            source=source,
            label=label,
        )

        fire_detection_payload.additional_properties = d
        return fire_detection_payload

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
