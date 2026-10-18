from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.signing_key_info_alg import SigningKeyInfoAlg
from ..models.signing_key_info_status import SigningKeyInfoStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.jwk import Jwk


T = TypeVar("T", bound="SigningKeyInfo")


@_attrs_define
class SigningKeyInfo:
    """Instance Ed25519 signing key metadata (109); the private key never leaves the server

    Attributes:
        kid (str):
        alg (SigningKeyInfoAlg):
        status (SigningKeyInfoStatus):
        created_at (datetime.datetime):
        jwk (Jwk): Public Ed25519 signing key as an OKP JWK (RFC 8037)
        retired_at (datetime.datetime | None | Unset):
        remove_after (datetime.datetime | None | Unset): When a retired key leaves the JWKS (retiredAt +
            signing.retainRetired)
    """

    kid: str
    alg: SigningKeyInfoAlg
    status: SigningKeyInfoStatus
    created_at: datetime.datetime
    jwk: Jwk
    retired_at: datetime.datetime | None | Unset = UNSET
    remove_after: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kid = self.kid

        alg = self.alg.value

        status = self.status.value

        created_at = self.created_at.isoformat()

        jwk = self.jwk.to_dict()

        retired_at: None | str | Unset
        if isinstance(self.retired_at, Unset):
            retired_at = UNSET
        elif isinstance(self.retired_at, datetime.datetime):
            retired_at = self.retired_at.isoformat()
        else:
            retired_at = self.retired_at

        remove_after: None | str | Unset
        if isinstance(self.remove_after, Unset):
            remove_after = UNSET
        elif isinstance(self.remove_after, datetime.datetime):
            remove_after = self.remove_after.isoformat()
        else:
            remove_after = self.remove_after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kid": kid,
                "alg": alg,
                "status": status,
                "createdAt": created_at,
                "jwk": jwk,
            }
        )
        if retired_at is not UNSET:
            field_dict["retiredAt"] = retired_at
        if remove_after is not UNSET:
            field_dict["removeAfter"] = remove_after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.jwk import Jwk

        d = dict(src_dict)
        kid = d.pop("kid")

        alg = SigningKeyInfoAlg(d.pop("alg"))

        status = SigningKeyInfoStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        jwk = Jwk.from_dict(d.pop("jwk"))

        def _parse_retired_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                retired_at_type_0 = datetime.datetime.fromisoformat(data)

                return retired_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        retired_at = _parse_retired_at(d.pop("retiredAt", UNSET))

        def _parse_remove_after(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                remove_after_type_0 = datetime.datetime.fromisoformat(data)

                return remove_after_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        remove_after = _parse_remove_after(d.pop("removeAfter", UNSET))

        signing_key_info = cls(
            kid=kid,
            alg=alg,
            status=status,
            created_at=created_at,
            jwk=jwk,
            retired_at=retired_at,
            remove_after=remove_after,
        )

        signing_key_info.additional_properties = d
        return signing_key_info

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
