from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
    from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
    from ..models.mapping_value_spec_type_2 import MappingValueSpecType2
    from ..models.record_mapping_quality_type_0_extra import (
        RecordMappingQualityType0Extra,
    )


T = TypeVar("T", bound="RecordMappingQualityType0")


@_attrs_define
class RecordMappingQualityType0:
    """
    Attributes:
        accuracy_meters (MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2 | Unset): One mapping
            value: a Jayway JSONPath (item-relative under `select`; `$pairs.<key>` reads a flattened pair), a substitution-
            only template, or a constant (059)
        extra (RecordMappingQualityType0Extra | Unset): Namespaced quality extras (Quality.extra), e.g. `lora.rssi`;
            scalar values only
    """

    accuracy_meters: (
        MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2 | Unset
    ) = UNSET
    extra: RecordMappingQualityType0Extra | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1

        accuracy_meters: dict[str, Any] | Unset
        if isinstance(self.accuracy_meters, Unset):
            accuracy_meters = UNSET
        elif isinstance(self.accuracy_meters, MappingValueSpecType0) or isinstance(
            self.accuracy_meters, MappingValueSpecType1
        ):
            accuracy_meters = self.accuracy_meters.to_dict()
        else:
            accuracy_meters = self.accuracy_meters.to_dict()

        extra: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extra, Unset):
            extra = self.extra.to_dict()

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if accuracy_meters is not UNSET:
            field_dict["accuracyMeters"] = accuracy_meters
        if extra is not UNSET:
            field_dict["extra"] = extra

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
        from ..models.mapping_value_spec_type_2 import MappingValueSpecType2
        from ..models.record_mapping_quality_type_0_extra import (
            RecordMappingQualityType0Extra,
        )

        d = dict(src_dict)

        def _parse_accuracy_meters(
            data: object,
        ) -> (
            MappingValueSpecType0
            | MappingValueSpecType1
            | MappingValueSpecType2
            | Unset
        ):
            if isinstance(data, Unset):
                return data
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

        accuracy_meters = _parse_accuracy_meters(d.pop("accuracyMeters", UNSET))

        _extra = d.pop("extra", UNSET)
        extra: RecordMappingQualityType0Extra | Unset
        if isinstance(_extra, Unset):
            extra = UNSET
        else:
            extra = RecordMappingQualityType0Extra.from_dict(_extra)

        record_mapping_quality_type_0 = cls(
            accuracy_meters=accuracy_meters,
            extra=extra,
        )

        return record_mapping_quality_type_0
