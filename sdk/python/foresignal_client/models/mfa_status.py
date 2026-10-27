from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mfa_recovery_codes import MfaRecoveryCodes


T = TypeVar("T", bound="MfaStatus")


@_attrs_define
class MfaStatus:
    """The caller's MFA state

    Attributes:
        enabled (bool):
        enrolled_at (datetime.datetime | None | Unset):
        required (bool | Unset): True when a role the user holds (Admin/Operator) requires MFA
        recovery_codes (MfaRecoveryCodes | Unset): Single-use recovery codes, shown once at generation (D1/187)
    """

    enabled: bool
    enrolled_at: datetime.datetime | None | Unset = UNSET
    required: bool | Unset = UNSET
    recovery_codes: MfaRecoveryCodes | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        enrolled_at: None | str | Unset
        if isinstance(self.enrolled_at, Unset):
            enrolled_at = UNSET
        elif isinstance(self.enrolled_at, datetime.datetime):
            enrolled_at = self.enrolled_at.isoformat()
        else:
            enrolled_at = self.enrolled_at

        required = self.required

        recovery_codes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.recovery_codes, Unset):
            recovery_codes = self.recovery_codes.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
            }
        )
        if enrolled_at is not UNSET:
            field_dict["enrolledAt"] = enrolled_at
        if required is not UNSET:
            field_dict["required"] = required
        if recovery_codes is not UNSET:
            field_dict["recoveryCodes"] = recovery_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mfa_recovery_codes import MfaRecoveryCodes

        d = dict(src_dict)
        enabled = d.pop("enabled")

        def _parse_enrolled_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                enrolled_at_type_0 = datetime.datetime.fromisoformat(data)

                return enrolled_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        enrolled_at = _parse_enrolled_at(d.pop("enrolledAt", UNSET))

        required = d.pop("required", UNSET)

        _recovery_codes = d.pop("recoveryCodes", UNSET)
        recovery_codes: MfaRecoveryCodes | Unset
        if isinstance(_recovery_codes, Unset):
            recovery_codes = UNSET
        else:
            recovery_codes = MfaRecoveryCodes.from_dict(_recovery_codes)

        mfa_status = cls(
            enabled=enabled,
            enrolled_at=enrolled_at,
            required=required,
            recovery_codes=recovery_codes,
        )

        mfa_status.additional_properties = d
        return mfa_status

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
