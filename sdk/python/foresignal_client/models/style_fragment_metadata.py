from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.style_fragment_metadata_legends import StyleFragmentMetadataLegends


T = TypeVar("T", bound="StyleFragmentMetadata")


@_attrs_define
class StyleFragmentMetadata:
    """Legend links and attribution

    Attributes:
        legends (StyleFragmentMetadataLegends | Unset):
        attribution (list[Attribution] | Unset):
    """

    legends: StyleFragmentMetadataLegends | Unset = UNSET
    attribution: list[Attribution] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        legends: dict[str, Any] | Unset = UNSET
        if not isinstance(self.legends, Unset):
            legends = self.legends.to_dict()

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if legends is not UNSET:
            field_dict["legends"] = legends
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.style_fragment_metadata_legends import (
            StyleFragmentMetadataLegends,
        )

        d = dict(src_dict)
        _legends = d.pop("legends", UNSET)
        legends: StyleFragmentMetadataLegends | Unset
        if isinstance(_legends, Unset):
            legends = UNSET
        else:
            legends = StyleFragmentMetadataLegends.from_dict(_legends)

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        style_fragment_metadata = cls(
            legends=legends,
            attribution=attribution,
        )

        style_fragment_metadata.additional_properties = d
        return style_fragment_metadata

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
