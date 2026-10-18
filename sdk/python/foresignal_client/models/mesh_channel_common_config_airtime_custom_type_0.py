from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MeshChannelCommonConfigAirtimeCustomType0")


@_attrs_define
class MeshChannelCommonConfigAirtimeCustomType0:
    """Radio settings when `airtimePreset=custom` (required then; the default preset of `meshcore`)

    Attributes:
        spreading_factor (int | Unset):
        bandwidth_khz (float | Unset):
        coding_rate (int | Unset): Denominator of 4/x
        preamble_symbols (int | Unset):
    """

    spreading_factor: int | Unset = UNSET
    bandwidth_khz: float | Unset = UNSET
    coding_rate: int | Unset = UNSET
    preamble_symbols: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        spreading_factor = self.spreading_factor

        bandwidth_khz = self.bandwidth_khz

        coding_rate = self.coding_rate

        preamble_symbols = self.preamble_symbols

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if spreading_factor is not UNSET:
            field_dict["spreadingFactor"] = spreading_factor
        if bandwidth_khz is not UNSET:
            field_dict["bandwidthKhz"] = bandwidth_khz
        if coding_rate is not UNSET:
            field_dict["codingRate"] = coding_rate
        if preamble_symbols is not UNSET:
            field_dict["preambleSymbols"] = preamble_symbols

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        spreading_factor = d.pop("spreadingFactor", UNSET)

        bandwidth_khz = d.pop("bandwidthKhz", UNSET)

        coding_rate = d.pop("codingRate", UNSET)

        preamble_symbols = d.pop("preambleSymbols", UNSET)

        mesh_channel_common_config_airtime_custom_type_0 = cls(
            spreading_factor=spreading_factor,
            bandwidth_khz=bandwidth_khz,
            coding_rate=coding_rate,
            preamble_symbols=preamble_symbols,
        )

        mesh_channel_common_config_airtime_custom_type_0.additional_properties = d
        return mesh_channel_common_config_airtime_custom_type_0

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
