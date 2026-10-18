from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmTestResultEmbeddingType0")


@_attrs_define
class LlmTestResultEmbeddingType0:
    """
    Attributes:
        model (str | Unset):
        dimensions (int | Unset):
        latency_ms (int | Unset):
    """

    model: str | Unset = UNSET
    dimensions: int | Unset = UNSET
    latency_ms: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        model = self.model

        dimensions = self.dimensions

        latency_ms = self.latency_ms

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if model is not UNSET:
            field_dict["model"] = model
        if dimensions is not UNSET:
            field_dict["dimensions"] = dimensions
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        model = d.pop("model", UNSET)

        dimensions = d.pop("dimensions", UNSET)

        latency_ms = d.pop("latencyMs", UNSET)

        llm_test_result_embedding_type_0 = cls(
            model=model,
            dimensions=dimensions,
            latency_ms=latency_ms,
        )

        llm_test_result_embedding_type_0.additional_properties = d
        return llm_test_result_embedding_type_0

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
