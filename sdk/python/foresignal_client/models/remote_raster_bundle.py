from __future__ import annotations

import json
from collections.abc import Mapping
from io import BytesIO
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from .. import types
from ..types import File

if TYPE_CHECKING:
    from ..models.remote_raster_bundle_meta import RemoteRasterBundleMeta


T = TypeVar("T", bound="RemoteRasterBundle")


@_attrs_define
class RemoteRasterBundle:
    """Multipart remote pass bundle processed like an inbox pass folder

    Attributes:
        meta (RemoteRasterBundleMeta): Metadata part of a remote pass bundle
        files (list[File]): File parts (`dataset.json`, `product.cbor`, PNG, TIF), each ≤ 25 MB; bundle ≤ 200 MB
    """

    meta: RemoteRasterBundleMeta
    files: list[File]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        meta = self.meta.to_dict()

        files = []
        for files_item_data in self.files:
            files_item = files_item_data.to_tuple()

            files.append(files_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "meta": meta,
                "files": files,
            }
        )

        return field_dict

    def to_multipart(self) -> types.RequestFiles:
        files: types.RequestFiles = []

        files.append(
            (
                "meta",
                (None, json.dumps(self.meta.to_dict()).encode(), "application/json"),
            )
        )

        for files_item_element in self.files:
            files.append(("files", files_item_element.to_tuple()))

        for prop_name, prop in self.additional_properties.items():
            files.append((prop_name, (None, str(prop).encode(), "text/plain")))

        return files

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.remote_raster_bundle_meta import RemoteRasterBundleMeta

        d = dict(src_dict)
        meta = RemoteRasterBundleMeta.from_dict(d.pop("meta"))

        files = []
        _files = d.pop("files")
        for files_item_data in _files:
            files_item = File(payload=BytesIO(files_item_data))

            files.append(files_item)

        remote_raster_bundle = cls(
            meta=meta,
            files=files,
        )

        remote_raster_bundle.additional_properties = d
        return remote_raster_bundle

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
