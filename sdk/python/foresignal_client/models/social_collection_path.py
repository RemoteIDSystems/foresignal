from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_collection_path_path import SocialCollectionPathPath
from ..models.social_collection_type import SocialCollectionType

T = TypeVar("T", bound="SocialCollectionPath")


@_attrs_define
class SocialCollectionPath:
    """One collection path of an adapter

    Attributes:
        path (SocialCollectionPathPath):
        collection_type (SocialCollectionType): Measured versus sampled (135): `complete` stream for its filter
            (Jetstream, Mastodon hashtag stream, X counts), `paged` search (complete within the API's window and limits),
            `sampled` ranked or targeted sample (top-post search, model-mediated search, cues). Only complete and paged
            paths feed baselines and velocity
        enabled (bool):
        feeds_baselines (bool): True only for complete and paged paths
    """

    path: SocialCollectionPathPath
    collection_type: SocialCollectionType
    enabled: bool
    feeds_baselines: bool
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        path = self.path.value

        collection_type = self.collection_type.value

        enabled = self.enabled

        feeds_baselines = self.feeds_baselines

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "path": path,
                "collectionType": collection_type,
                "enabled": enabled,
                "feedsBaselines": feeds_baselines,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = SocialCollectionPathPath(d.pop("path"))

        collection_type = SocialCollectionType(d.pop("collectionType"))

        enabled = d.pop("enabled")

        feeds_baselines = d.pop("feedsBaselines")

        social_collection_path = cls(
            path=path,
            collection_type=collection_type,
            enabled=enabled,
            feeds_baselines=feeds_baselines,
        )

        social_collection_path.additional_properties = d
        return social_collection_path

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
