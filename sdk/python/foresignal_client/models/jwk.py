from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.jwk_alg import JwkAlg
from ..models.jwk_crv import JwkCrv
from ..models.jwk_kty import JwkKty
from ..models.jwk_use import JwkUse

T = TypeVar("T", bound="Jwk")


@_attrs_define
class Jwk:
    """Public Ed25519 signing key as an OKP JWK (RFC 8037)

    Attributes:
        kty (JwkKty):
        crv (JwkCrv):
        x (str): Base64url public key
        kid (str): RFC 7638 JWK thumbprint
        use (JwkUse):
        alg (JwkAlg): Ed25519 (RFC 9864); EdDSA only when signing.legacyAlg=true
    """

    kty: JwkKty
    crv: JwkCrv
    x: str
    kid: str
    use: JwkUse
    alg: JwkAlg
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kty = self.kty.value

        crv = self.crv.value

        x = self.x

        kid = self.kid

        use = self.use.value

        alg = self.alg.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kty": kty,
                "crv": crv,
                "x": x,
                "kid": kid,
                "use": use,
                "alg": alg,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kty = JwkKty(d.pop("kty"))

        crv = JwkCrv(d.pop("crv"))

        x = d.pop("x")

        kid = d.pop("kid")

        use = JwkUse(d.pop("use"))

        alg = JwkAlg(d.pop("alg"))

        jwk = cls(
            kty=kty,
            crv=crv,
            x=x,
            kid=kid,
            use=use,
            alg=alg,
        )

        jwk.additional_properties = d
        return jwk

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
