from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DcpObservationPayload")


@_attrs_define
class DcpObservationPayload:
    """Kind `dcp-observation` (`satdump-{station}` DCS): an environmental data platform message located through
    `dcp_location`

        Attributes:
            address (str):
            name (None | str | Unset):
            agency (None | str | Unset):
            channel (None | str | Unset):
            signal_dbm (float | None | Unset):
            frequency_offset_hz (float | None | Unset):
            failure_code (None | str | Unset):
            data_ascii (None | str | Unset):
            data_hex (None | str | Unset):
            decoded (None | Unset):
    """

    address: str
    name: None | str | Unset = UNSET
    agency: None | str | Unset = UNSET
    channel: None | str | Unset = UNSET
    signal_dbm: float | None | Unset = UNSET
    frequency_offset_hz: float | None | Unset = UNSET
    failure_code: None | str | Unset = UNSET
    data_ascii: None | str | Unset = UNSET
    data_hex: None | str | Unset = UNSET
    decoded: None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        address = self.address

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        agency: None | str | Unset
        if isinstance(self.agency, Unset):
            agency = UNSET
        else:
            agency = self.agency

        channel: None | str | Unset
        if isinstance(self.channel, Unset):
            channel = UNSET
        else:
            channel = self.channel

        signal_dbm: float | None | Unset
        if isinstance(self.signal_dbm, Unset):
            signal_dbm = UNSET
        else:
            signal_dbm = self.signal_dbm

        frequency_offset_hz: float | None | Unset
        if isinstance(self.frequency_offset_hz, Unset):
            frequency_offset_hz = UNSET
        else:
            frequency_offset_hz = self.frequency_offset_hz

        failure_code: None | str | Unset
        if isinstance(self.failure_code, Unset):
            failure_code = UNSET
        else:
            failure_code = self.failure_code

        data_ascii: None | str | Unset
        if isinstance(self.data_ascii, Unset):
            data_ascii = UNSET
        else:
            data_ascii = self.data_ascii

        data_hex: None | str | Unset
        if isinstance(self.data_hex, Unset):
            data_hex = UNSET
        else:
            data_hex = self.data_hex

        decoded = self.decoded

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "address": address,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if agency is not UNSET:
            field_dict["agency"] = agency
        if channel is not UNSET:
            field_dict["channel"] = channel
        if signal_dbm is not UNSET:
            field_dict["signalDbm"] = signal_dbm
        if frequency_offset_hz is not UNSET:
            field_dict["frequencyOffsetHz"] = frequency_offset_hz
        if failure_code is not UNSET:
            field_dict["failureCode"] = failure_code
        if data_ascii is not UNSET:
            field_dict["dataAscii"] = data_ascii
        if data_hex is not UNSET:
            field_dict["dataHex"] = data_hex
        if decoded is not UNSET:
            field_dict["decoded"] = decoded

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        address = d.pop("address")

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        def _parse_agency(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        agency = _parse_agency(d.pop("agency", UNSET))

        def _parse_channel(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        channel = _parse_channel(d.pop("channel", UNSET))

        def _parse_signal_dbm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        signal_dbm = _parse_signal_dbm(d.pop("signalDbm", UNSET))

        def _parse_frequency_offset_hz(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        frequency_offset_hz = _parse_frequency_offset_hz(
            d.pop("frequencyOffsetHz", UNSET)
        )

        def _parse_failure_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        failure_code = _parse_failure_code(d.pop("failureCode", UNSET))

        def _parse_data_ascii(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        data_ascii = _parse_data_ascii(d.pop("dataAscii", UNSET))

        def _parse_data_hex(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        data_hex = _parse_data_hex(d.pop("dataHex", UNSET))

        decoded = d.pop("decoded", UNSET)

        dcp_observation_payload = cls(
            address=address,
            name=name,
            agency=agency,
            channel=channel,
            signal_dbm=signal_dbm,
            frequency_offset_hz=frequency_offset_hz,
            failure_code=failure_code,
            data_ascii=data_ascii,
            data_hex=data_hex,
            decoded=decoded,
        )

        dcp_observation_payload.additional_properties = d
        return dcp_observation_payload

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
