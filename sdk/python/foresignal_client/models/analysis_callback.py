from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_callback_quantiles import AnalysisCallbackQuantiles


T = TypeVar("T", bound="AnalysisCallback")


@_attrs_define
class AnalysisCallback:
    """A provider's asynchronous answer to an on-demand analysis request (155).

    Attributes:
        correlation_id (str): The correlation id from the original request; the credential for this callback.
        probability (float | Unset): The forecast probability for a binary question.
        quantiles (AnalysisCallbackQuantiles | Unset): Quantile levels to values for a numeric question. The ledger's
            required levels are p05, p25, p50, p75, p95; a decile provider (p10..p90, as TimesFM emits) is accepted and
            mapped binding-side onto the required set.
    """

    correlation_id: str
    probability: float | Unset = UNSET
    quantiles: AnalysisCallbackQuantiles | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        correlation_id = self.correlation_id

        probability = self.probability

        quantiles: dict[str, Any] | Unset = UNSET
        if not isinstance(self.quantiles, Unset):
            quantiles = self.quantiles.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "correlationId": correlation_id,
            }
        )
        if probability is not UNSET:
            field_dict["probability"] = probability
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_callback_quantiles import AnalysisCallbackQuantiles

        d = dict(src_dict)
        correlation_id = d.pop("correlationId")

        probability = d.pop("probability", UNSET)

        _quantiles = d.pop("quantiles", UNSET)
        quantiles: AnalysisCallbackQuantiles | Unset
        if isinstance(_quantiles, Unset):
            quantiles = UNSET
        else:
            quantiles = AnalysisCallbackQuantiles.from_dict(_quantiles)

        analysis_callback = cls(
            correlation_id=correlation_id,
            probability=probability,
            quantiles=quantiles,
        )

        analysis_callback.additional_properties = d
        return analysis_callback

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
