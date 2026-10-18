from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_manifest import EdpManifest
    from ..models.edp_validation_request_items_item import EdpValidationRequestItemsItem


T = TypeVar("T", bound="EdpValidationRequest")


@_attrs_define
class EdpValidationRequest:
    """Dry-run validation input

    Attributes:
        manifest (EdpManifest | None | Unset):
        source_id (None | str | Unset):
        items (list[EdpValidationRequestItemsItem] | Unset):
    """

    manifest: EdpManifest | None | Unset = UNSET
    source_id: None | str | Unset = UNSET
    items: list[EdpValidationRequestItemsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.edp_manifest import EdpManifest

        manifest: dict[str, Any] | None | Unset
        if isinstance(self.manifest, Unset):
            manifest = UNSET
        elif isinstance(self.manifest, EdpManifest):
            manifest = self.manifest.to_dict()
        else:
            manifest = self.manifest

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if manifest is not UNSET:
            field_dict["manifest"] = manifest
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if items is not UNSET:
            field_dict["items"] = items

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_manifest import EdpManifest
        from ..models.edp_validation_request_items_item import (
            EdpValidationRequestItemsItem,
        )

        d = dict(src_dict)

        def _parse_manifest(data: object) -> EdpManifest | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                manifest_type_0 = EdpManifest.from_dict(data)

                return manifest_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EdpManifest | None | Unset, data)

        manifest = _parse_manifest(d.pop("manifest", UNSET))

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        _items = d.pop("items", UNSET)
        items: list[EdpValidationRequestItemsItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = EdpValidationRequestItemsItem.from_dict(items_item_data)

                items.append(items_item)

        edp_validation_request = cls(
            manifest=manifest,
            source_id=source_id,
            items=items,
        )

        edp_validation_request.additional_properties = d
        return edp_validation_request

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
