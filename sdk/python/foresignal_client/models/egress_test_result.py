from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EgressTestResult")


@_attrs_define
class EgressTestResult:
    """Result of an egress route test (reports the observed exit IP, C1/181)

    Attributes:
        ok (bool):
        exit_ip (None | str | Unset): The public IP the route presented on the test call
        latency_ms (float | None | Unset):
        message (None | str | Unset):
        checked_at (datetime.datetime | Unset):
    """

    ok: bool
    exit_ip: None | str | Unset = UNSET
    latency_ms: float | None | Unset = UNSET
    message: None | str | Unset = UNSET
    checked_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ok = self.ok

        exit_ip: None | str | Unset
        if isinstance(self.exit_ip, Unset):
            exit_ip = UNSET
        else:
            exit_ip = self.exit_ip

        latency_ms: float | None | Unset
        if isinstance(self.latency_ms, Unset):
            latency_ms = UNSET
        else:
            latency_ms = self.latency_ms

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        checked_at: str | Unset = UNSET
        if not isinstance(self.checked_at, Unset):
            checked_at = self.checked_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ok": ok,
            }
        )
        if exit_ip is not UNSET:
            field_dict["exitIp"] = exit_ip
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms
        if message is not UNSET:
            field_dict["message"] = message
        if checked_at is not UNSET:
            field_dict["checkedAt"] = checked_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ok = d.pop("ok")

        def _parse_exit_ip(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        exit_ip = _parse_exit_ip(d.pop("exitIp", UNSET))

        def _parse_latency_ms(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        latency_ms = _parse_latency_ms(d.pop("latencyMs", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        _checked_at = d.pop("checkedAt", UNSET)
        checked_at: datetime.datetime | Unset
        if isinstance(_checked_at, Unset):
            checked_at = UNSET
        else:
            checked_at = datetime.datetime.fromisoformat(_checked_at)

        egress_test_result = cls(
            ok=ok,
            exit_ip=exit_ip,
            latency_ms=latency_ms,
            message=message,
            checked_at=checked_at,
        )

        egress_test_result.additional_properties = d
        return egress_test_result

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
