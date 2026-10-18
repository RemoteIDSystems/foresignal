from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RemoteRasterBundleMeta")


@_attrs_define
class RemoteRasterBundleMeta:
    """Metadata part of a remote pass bundle

    Attributes:
        pass_folder (str): Pass folder name: one safe path segment (no separators, no `..`, no leading dot), at most 128
            characters
        files (list[str]): Filenames of the file parts (no separators, no `..`)
        product (None | str | Unset): Product directory for every file except `dataset.json` (one safe path segment, for
            example `MSU-MR`); default: the single product `dataset.json` lists, else the pass folder. Send one bundle per
            product for a multi-instrument pass.
    """

    pass_folder: str
    files: list[str]
    product: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pass_folder = self.pass_folder

        files = self.files

        product: None | str | Unset
        if isinstance(self.product, Unset):
            product = UNSET
        else:
            product = self.product

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "passFolder": pass_folder,
                "files": files,
            }
        )
        if product is not UNSET:
            field_dict["product"] = product

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        pass_folder = d.pop("passFolder")

        files = cast(list[str], d.pop("files"))

        def _parse_product(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        product = _parse_product(d.pop("product", UNSET))

        remote_raster_bundle_meta = cls(
            pass_folder=pass_folder,
            files=files,
            product=product,
        )

        remote_raster_bundle_meta.additional_properties = d
        return remote_raster_bundle_meta

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
