from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LoginRequest")


@_attrs_define
class LoginRequest:
    """Email + password credentials, with an optional MFA code (D1)

    Attributes:
        email (str): The user's email (primary identity, normalized)
        password (str):
        mfa_code (None | str | Unset): TOTP or recovery code when MFA is enrolled
    """

    email: str
    password: str
    mfa_code: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        email = self.email

        password = self.password

        mfa_code: None | str | Unset
        if isinstance(self.mfa_code, Unset):
            mfa_code = UNSET
        else:
            mfa_code = self.mfa_code

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "email": email,
                "password": password,
            }
        )
        if mfa_code is not UNSET:
            field_dict["mfaCode"] = mfa_code

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        email = d.pop("email")

        password = d.pop("password")

        def _parse_mfa_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        mfa_code = _parse_mfa_code(d.pop("mfaCode", UNSET))

        login_request = cls(
            email=email,
            password=password,
            mfa_code=mfa_code,
        )

        login_request.additional_properties = d
        return login_request

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
