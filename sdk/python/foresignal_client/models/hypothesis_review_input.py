from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_review_input_cause import HypothesisReviewInputCause
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_review_input_assumptions_item import (
        HypothesisReviewInputAssumptionsItem,
    )
    from ..models.hypothesis_review_input_lr_item import HypothesisReviewInputLrItem
    from ..models.hypothesis_review_input_probabilities_type_0 import (
        HypothesisReviewInputProbabilitiesType0,
    )


T = TypeVar("T", bound="HypothesisReviewInput")


@_attrs_define
class HypothesisReviewInput:
    """A review revision (cause `review` or `operator`)

    Attributes:
        rationale (str):
        cause (HypothesisReviewInputCause | Unset): default `review`
        proposal_id (None | str | Unset):
        probabilities (HypothesisReviewInputProbabilitiesType0 | None | Unset):
        lr (list[HypothesisReviewInputLrItem] | Unset):
        assumptions (list[HypothesisReviewInputAssumptionsItem] | Unset):
        exceed_cap (bool | Unset):  Default: False.
        citations (list[str] | Unset):
        version (int | Unset): Set version (optimistic lock)
    """

    rationale: str
    cause: HypothesisReviewInputCause | Unset = UNSET
    proposal_id: None | str | Unset = UNSET
    probabilities: HypothesisReviewInputProbabilitiesType0 | None | Unset = UNSET
    lr: list[HypothesisReviewInputLrItem] | Unset = UNSET
    assumptions: list[HypothesisReviewInputAssumptionsItem] | Unset = UNSET
    exceed_cap: bool | Unset = False
    citations: list[str] | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.hypothesis_review_input_probabilities_type_0 import (
            HypothesisReviewInputProbabilitiesType0,
        )

        rationale = self.rationale

        cause: str | Unset = UNSET
        if not isinstance(self.cause, Unset):
            cause = self.cause.value

        proposal_id: None | str | Unset
        if isinstance(self.proposal_id, Unset):
            proposal_id = UNSET
        else:
            proposal_id = self.proposal_id

        probabilities: dict[str, Any] | None | Unset
        if isinstance(self.probabilities, Unset):
            probabilities = UNSET
        elif isinstance(self.probabilities, HypothesisReviewInputProbabilitiesType0):
            probabilities = self.probabilities.to_dict()
        else:
            probabilities = self.probabilities

        lr: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.lr, Unset):
            lr = []
            for lr_item_data in self.lr:
                lr_item = lr_item_data.to_dict()
                lr.append(lr_item)

        assumptions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.assumptions, Unset):
            assumptions = []
            for assumptions_item_data in self.assumptions:
                assumptions_item = assumptions_item_data.to_dict()
                assumptions.append(assumptions_item)

        exceed_cap = self.exceed_cap

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rationale": rationale,
            }
        )
        if cause is not UNSET:
            field_dict["cause"] = cause
        if proposal_id is not UNSET:
            field_dict["proposalId"] = proposal_id
        if probabilities is not UNSET:
            field_dict["probabilities"] = probabilities
        if lr is not UNSET:
            field_dict["lr"] = lr
        if assumptions is not UNSET:
            field_dict["assumptions"] = assumptions
        if exceed_cap is not UNSET:
            field_dict["exceedCap"] = exceed_cap
        if citations is not UNSET:
            field_dict["citations"] = citations
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_review_input_assumptions_item import (
            HypothesisReviewInputAssumptionsItem,
        )
        from ..models.hypothesis_review_input_lr_item import HypothesisReviewInputLrItem
        from ..models.hypothesis_review_input_probabilities_type_0 import (
            HypothesisReviewInputProbabilitiesType0,
        )

        d = dict(src_dict)
        rationale = d.pop("rationale")

        _cause = d.pop("cause", UNSET)
        cause: HypothesisReviewInputCause | Unset
        if isinstance(_cause, Unset):
            cause = UNSET
        else:
            cause = HypothesisReviewInputCause(_cause)

        def _parse_proposal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        proposal_id = _parse_proposal_id(d.pop("proposalId", UNSET))

        def _parse_probabilities(
            data: object,
        ) -> HypothesisReviewInputProbabilitiesType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                probabilities_type_0 = (
                    HypothesisReviewInputProbabilitiesType0.from_dict(data)
                )

                return probabilities_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisReviewInputProbabilitiesType0 | None | Unset, data)

        probabilities = _parse_probabilities(d.pop("probabilities", UNSET))

        _lr = d.pop("lr", UNSET)
        lr: list[HypothesisReviewInputLrItem] | Unset = UNSET
        if _lr is not UNSET:
            lr = []
            for lr_item_data in _lr:
                lr_item = HypothesisReviewInputLrItem.from_dict(lr_item_data)

                lr.append(lr_item)

        _assumptions = d.pop("assumptions", UNSET)
        assumptions: list[HypothesisReviewInputAssumptionsItem] | Unset = UNSET
        if _assumptions is not UNSET:
            assumptions = []
            for assumptions_item_data in _assumptions:
                assumptions_item = HypothesisReviewInputAssumptionsItem.from_dict(
                    assumptions_item_data
                )

                assumptions.append(assumptions_item)

        exceed_cap = d.pop("exceedCap", UNSET)

        citations = cast(list[str], d.pop("citations", UNSET))

        version = d.pop("version", UNSET)

        hypothesis_review_input = cls(
            rationale=rationale,
            cause=cause,
            proposal_id=proposal_id,
            probabilities=probabilities,
            lr=lr,
            assumptions=assumptions,
            exceed_cap=exceed_cap,
            citations=citations,
            version=version,
        )

        hypothesis_review_input.additional_properties = d
        return hypothesis_review_input

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
