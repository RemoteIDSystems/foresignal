from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MfaEnrollment")


@_attrs_define
class MfaEnrollment:
    """A pending TOTP enrollment; confirm with verifyMfa before it takes effect (D1/187)

    Attributes:
        secret (str): Base32 TOTP secret (RFC 6238: HMAC-SHA1, 6 digits, 30 s); stored sealed with the instance key,
            shown only here
        otpauth_uri (str): otpauth:// provisioning URI for an authenticator app:
            otpauth://totp/ForeSignal:<email>?secret=…&issuer=ForeSignal
        qr_svg (None | str | Unset): Inline SVG QR of the provisioning URI
    """

    secret: str
    otpauth_uri: str
    qr_svg: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        secret = self.secret

        otpauth_uri = self.otpauth_uri

        qr_svg: None | str | Unset
        if isinstance(self.qr_svg, Unset):
            qr_svg = UNSET
        else:
            qr_svg = self.qr_svg

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "secret": secret,
                "otpauthUri": otpauth_uri,
            }
        )
        if qr_svg is not UNSET:
            field_dict["qrSvg"] = qr_svg

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        secret = d.pop("secret")

        otpauth_uri = d.pop("otpauthUri")

        def _parse_qr_svg(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        qr_svg = _parse_qr_svg(d.pop("qrSvg", UNSET))

        mfa_enrollment = cls(
            secret=secret,
            otpauth_uri=otpauth_uri,
            qr_svg=qr_svg,
        )

        mfa_enrollment.additional_properties = d
        return mfa_enrollment

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
