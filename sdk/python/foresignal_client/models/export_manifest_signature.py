from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.export_manifest_signature_alg import ExportManifestSignatureAlg

T = TypeVar("T", bound="ExportManifestSignature")


@_attrs_define
class ExportManifestSignature:
    """Detached JWS over manifest.json

    Attributes:
        alg (ExportManifestSignatureAlg):
        kid (str):
        file (str): manifest.json.jws
    """

    alg: ExportManifestSignatureAlg
    kid: str
    file: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alg = self.alg.value

        kid = self.kid

        file = self.file

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alg": alg,
                "kid": kid,
                "file": file,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alg = ExportManifestSignatureAlg(d.pop("alg"))

        kid = d.pop("kid")

        file = d.pop("file")

        export_manifest_signature = cls(
            alg=alg,
            kid=kid,
            file=file,
        )

        export_manifest_signature.additional_properties = d
        return export_manifest_signature

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
