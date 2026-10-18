from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.context_summary_narrative_type_0_text_origin import (
    ContextSummaryNarrativeType0TextOrigin,
)

T = TypeVar("T", bound="ContextSummaryNarrativeType0")


@_attrs_define
class ContextSummaryNarrativeType0:
    """Optional LLM text; omitted when no provider

    Attributes:
        text (str):
        text_origin (ContextSummaryNarrativeType0TextOrigin):
    """

    text: str
    text_origin: ContextSummaryNarrativeType0TextOrigin
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        text = self.text

        text_origin = self.text_origin.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "text": text,
                "textOrigin": text_origin,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        text = d.pop("text")

        text_origin = ContextSummaryNarrativeType0TextOrigin(d.pop("textOrigin"))

        context_summary_narrative_type_0 = cls(
            text=text,
            text_origin=text_origin,
        )

        context_summary_narrative_type_0.additional_properties = d
        return context_summary_narrative_type_0

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
