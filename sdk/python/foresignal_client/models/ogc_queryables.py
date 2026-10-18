from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_queryables_type import OgcQueryablesType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_queryables_properties import OgcQueryablesProperties


T = TypeVar("T", bound="OgcQueryables")


@_attrs_define
class OgcQueryables:
    """Queryables as a JSON Schema: top-level payload keys from the kind's fieldSpecs (008) plus title, sourceId,
    observedAt, eventTime, version (112)

        Attributes:
            type_ (OgcQueryablesType):
            properties (OgcQueryablesProperties):
            schema (str | Unset):
            id (str | Unset):
            title (str | Unset):
            additional_properties (bool | Unset):
    """

    type_: OgcQueryablesType
    properties: OgcQueryablesProperties
    schema: str | Unset = UNSET
    id: str | Unset = UNSET
    title: str | Unset = UNSET
    additional_properties: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        properties = self.properties.to_dict()

        schema = self.schema

        id = self.id

        title = self.title

        additional_properties = self.additional_properties

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "properties": properties,
            }
        )
        if schema is not UNSET:
            field_dict["$schema"] = schema
        if id is not UNSET:
            field_dict["$id"] = id
        if title is not UNSET:
            field_dict["title"] = title
        if additional_properties is not UNSET:
            field_dict["additionalProperties"] = additional_properties

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_queryables_properties import OgcQueryablesProperties

        d = dict(src_dict)
        type_ = OgcQueryablesType(d.pop("type"))

        properties = OgcQueryablesProperties.from_dict(d.pop("properties"))

        schema = d.pop("$schema", UNSET)

        id = d.pop("$id", UNSET)

        title = d.pop("title", UNSET)

        additional_properties = d.pop("additionalProperties", UNSET)

        ogc_queryables = cls(
            type_=type_,
            properties=properties,
            schema=schema,
            id=id,
            title=title,
            additional_properties=additional_properties,
        )

        ogc_queryables.additional_properties = d
        return ogc_queryables

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
