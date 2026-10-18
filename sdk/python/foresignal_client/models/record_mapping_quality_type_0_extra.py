from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
    from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
    from ..models.mapping_value_spec_type_2 import MappingValueSpecType2


T = TypeVar("T", bound="RecordMappingQualityType0Extra")


@_attrs_define
class RecordMappingQualityType0Extra:
    """Namespaced quality extras (Quality.extra), e.g. `lora.rssi`; scalar values only"""

    additional_properties: dict[
        str, MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2
    ] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            if isinstance(prop, MappingValueSpecType0) or isinstance(
                prop, MappingValueSpecType1
            ):
                field_dict[prop_name] = prop.to_dict()
            else:
                field_dict[prop_name] = prop.to_dict()

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
        from ..models.mapping_value_spec_type_2 import MappingValueSpecType2

        d = dict(src_dict)
        record_mapping_quality_type_0_extra = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():

            def _parse_additional_property(
                data: object,
            ) -> MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2:
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_mapping_value_spec_type_0 = (
                        MappingValueSpecType0.from_dict(data)
                    )

                    return componentsschemas_mapping_value_spec_type_0
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    componentsschemas_mapping_value_spec_type_1 = (
                        MappingValueSpecType1.from_dict(data)
                    )

                    return componentsschemas_mapping_value_spec_type_1
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_2 = (
                    MappingValueSpecType2.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_2

            additional_property = _parse_additional_property(prop_dict)

            additional_properties[prop_name] = additional_property

        record_mapping_quality_type_0_extra.additional_properties = (
            additional_properties
        )
        return record_mapping_quality_type_0_extra

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(
        self, key: str
    ) -> MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2:
        return self.additional_properties[key]

    def __setitem__(
        self,
        key: str,
        value: MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2,
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
