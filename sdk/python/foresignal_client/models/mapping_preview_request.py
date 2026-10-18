from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_sample import MappingSample
    from ..models.record_mapping import RecordMapping


T = TypeVar("T", bound="MappingPreviewRequest")


@_attrs_define
class MappingPreviewRequest:
    """Preview request: a mapping and a sample, optionally checked against an existing custom source (059)

    Attributes:
        mapping (RecordMapping): Declarative mapping from an inbound JSON, form or query-string payload to normalized
            records (059). Data, not script: no expression language. JSONPath is the Jayway dialect (`$` is the document, or
            the current item under `select`; `['a-b']` quotes a member name); deep scan (`..`), function calls and regex
            filters (`=~`) are rejected. Unknown members are rejected.
        sample (MappingSample): A raw inbound sample for preview or suggestion; form and query inputs are converted to a
            flat JSON object of strings before mapping (059)
        source_id (None | str | Unset): Custom source whose kinds, fieldSpecs, policy and observedAtMode are checked
    """

    mapping: RecordMapping
    sample: MappingSample
    source_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mapping = self.mapping.to_dict()

        sample = self.sample.to_dict()

        source_id: None | str | Unset
        if isinstance(self.source_id, Unset):
            source_id = UNSET
        else:
            source_id = self.source_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mapping": mapping,
                "sample": sample,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_sample import MappingSample
        from ..models.record_mapping import RecordMapping

        d = dict(src_dict)
        mapping = RecordMapping.from_dict(d.pop("mapping"))

        sample = MappingSample.from_dict(d.pop("sample"))

        def _parse_source_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_id = _parse_source_id(d.pop("sourceId", UNSET))

        mapping_preview_request = cls(
            mapping=mapping,
            sample=sample,
            source_id=source_id,
        )

        mapping_preview_request.additional_properties = d
        return mapping_preview_request

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
