from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ogc_coverage_collection_parameter_names_additional_property import (
        OgcCoverageCollectionParameterNamesAdditionalProperty,
    )


T = TypeVar("T", bound="OgcCoverageCollectionParameterNames")


@_attrs_define
class OgcCoverageCollectionParameterNames:
    """EDR parameters with units and nodata"""

    additional_properties: dict[
        str, OgcCoverageCollectionParameterNamesAdditionalProperty
    ] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.to_dict()

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_coverage_collection_parameter_names_additional_property import (
            OgcCoverageCollectionParameterNamesAdditionalProperty,
        )

        d = dict(src_dict)
        ogc_coverage_collection_parameter_names = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = (
                OgcCoverageCollectionParameterNamesAdditionalProperty.from_dict(
                    prop_dict
                )
            )

            additional_properties[prop_name] = additional_property

        ogc_coverage_collection_parameter_names.additional_properties = (
            additional_properties
        )
        return ogc_coverage_collection_parameter_names

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(
        self, key: str
    ) -> OgcCoverageCollectionParameterNamesAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: OgcCoverageCollectionParameterNamesAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
