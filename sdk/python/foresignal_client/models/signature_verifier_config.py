from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.signature_verifier_config_scheme import SignatureVerifierConfigScheme
from ..types import UNSET, Unset

T = TypeVar("T", bound="SignatureVerifierConfig")


@_attrs_define
class SignatureVerifierConfig:
    """Per-format webhook signature verification for a custom source (024). Secrets are write-only.

    Attributes:
        scheme (SignatureVerifierConfigScheme): `hmac-sha256`: header (default X-Signature-256) `sha256=<hex>` over the
            raw body; `standard-webhooks`: webhook-id/-timestamp/-signature `v1,<base64>` over `id.timestamp.body`, ±5 min,
            replayed ids rejected for 10 min; `foresignal`: X-Foresignal-Timestamp + X-Foresignal-Signature
            `sha256=HMAC("<ts>.<body>")`
        header (None | str | Unset): hmac-sha256 only; default X-Signature-256
        secret (str | Unset): Write-only; omit on update to keep the stored secret
        secret_set (bool | Unset):
        key_optional (bool | Unset): When true a valid signature alone authenticates; otherwise the ingest key is
            required too Default: False.
    """

    scheme: SignatureVerifierConfigScheme
    header: None | str | Unset = UNSET
    secret: str | Unset = UNSET
    secret_set: bool | Unset = UNSET
    key_optional: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        scheme = self.scheme.value

        header: None | str | Unset
        if isinstance(self.header, Unset):
            header = UNSET
        else:
            header = self.header

        secret = self.secret

        secret_set = self.secret_set

        key_optional = self.key_optional

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "scheme": scheme,
            }
        )
        if header is not UNSET:
            field_dict["header"] = header
        if secret is not UNSET:
            field_dict["secret"] = secret
        if secret_set is not UNSET:
            field_dict["secretSet"] = secret_set
        if key_optional is not UNSET:
            field_dict["keyOptional"] = key_optional

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        scheme = SignatureVerifierConfigScheme(d.pop("scheme"))

        def _parse_header(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        header = _parse_header(d.pop("header", UNSET))

        secret = d.pop("secret", UNSET)

        secret_set = d.pop("secretSet", UNSET)

        key_optional = d.pop("keyOptional", UNSET)

        signature_verifier_config = cls(
            scheme=scheme,
            header=header,
            secret=secret,
            secret_set=secret_set,
            key_optional=key_optional,
        )

        signature_verifier_config.additional_properties = d
        return signature_verifier_config

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
