from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.field_spec_type import FieldSpecType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.field_spec_enum_labels_type_0 import FieldSpecEnumLabelsType0


T = TypeVar("T", bound="FieldSpec")


@_attrs_define
class FieldSpec:
    """Declared payload field (descriptor `fieldSpecs`): JSON type, UCUM unit, label and optional vertical datum

    Attributes:
        type_ (FieldSpecType):
        unit (None | str | Unset): UCUM unit code, e.g. `m`, `km`, `[kn_i]`, `[ft_i]`, `deg`, `s`, `1` Example: km.
        unit_label (None | str | Unset): Human-readable unit label (emitted as `x-unitLabel` by getKindSchema)
        label (None | str | Unset): Display label Example: Depth.
        description (None | str | Unset):
        datum (None | str | Unset): Vertical datum, for vertical fields only: MSL, WGS84, AGL, PRESSURE or free text
            Example: MSL.
        format_ (None | str | Unset): Display format hint (emitted as `x-format` by getKindSchema)
        enum_labels (FieldSpecEnumLabelsType0 | None | Unset): Allowed values mapped to display labels (emitted as
            `enum` by getKindSchema)
        kinds (list[str] | Unset): Kinds this spec applies to; empty means every kind of the source
        no_link (bool | Unset): Display hint: never render this value as a link (hostile URLs, 076) Default: False.
    """

    type_: FieldSpecType
    unit: None | str | Unset = UNSET
    unit_label: None | str | Unset = UNSET
    label: None | str | Unset = UNSET
    description: None | str | Unset = UNSET
    datum: None | str | Unset = UNSET
    format_: None | str | Unset = UNSET
    enum_labels: FieldSpecEnumLabelsType0 | None | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    no_link: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.field_spec_enum_labels_type_0 import FieldSpecEnumLabelsType0

        type_ = self.type_.value

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        unit_label: None | str | Unset
        if isinstance(self.unit_label, Unset):
            unit_label = UNSET
        else:
            unit_label = self.unit_label

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        datum: None | str | Unset
        if isinstance(self.datum, Unset):
            datum = UNSET
        else:
            datum = self.datum

        format_: None | str | Unset
        if isinstance(self.format_, Unset):
            format_ = UNSET
        else:
            format_ = self.format_

        enum_labels: dict[str, Any] | None | Unset
        if isinstance(self.enum_labels, Unset):
            enum_labels = UNSET
        elif isinstance(self.enum_labels, FieldSpecEnumLabelsType0):
            enum_labels = self.enum_labels.to_dict()
        else:
            enum_labels = self.enum_labels

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        no_link = self.no_link

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if unit is not UNSET:
            field_dict["unit"] = unit
        if unit_label is not UNSET:
            field_dict["unitLabel"] = unit_label
        if label is not UNSET:
            field_dict["label"] = label
        if description is not UNSET:
            field_dict["description"] = description
        if datum is not UNSET:
            field_dict["datum"] = datum
        if format_ is not UNSET:
            field_dict["format"] = format_
        if enum_labels is not UNSET:
            field_dict["enumLabels"] = enum_labels
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if no_link is not UNSET:
            field_dict["noLink"] = no_link

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.field_spec_enum_labels_type_0 import FieldSpecEnumLabelsType0

        d = dict(src_dict)
        type_ = FieldSpecType(d.pop("type"))

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        def _parse_unit_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit_label = _parse_unit_label(d.pop("unitLabel", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_datum(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        datum = _parse_datum(d.pop("datum", UNSET))

        def _parse_format_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        format_ = _parse_format_(d.pop("format", UNSET))

        def _parse_enum_labels(data: object) -> FieldSpecEnumLabelsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                enum_labels_type_0 = FieldSpecEnumLabelsType0.from_dict(data)

                return enum_labels_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FieldSpecEnumLabelsType0 | None | Unset, data)

        enum_labels = _parse_enum_labels(d.pop("enumLabels", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        no_link = d.pop("noLink", UNSET)

        field_spec = cls(
            type_=type_,
            unit=unit,
            unit_label=unit_label,
            label=label,
            description=description,
            datum=datum,
            format_=format_,
            enum_labels=enum_labels,
            kinds=kinds,
            no_link=no_link,
        )

        field_spec.additional_properties = d
        return field_spec

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
