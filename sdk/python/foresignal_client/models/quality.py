from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.quality_extra import QualityExtra


T = TypeVar("T", bound="Quality")


@_attrs_define
class Quality:
    """Observation quality (never hashed); validated on ingest (Core-Data-Model §14)

    Attributes:
        accuracy_meters (float | None | Unset): Horizontal accuracy in metres
        confidence (float | None | Unset): Confidence 0–1
        position_source (None | str | Unset): How the position was obtained (adapter-defined `PositionSource`)
        extra (QualityExtra | Unset): Namespaced scalar extras, e.g. `adsb.seen_pos` Example: {'adsb.seen_pos': 1.2}.
    """

    accuracy_meters: float | None | Unset = UNSET
    confidence: float | None | Unset = UNSET
    position_source: None | str | Unset = UNSET
    extra: QualityExtra | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        accuracy_meters: float | None | Unset
        if isinstance(self.accuracy_meters, Unset):
            accuracy_meters = UNSET
        else:
            accuracy_meters = self.accuracy_meters

        confidence: float | None | Unset
        if isinstance(self.confidence, Unset):
            confidence = UNSET
        else:
            confidence = self.confidence

        position_source: None | str | Unset
        if isinstance(self.position_source, Unset):
            position_source = UNSET
        else:
            position_source = self.position_source

        extra: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extra, Unset):
            extra = self.extra.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if accuracy_meters is not UNSET:
            field_dict["accuracyMeters"] = accuracy_meters
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if position_source is not UNSET:
            field_dict["positionSource"] = position_source
        if extra is not UNSET:
            field_dict["extra"] = extra

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.quality_extra import QualityExtra

        d = dict(src_dict)

        def _parse_accuracy_meters(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        accuracy_meters = _parse_accuracy_meters(d.pop("accuracyMeters", UNSET))

        def _parse_confidence(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        confidence = _parse_confidence(d.pop("confidence", UNSET))

        def _parse_position_source(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        position_source = _parse_position_source(d.pop("positionSource", UNSET))

        _extra = d.pop("extra", UNSET)
        extra: QualityExtra | Unset
        if isinstance(_extra, Unset):
            extra = UNSET
        else:
            extra = QualityExtra.from_dict(_extra)

        quality = cls(
            accuracy_meters=accuracy_meters,
            confidence=confidence,
            position_source=position_source,
            extra=extra,
        )

        quality.additional_properties = d
        return quality

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
