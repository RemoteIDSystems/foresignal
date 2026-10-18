from __future__ import annotations

from collections.abc import Mapping
from io import BytesIO
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from .. import types
from ..types import UNSET, File, Unset

T = TypeVar("T", bound="ImportExchangePackageBody")


@_attrs_define
class ImportExchangePackageBody:
    """Import form

    Attributes:
        file (File):
        jwks (None | str | Unset): Sender JWKS JSON to pin
    """

    file: File
    jwks: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        file = self.file.to_tuple()

        jwks: None | str | Unset
        if isinstance(self.jwks, Unset):
            jwks = UNSET
        else:
            jwks = self.jwks

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "file": file,
            }
        )
        if jwks is not UNSET:
            field_dict["jwks"] = jwks

        return field_dict

    def to_multipart(self) -> types.RequestFiles:
        files: types.RequestFiles = []

        files.append(("file", self.file.to_tuple()))

        if not isinstance(self.jwks, Unset):
            if isinstance(self.jwks, str):
                files.append(("jwks", (None, str(self.jwks).encode(), "text/plain")))
            else:
                files.append(("jwks", (None, str(self.jwks).encode(), "text/plain")))

        for prop_name, prop in self.additional_properties.items():
            files.append((prop_name, (None, str(prop).encode(), "text/plain")))

        return files

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        file = File(payload=BytesIO(d.pop("file")))

        def _parse_jwks(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        jwks = _parse_jwks(d.pop("jwks", UNSET))

        import_exchange_package_body = cls(
            file=file,
            jwks=jwks,
        )

        import_exchange_package_body.additional_properties = d
        return import_exchange_package_body

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
