from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AnalysisCallbackQuantiles")


@_attrs_define
class AnalysisCallbackQuantiles:
    """Quantile levels to values for a numeric question. The ledger's required levels are p05, p25, p50, p75, p95; a decile
    provider (p10..p90, as TimesFM emits) is accepted and mapped binding-side onto the required set.

    """

    additional_properties: dict[str, float] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        analysis_callback_quantiles = cls()

        analysis_callback_quantiles.additional_properties = d
        return analysis_callback_quantiles

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> float:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: float) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
