from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_feature_collection_type import OgcFeatureCollectionType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.ogc_feature import OgcFeature
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcFeatureCollection")


@_attrs_define
class OgcFeatureCollection:
    """OGC API items response (GeoJSON FeatureCollection with links) (112)

    Attributes:
        type_ (OgcFeatureCollectionType):
        features (list[OgcFeature]):
        links (list[OgcLink]): self, next (opaque keyset cursor), collection
        number_returned (int):
        time_stamp (datetime.datetime):
        number_matched (int | Unset): Only when count=true and at most 100,000
        attributions (list[Attribution] | Unset):
        licence_filtered (int | Unset):
    """

    type_: OgcFeatureCollectionType
    features: list[OgcFeature]
    links: list[OgcLink]
    number_returned: int
    time_stamp: datetime.datetime
    number_matched: int | Unset = UNSET
    attributions: list[Attribution] | Unset = UNSET
    licence_filtered: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        features = []
        for features_item_data in self.features:
            features_item = features_item_data.to_dict()
            features.append(features_item)

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        number_returned = self.number_returned

        time_stamp = self.time_stamp.isoformat()

        number_matched = self.number_matched

        attributions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attributions, Unset):
            attributions = []
            for attributions_item_data in self.attributions:
                attributions_item = attributions_item_data.to_dict()
                attributions.append(attributions_item)

        licence_filtered = self.licence_filtered

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "features": features,
                "links": links,
                "numberReturned": number_returned,
                "timeStamp": time_stamp,
            }
        )
        if number_matched is not UNSET:
            field_dict["numberMatched"] = number_matched
        if attributions is not UNSET:
            field_dict["attributions"] = attributions
        if licence_filtered is not UNSET:
            field_dict["licenceFiltered"] = licence_filtered

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.ogc_feature import OgcFeature
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        type_ = OgcFeatureCollectionType(d.pop("type"))

        features = []
        _features = d.pop("features")
        for features_item_data in _features:
            features_item = OgcFeature.from_dict(features_item_data)

            features.append(features_item)

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        number_returned = d.pop("numberReturned")

        time_stamp = datetime.datetime.fromisoformat(d.pop("timeStamp"))

        number_matched = d.pop("numberMatched", UNSET)

        _attributions = d.pop("attributions", UNSET)
        attributions: list[Attribution] | Unset = UNSET
        if _attributions is not UNSET:
            attributions = []
            for attributions_item_data in _attributions:
                attributions_item = Attribution.from_dict(attributions_item_data)

                attributions.append(attributions_item)

        licence_filtered = d.pop("licenceFiltered", UNSET)

        ogc_feature_collection = cls(
            type_=type_,
            features=features,
            links=links,
            number_returned=number_returned,
            time_stamp=time_stamp,
            number_matched=number_matched,
            attributions=attributions,
            licence_filtered=licence_filtered,
        )

        ogc_feature_collection.additional_properties = d
        return ogc_feature_collection

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
