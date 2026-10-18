from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcCoverageCollectionDataQueriesArea")


@_attrs_define
class OgcCoverageCollectionDataQueriesArea:
    """EDR area query

    Attributes:
        link (OgcLink | Unset): OGC API link
    """

    link: OgcLink | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        link: dict[str, Any] | Unset = UNSET
        if not isinstance(self.link, Unset):
            link = self.link.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if link is not UNSET:
            field_dict["link"] = link

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        _link = d.pop("link", UNSET)
        link: OgcLink | Unset
        if isinstance(_link, Unset):
            link = UNSET
        else:
            link = OgcLink.from_dict(_link)

        ogc_coverage_collection_data_queries_area = cls(
            link=link,
        )

        ogc_coverage_collection_data_queries_area.additional_properties = d
        return ogc_coverage_collection_data_queries_area

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
