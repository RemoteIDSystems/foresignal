from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_input_type import HypothesisInputType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_input_coincidence_baseline_type_0 import (
        HypothesisInputCoincidenceBaselineType0,
    )
    from ..models.hypothesis_input_prior_basis import HypothesisInputPriorBasis


T = TypeVar("T", bound="HypothesisInput")


@_attrs_define
class HypothesisInput:
    """A hypothesis to create (inside a set or added to one)

    Attributes:
        label (str):
        statement (str):
        type_ (HypothesisInputType):
        prior (float):
        prior_basis (HypothesisInputPriorBasis): Outside-view basis of a prior
        ledger_outcome (None | str | Unset):
        linked_finding_ids (list[str] | Unset):
        coincidence_baseline (HypothesisInputCoincidenceBaselineType0 | None | Unset):
        alternatives (list[str] | Unset): Ids or labels of ≥ 2 other hypotheses in the same set (labels when created
            together with the set)
    """

    label: str
    statement: str
    type_: HypothesisInputType
    prior: float
    prior_basis: HypothesisInputPriorBasis
    ledger_outcome: None | str | Unset = UNSET
    linked_finding_ids: list[str] | Unset = UNSET
    coincidence_baseline: HypothesisInputCoincidenceBaselineType0 | None | Unset = UNSET
    alternatives: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.hypothesis_input_coincidence_baseline_type_0 import (
            HypothesisInputCoincidenceBaselineType0,
        )

        label = self.label

        statement = self.statement

        type_ = self.type_.value

        prior = self.prior

        prior_basis = self.prior_basis.to_dict()

        ledger_outcome: None | str | Unset
        if isinstance(self.ledger_outcome, Unset):
            ledger_outcome = UNSET
        else:
            ledger_outcome = self.ledger_outcome

        linked_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.linked_finding_ids, Unset):
            linked_finding_ids = self.linked_finding_ids

        coincidence_baseline: dict[str, Any] | None | Unset
        if isinstance(self.coincidence_baseline, Unset):
            coincidence_baseline = UNSET
        elif isinstance(
            self.coincidence_baseline, HypothesisInputCoincidenceBaselineType0
        ):
            coincidence_baseline = self.coincidence_baseline.to_dict()
        else:
            coincidence_baseline = self.coincidence_baseline

        alternatives: list[str] | Unset = UNSET
        if not isinstance(self.alternatives, Unset):
            alternatives = self.alternatives

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "label": label,
                "statement": statement,
                "type": type_,
                "prior": prior,
                "priorBasis": prior_basis,
            }
        )
        if ledger_outcome is not UNSET:
            field_dict["ledgerOutcome"] = ledger_outcome
        if linked_finding_ids is not UNSET:
            field_dict["linkedFindingIds"] = linked_finding_ids
        if coincidence_baseline is not UNSET:
            field_dict["coincidenceBaseline"] = coincidence_baseline
        if alternatives is not UNSET:
            field_dict["alternatives"] = alternatives

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_input_coincidence_baseline_type_0 import (
            HypothesisInputCoincidenceBaselineType0,
        )
        from ..models.hypothesis_input_prior_basis import HypothesisInputPriorBasis

        d = dict(src_dict)
        label = d.pop("label")

        statement = d.pop("statement")

        type_ = HypothesisInputType(d.pop("type"))

        prior = d.pop("prior")

        prior_basis = HypothesisInputPriorBasis.from_dict(d.pop("priorBasis"))

        def _parse_ledger_outcome(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ledger_outcome = _parse_ledger_outcome(d.pop("ledgerOutcome", UNSET))

        linked_finding_ids = cast(list[str], d.pop("linkedFindingIds", UNSET))

        def _parse_coincidence_baseline(
            data: object,
        ) -> HypothesisInputCoincidenceBaselineType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coincidence_baseline_type_0 = (
                    HypothesisInputCoincidenceBaselineType0.from_dict(data)
                )

                return coincidence_baseline_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisInputCoincidenceBaselineType0 | None | Unset, data)

        coincidence_baseline = _parse_coincidence_baseline(
            d.pop("coincidenceBaseline", UNSET)
        )

        alternatives = cast(list[str], d.pop("alternatives", UNSET))

        hypothesis_input = cls(
            label=label,
            statement=statement,
            type_=type_,
            prior=prior,
            prior_basis=prior_basis,
            ledger_outcome=ledger_outcome,
            linked_finding_ids=linked_finding_ids,
            coincidence_baseline=coincidence_baseline,
            alternatives=alternatives,
        )

        hypothesis_input.additional_properties = d
        return hypothesis_input

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
