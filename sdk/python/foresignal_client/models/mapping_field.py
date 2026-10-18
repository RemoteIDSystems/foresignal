from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.mapping_field_type import MappingFieldType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_field_unit import MappingFieldUnit


T = TypeVar("T", bound="MappingField")


@_attrs_define
class MappingField:
    """One payload field produced by a RecordMapping (059)

    Attributes:
        name (str): Top-level payload key
        path (str): JSONPath, or `$pairs.<key>`
        type_ (MappingFieldType):
        unit (MappingFieldUnit | Unset): Unit conversion from the built-in UCUM table; unknown pairs fail validation
        scale (float | Unset):
        offset (float | Unset):
        default (Any | Unset): Value used when the path is absent or holds a sentinel
        sentinels (list[Any] | Unset): Input values treated as absent (e.g. AIS heading 511), compared before conversion
    """

    name: str
    path: str
    type_: MappingFieldType
    unit: MappingFieldUnit | Unset = UNSET
    scale: float | Unset = UNSET
    offset: float | Unset = UNSET
    default: Any | Unset = UNSET
    sentinels: list[Any] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        path = self.path

        type_ = self.type_.value

        unit: dict[str, Any] | Unset = UNSET
        if not isinstance(self.unit, Unset):
            unit = self.unit.to_dict()

        scale = self.scale

        offset = self.offset

        default = self.default

        sentinels: list[Any] | Unset = UNSET
        if not isinstance(self.sentinels, Unset):
            sentinels = self.sentinels

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "name": name,
                "path": path,
                "type": type_,
            }
        )
        if unit is not UNSET:
            field_dict["unit"] = unit
        if scale is not UNSET:
            field_dict["scale"] = scale
        if offset is not UNSET:
            field_dict["offset"] = offset
        if default is not UNSET:
            field_dict["default"] = default
        if sentinels is not UNSET:
            field_dict["sentinels"] = sentinels

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_field_unit import MappingFieldUnit

        d = dict(src_dict)
        name = d.pop("name")

        path = d.pop("path")

        type_ = MappingFieldType(d.pop("type"))

        _unit = d.pop("unit", UNSET)
        unit: MappingFieldUnit | Unset
        if isinstance(_unit, Unset):
            unit = UNSET
        else:
            unit = MappingFieldUnit.from_dict(_unit)

        scale = d.pop("scale", UNSET)

        offset = d.pop("offset", UNSET)

        default = d.pop("default", UNSET)

        sentinels = cast(list[Any], d.pop("sentinels", UNSET))

        mapping_field = cls(
            name=name,
            path=path,
            type_=type_,
            unit=unit,
            scale=scale,
            offset=offset,
            default=default,
            sentinels=sentinels,
        )

        return mapping_field
