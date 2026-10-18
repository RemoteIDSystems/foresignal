from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.kind_display import KindDisplay
    from ..models.kind_schema_properties import KindSchemaProperties


T = TypeVar("T", bound="KindSchema")


@_attrs_define
class KindSchema:
    """JSON Schema 2020-12 document merged from the fieldSpecs of every source emitting a kind, with `x-unit`,
    `x-unitLabel`, `x-label`, `x-format`, `x-display` and `x-conflicts` extensions. Each property may also carry
    `x-datum` (vertical datum of a length field); `TIMESTAMP` fields are `["string", "integer"]` with `x-format:
    timestamp`; each `oneOf` variant of a conflicting field lists its `x-sources`.

        Attributes:
            schema (str | Unset):  Example: https://json-schema.org/draft/2020-12/schema.
            title (str | Unset):
            type_ (str | Unset):  Example: object.
            properties (KindSchemaProperties | Unset):
            x_display (KindDisplay | None | Unset):
            x_conflicts (list[str] | Unset): Source ids whose fieldSpecs disagree on a field's type
    """

    schema: str | Unset = UNSET
    title: str | Unset = UNSET
    type_: str | Unset = UNSET
    properties: KindSchemaProperties | Unset = UNSET
    x_display: KindDisplay | None | Unset = UNSET
    x_conflicts: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.kind_display import KindDisplay

        schema = self.schema

        title = self.title

        type_ = self.type_

        properties: dict[str, Any] | Unset = UNSET
        if not isinstance(self.properties, Unset):
            properties = self.properties.to_dict()

        x_display: dict[str, Any] | None | Unset
        if isinstance(self.x_display, Unset):
            x_display = UNSET
        elif isinstance(self.x_display, KindDisplay):
            x_display = self.x_display.to_dict()
        else:
            x_display = self.x_display

        x_conflicts: list[str] | Unset = UNSET
        if not isinstance(self.x_conflicts, Unset):
            x_conflicts = self.x_conflicts

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if schema is not UNSET:
            field_dict["$schema"] = schema
        if title is not UNSET:
            field_dict["title"] = title
        if type_ is not UNSET:
            field_dict["type"] = type_
        if properties is not UNSET:
            field_dict["properties"] = properties
        if x_display is not UNSET:
            field_dict["x-display"] = x_display
        if x_conflicts is not UNSET:
            field_dict["x-conflicts"] = x_conflicts

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.kind_display import KindDisplay
        from ..models.kind_schema_properties import KindSchemaProperties

        d = dict(src_dict)
        schema = d.pop("$schema", UNSET)

        title = d.pop("title", UNSET)

        type_ = d.pop("type", UNSET)

        _properties = d.pop("properties", UNSET)
        properties: KindSchemaProperties | Unset
        if isinstance(_properties, Unset):
            properties = UNSET
        else:
            properties = KindSchemaProperties.from_dict(_properties)

        def _parse_x_display(data: object) -> KindDisplay | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                x_display_type_0 = KindDisplay.from_dict(data)

                return x_display_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(KindDisplay | None | Unset, data)

        x_display = _parse_x_display(d.pop("x-display", UNSET))

        x_conflicts = cast(list[str], d.pop("x-conflicts", UNSET))

        kind_schema = cls(
            schema=schema,
            title=title,
            type_=type_,
            properties=properties,
            x_display=x_display,
            x_conflicts=x_conflicts,
        )

        kind_schema.additional_properties = d
        return kind_schema

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
