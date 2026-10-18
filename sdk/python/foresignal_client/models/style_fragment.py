from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.style_fragment_layers_item import StyleFragmentLayersItem
    from ..models.style_fragment_metadata import StyleFragmentMetadata
    from ..models.style_fragment_sources import StyleFragmentSources


T = TypeVar("T", bound="StyleFragment")


@_attrs_define
class StyleFragment:
    """MapLibre style fragment with concrete content-addressed URLs and namespaced layer ids (`foresignal-{product}`), for
    merging below a layer of the client's basemap

        Attributes:
            version (Literal[8]):
            sources (StyleFragmentSources):
            layers (list[StyleFragmentLayersItem]):
            metadata (StyleFragmentMetadata | Unset): Legend links and attribution
    """

    version: Literal[8]
    sources: StyleFragmentSources
    layers: list[StyleFragmentLayersItem]
    metadata: StyleFragmentMetadata | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version

        sources = self.sources.to_dict()

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        metadata: dict[str, Any] | Unset = UNSET
        if not isinstance(self.metadata, Unset):
            metadata = self.metadata.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "sources": sources,
                "layers": layers,
            }
        )
        if metadata is not UNSET:
            field_dict["metadata"] = metadata

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.style_fragment_layers_item import StyleFragmentLayersItem
        from ..models.style_fragment_metadata import StyleFragmentMetadata
        from ..models.style_fragment_sources import StyleFragmentSources

        d = dict(src_dict)
        version = cast(Literal[8], d.pop("version"))
        if version != 8:
            raise ValueError(f"version must match const 8, got '{version}'")

        sources = StyleFragmentSources.from_dict(d.pop("sources"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = StyleFragmentLayersItem.from_dict(layers_item_data)

            layers.append(layers_item)

        _metadata = d.pop("metadata", UNSET)
        metadata: StyleFragmentMetadata | Unset
        if isinstance(_metadata, Unset):
            metadata = UNSET
        else:
            metadata = StyleFragmentMetadata.from_dict(_metadata)

        style_fragment = cls(
            version=version,
            sources=sources,
            layers=layers,
            metadata=metadata,
        )

        style_fragment.additional_properties = d
        return style_fragment

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
