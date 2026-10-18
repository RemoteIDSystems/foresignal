from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mapping_test_result_mapping_source import MappingTestResultMappingSource

if TYPE_CHECKING:
    from ..models.mapping_test_result_samples_item import MappingTestResultSamplesItem


T = TypeVar("T", bound="MappingTestResult")


@_attrs_define
class MappingTestResult:
    """Mapping applied to each captured sample (059)

    Attributes:
        mapping_source (MappingTestResultMappingSource):
        samples (list[MappingTestResultSamplesItem]):
    """

    mapping_source: MappingTestResultMappingSource
    samples: list[MappingTestResultSamplesItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mapping_source = self.mapping_source.value

        samples = []
        for samples_item_data in self.samples:
            samples_item = samples_item_data.to_dict()
            samples.append(samples_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mappingSource": mapping_source,
                "samples": samples,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_test_result_samples_item import (
            MappingTestResultSamplesItem,
        )

        d = dict(src_dict)
        mapping_source = MappingTestResultMappingSource(d.pop("mappingSource"))

        samples = []
        _samples = d.pop("samples")
        for samples_item_data in _samples:
            samples_item = MappingTestResultSamplesItem.from_dict(samples_item_data)

            samples.append(samples_item)

        mapping_test_result = cls(
            mapping_source=mapping_source,
            samples=samples,
        )

        mapping_test_result.additional_properties = d
        return mapping_test_result

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
