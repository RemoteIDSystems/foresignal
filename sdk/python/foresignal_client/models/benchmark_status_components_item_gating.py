from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_status_components_item_gating_llm_ensemble_weight import (
    BenchmarkStatusComponentsItemGatingLlmEnsembleWeight,
)
from ..models.benchmark_status_components_item_gating_proposer_auto_execution import (
    BenchmarkStatusComponentsItemGatingProposerAutoExecution,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkStatusComponentsItemGating")


@_attrs_define
class BenchmarkStatusComponentsItemGating:
    """Effect on autonomous features

    Attributes:
        proposer_auto_execution (BenchmarkStatusComponentsItemGatingProposerAutoExecution | Unset):
        llm_ensemble_weight (BenchmarkStatusComponentsItemGatingLlmEnsembleWeight | Unset):
        finding_caveat (None | str | Unset): `BENCHMARK_FAIL` while failing
    """

    proposer_auto_execution: (
        BenchmarkStatusComponentsItemGatingProposerAutoExecution | Unset
    ) = UNSET
    llm_ensemble_weight: (
        BenchmarkStatusComponentsItemGatingLlmEnsembleWeight | Unset
    ) = UNSET
    finding_caveat: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        proposer_auto_execution: str | Unset = UNSET
        if not isinstance(self.proposer_auto_execution, Unset):
            proposer_auto_execution = self.proposer_auto_execution.value

        llm_ensemble_weight: str | Unset = UNSET
        if not isinstance(self.llm_ensemble_weight, Unset):
            llm_ensemble_weight = self.llm_ensemble_weight.value

        finding_caveat: None | str | Unset
        if isinstance(self.finding_caveat, Unset):
            finding_caveat = UNSET
        else:
            finding_caveat = self.finding_caveat

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if proposer_auto_execution is not UNSET:
            field_dict["proposerAutoExecution"] = proposer_auto_execution
        if llm_ensemble_weight is not UNSET:
            field_dict["llmEnsembleWeight"] = llm_ensemble_weight
        if finding_caveat is not UNSET:
            field_dict["findingCaveat"] = finding_caveat

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _proposer_auto_execution = d.pop("proposerAutoExecution", UNSET)
        proposer_auto_execution: (
            BenchmarkStatusComponentsItemGatingProposerAutoExecution | Unset
        )
        if isinstance(_proposer_auto_execution, Unset):
            proposer_auto_execution = UNSET
        else:
            proposer_auto_execution = (
                BenchmarkStatusComponentsItemGatingProposerAutoExecution(
                    _proposer_auto_execution
                )
            )

        _llm_ensemble_weight = d.pop("llmEnsembleWeight", UNSET)
        llm_ensemble_weight: (
            BenchmarkStatusComponentsItemGatingLlmEnsembleWeight | Unset
        )
        if isinstance(_llm_ensemble_weight, Unset):
            llm_ensemble_weight = UNSET
        else:
            llm_ensemble_weight = BenchmarkStatusComponentsItemGatingLlmEnsembleWeight(
                _llm_ensemble_weight
            )

        def _parse_finding_caveat(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        finding_caveat = _parse_finding_caveat(d.pop("findingCaveat", UNSET))

        benchmark_status_components_item_gating = cls(
            proposer_auto_execution=proposer_auto_execution,
            llm_ensemble_weight=llm_ensemble_weight,
            finding_caveat=finding_caveat,
        )

        benchmark_status_components_item_gating.additional_properties = d
        return benchmark_status_components_item_gating

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
