from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_preview_result import MappingPreviewResult
    from ..models.mapping_warning import MappingWarning
    from ..models.record_mapping import RecordMapping


T = TypeVar("T", bound="MappingSuggestResult")


@_attrs_define
class MappingSuggestResult:
    """LLM-drafted mapping, validated and previewed; never stored or applied (059)

    Attributes:
        draft (RecordMapping): Declarative mapping from an inbound JSON, form or query-string payload to normalized
            records (059). Data, not script: no expression language. JSONPath is the Jayway dialect (`$` is the document, or
            the current item under `select`; `['a-b']` quotes a member name); deep scan (`..`), function calls and regex
            filters (`=~`) are rejected. Unknown members are rejected.
        explanation (str):
        warnings (list[MappingWarning]):
        provider (str):
        model (str):
        preview (MappingPreviewResult | Unset): What ingest would write for a sample; nothing is persisted (059)
        repair_rounds (int | Unset):
    """

    draft: RecordMapping
    explanation: str
    warnings: list[MappingWarning]
    provider: str
    model: str
    preview: MappingPreviewResult | Unset = UNSET
    repair_rounds: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        draft = self.draft.to_dict()

        explanation = self.explanation

        warnings = []
        for warnings_item_data in self.warnings:
            warnings_item = warnings_item_data.to_dict()
            warnings.append(warnings_item)

        provider = self.provider

        model = self.model

        preview: dict[str, Any] | Unset = UNSET
        if not isinstance(self.preview, Unset):
            preview = self.preview.to_dict()

        repair_rounds = self.repair_rounds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "draft": draft,
                "explanation": explanation,
                "warnings": warnings,
                "provider": provider,
                "model": model,
            }
        )
        if preview is not UNSET:
            field_dict["preview"] = preview
        if repair_rounds is not UNSET:
            field_dict["repairRounds"] = repair_rounds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_preview_result import MappingPreviewResult
        from ..models.mapping_warning import MappingWarning
        from ..models.record_mapping import RecordMapping

        d = dict(src_dict)
        draft = RecordMapping.from_dict(d.pop("draft"))

        explanation = d.pop("explanation")

        warnings = []
        _warnings = d.pop("warnings")
        for warnings_item_data in _warnings:
            warnings_item = MappingWarning.from_dict(warnings_item_data)

            warnings.append(warnings_item)

        provider = d.pop("provider")

        model = d.pop("model")

        _preview = d.pop("preview", UNSET)
        preview: MappingPreviewResult | Unset
        if isinstance(_preview, Unset):
            preview = UNSET
        else:
            preview = MappingPreviewResult.from_dict(_preview)

        repair_rounds = d.pop("repairRounds", UNSET)

        mapping_suggest_result = cls(
            draft=draft,
            explanation=explanation,
            warnings=warnings,
            provider=provider,
            model=model,
            preview=preview,
            repair_rounds=repair_rounds,
        )

        mapping_suggest_result.additional_properties = d
        return mapping_suggest_result

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
