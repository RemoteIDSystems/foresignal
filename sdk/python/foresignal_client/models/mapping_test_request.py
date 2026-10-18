from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.record_mapping import RecordMapping


T = TypeVar("T", bound="MappingTestRequest")


@_attrs_define
class MappingTestRequest:
    """Optional proposed mapping to test against captured samples; omit to test the stored mapping (059)

    Attributes:
        mapping (None | RecordMapping | Unset):
    """

    mapping: None | RecordMapping | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.record_mapping import RecordMapping

        mapping: dict[str, Any] | None | Unset
        if isinstance(self.mapping, Unset):
            mapping = UNSET
        elif isinstance(self.mapping, RecordMapping):
            mapping = self.mapping.to_dict()
        else:
            mapping = self.mapping

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if mapping is not UNSET:
            field_dict["mapping"] = mapping

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.record_mapping import RecordMapping

        d = dict(src_dict)

        def _parse_mapping(data: object) -> None | RecordMapping | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                mapping_type_0 = RecordMapping.from_dict(data)

                return mapping_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMapping | Unset, data)

        mapping = _parse_mapping(d.pop("mapping", UNSET))

        mapping_test_request = cls(
            mapping=mapping,
        )

        mapping_test_request.additional_properties = d
        return mapping_test_request

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
