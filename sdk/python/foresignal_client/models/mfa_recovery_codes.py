from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="MfaRecoveryCodes")


@_attrs_define
class MfaRecoveryCodes:
    """Single-use recovery codes, shown once at generation (D1/187)

    Attributes:
        codes (list[str]): Ten single-use codes (xxxxx-xxxxx); stored hashed, accepted case-insensitively with or
            without the dash
        generated_at (datetime.datetime):
    """

    codes: list[str]
    generated_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        codes = self.codes

        generated_at = self.generated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "codes": codes,
                "generatedAt": generated_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        codes = cast(list[str], d.pop("codes"))

        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        mfa_recovery_codes = cls(
            codes=codes,
            generated_at=generated_at,
        )

        mfa_recovery_codes.additional_properties = d
        return mfa_recovery_codes

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
