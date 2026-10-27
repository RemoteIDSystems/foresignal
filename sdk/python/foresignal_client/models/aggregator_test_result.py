from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AggregatorTestResult")


@_attrs_define
class AggregatorTestResult:
    """Result of probing an aggregator's local endpoint

    Attributes:
        ok (bool):
        reachable (bool):
        sample_count (int | None | Unset): Objects seen in a sample read
        message (None | str | Unset):
        checked_at (datetime.datetime | Unset):
    """

    ok: bool
    reachable: bool
    sample_count: int | None | Unset = UNSET
    message: None | str | Unset = UNSET
    checked_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ok = self.ok

        reachable = self.reachable

        sample_count: int | None | Unset
        if isinstance(self.sample_count, Unset):
            sample_count = UNSET
        else:
            sample_count = self.sample_count

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
                "reachable": reachable,
            }
        )
        if sample_count is not UNSET:
            field_dict["sampleCount"] = sample_count
        if message is not UNSET:
            field_dict["message"] = message
        if checked_at is not UNSET:
            field_dict["checkedAt"] = checked_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ok = d.pop("ok")

        reachable = d.pop("reachable")

        def _parse_sample_count(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        sample_count = _parse_sample_count(d.pop("sampleCount", UNSET))

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

        aggregator_test_result = cls(
            ok=ok,
            reachable=reachable,
            sample_count=sample_count,
            message=message,
            checked_at=checked_at,
        )

        aggregator_test_result.additional_properties = d
        return aggregator_test_result

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
