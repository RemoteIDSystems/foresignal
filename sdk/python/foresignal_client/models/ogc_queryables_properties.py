from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ogc_queryables_properties_additional_property import (
        OgcQueryablesPropertiesAdditionalProperty,
    )


T = TypeVar("T", bound="OgcQueryablesProperties")


@_attrs_define
class OgcQueryablesProperties:
    additional_properties: dict[str, OgcQueryablesPropertiesAdditionalProperty] = (
        _attrs_field(init=False, factory=dict)
    )

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.to_dict()

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_queryables_properties_additional_property import (
            OgcQueryablesPropertiesAdditionalProperty,
        )

        d = dict(src_dict)
        ogc_queryables_properties = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = OgcQueryablesPropertiesAdditionalProperty.from_dict(
                prop_dict
            )

            additional_properties[prop_name] = additional_property

        ogc_queryables_properties.additional_properties = additional_properties
        return ogc_queryables_properties

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> OgcQueryablesPropertiesAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: OgcQueryablesPropertiesAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
