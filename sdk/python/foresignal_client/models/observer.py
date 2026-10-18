from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Observer")


@_attrs_define
class Observer:
    """A receiver that observed this version (≤ 8 per version; returned only when gated in)

    Attributes:
        receiver_id (str):
        frequency_hz (int | None | Unset):
        rssi (float | None | Unset):
        snr (float | None | Unset):
        decode_quality (float | None | Unset):
    """

    receiver_id: str
    frequency_hz: int | None | Unset = UNSET
    rssi: float | None | Unset = UNSET
    snr: float | None | Unset = UNSET
    decode_quality: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        receiver_id = self.receiver_id

        frequency_hz: int | None | Unset
        if isinstance(self.frequency_hz, Unset):
            frequency_hz = UNSET
        else:
            frequency_hz = self.frequency_hz

        rssi: float | None | Unset
        if isinstance(self.rssi, Unset):
            rssi = UNSET
        else:
            rssi = self.rssi

        snr: float | None | Unset
        if isinstance(self.snr, Unset):
            snr = UNSET
        else:
            snr = self.snr

        decode_quality: float | None | Unset
        if isinstance(self.decode_quality, Unset):
            decode_quality = UNSET
        else:
            decode_quality = self.decode_quality

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "receiverId": receiver_id,
            }
        )
        if frequency_hz is not UNSET:
            field_dict["frequencyHz"] = frequency_hz
        if rssi is not UNSET:
            field_dict["rssi"] = rssi
        if snr is not UNSET:
            field_dict["snr"] = snr
        if decode_quality is not UNSET:
            field_dict["decodeQuality"] = decode_quality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        receiver_id = d.pop("receiverId")

        def _parse_frequency_hz(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        frequency_hz = _parse_frequency_hz(d.pop("frequencyHz", UNSET))

        def _parse_rssi(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        rssi = _parse_rssi(d.pop("rssi", UNSET))

        def _parse_snr(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        snr = _parse_snr(d.pop("snr", UNSET))

        def _parse_decode_quality(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        decode_quality = _parse_decode_quality(d.pop("decodeQuality", UNSET))

        observer = cls(
            receiver_id=receiver_id,
            frequency_hz=frequency_hz,
            rssi=rssi,
            snr=snr,
            decode_quality=decode_quality,
        )

        observer.additional_properties = d
        return observer

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
