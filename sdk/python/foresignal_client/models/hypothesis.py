from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.hypothesis_status import HypothesisStatus
from ..models.hypothesis_type import HypothesisType
from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.coverage_stats import CoverageStats
    from ..models.hypothesis_cap_override_type_0 import HypothesisCapOverrideType0
    from ..models.hypothesis_change_type_0 import HypothesisChangeType0
    from ..models.hypothesis_coincidence_baseline_type_0 import (
        HypothesisCoincidenceBaselineType0,
    )
    from ..models.hypothesis_confidence_basis import HypothesisConfidenceBasis
    from ..models.hypothesis_prior_basis import HypothesisPriorBasis


T = TypeVar("T", bound="Hypothesis")


@_attrs_define
class Hypothesis:
    """One competing answer to a set's key question

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        label (str):
        statement (str):
        type_ (HypothesisType):
        prior (float):
        prior_basis (HypothesisPriorBasis): Outside-view basis of a prior
        probability (float):
        estimative_term (IcdTerm): ICD 203 likelihood term, derived from the numeric probability
        confidence (ConfidenceLevel): Analytic confidence in the evidence (separate from probability)
        status (HypothesisStatus):
        confidence_basis (HypothesisConfidenceBasis | Unset): Inputs to the rule-based confidence (`ConfidenceAssessor`
            1.2.0)
        ledger_outcome (None | str | Unset): Outcome of the linked question this hypothesis maps to
        change (HypothesisChangeType0 | None | Unset):
        linked_finding_ids (list[str] | Unset): Speculative links: ≥ 1 linked 119 findings
        coincidence_baseline (HypothesisCoincidenceBaselineType0 | None | Unset):
        alternatives (list[str] | Unset): Speculative links: ≥ 2 other hypotheses in the same set
        probability_cap (float | None | Unset): Speculative links: computed cap the updater never exceeds
        cap_override (HypothesisCapOverrideType0 | None | Unset):
        version (int | Unset):
        news_coverage (CoverageStats | None | Unset): Lean and state-affiliation balance of the hypothesis's supporting
            news evidence; SKEWED_COVERAGE caveat when skewed
    """

    id: str
    set_id: str
    label: str
    statement: str
    type_: HypothesisType
    prior: float
    prior_basis: HypothesisPriorBasis
    probability: float
    estimative_term: IcdTerm
    confidence: ConfidenceLevel
    status: HypothesisStatus
    confidence_basis: HypothesisConfidenceBasis | Unset = UNSET
    ledger_outcome: None | str | Unset = UNSET
    change: HypothesisChangeType0 | None | Unset = UNSET
    linked_finding_ids: list[str] | Unset = UNSET
    coincidence_baseline: HypothesisCoincidenceBaselineType0 | None | Unset = UNSET
    alternatives: list[str] | Unset = UNSET
    probability_cap: float | None | Unset = UNSET
    cap_override: HypothesisCapOverrideType0 | None | Unset = UNSET
    version: int | Unset = UNSET
    news_coverage: CoverageStats | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.coverage_stats import CoverageStats
        from ..models.hypothesis_cap_override_type_0 import HypothesisCapOverrideType0
        from ..models.hypothesis_change_type_0 import HypothesisChangeType0
        from ..models.hypothesis_coincidence_baseline_type_0 import (
            HypothesisCoincidenceBaselineType0,
        )

        id = self.id

        set_id = self.set_id

        label = self.label

        statement = self.statement

        type_ = self.type_.value

        prior = self.prior

        prior_basis = self.prior_basis.to_dict()

        probability = self.probability

        estimative_term = self.estimative_term.value

        confidence = self.confidence.value

        status = self.status.value

        confidence_basis: dict[str, Any] | Unset = UNSET
        if not isinstance(self.confidence_basis, Unset):
            confidence_basis = self.confidence_basis.to_dict()

        ledger_outcome: None | str | Unset
        if isinstance(self.ledger_outcome, Unset):
            ledger_outcome = UNSET
        else:
            ledger_outcome = self.ledger_outcome

        change: dict[str, Any] | None | Unset
        if isinstance(self.change, Unset):
            change = UNSET
        elif isinstance(self.change, HypothesisChangeType0):
            change = self.change.to_dict()
        else:
            change = self.change

        linked_finding_ids: list[str] | Unset = UNSET
        if not isinstance(self.linked_finding_ids, Unset):
            linked_finding_ids = self.linked_finding_ids

        coincidence_baseline: dict[str, Any] | None | Unset
        if isinstance(self.coincidence_baseline, Unset):
            coincidence_baseline = UNSET
        elif isinstance(self.coincidence_baseline, HypothesisCoincidenceBaselineType0):
            coincidence_baseline = self.coincidence_baseline.to_dict()
        else:
            coincidence_baseline = self.coincidence_baseline

        alternatives: list[str] | Unset = UNSET
        if not isinstance(self.alternatives, Unset):
            alternatives = self.alternatives

        probability_cap: float | None | Unset
        if isinstance(self.probability_cap, Unset):
            probability_cap = UNSET
        else:
            probability_cap = self.probability_cap

        cap_override: dict[str, Any] | None | Unset
        if isinstance(self.cap_override, Unset):
            cap_override = UNSET
        elif isinstance(self.cap_override, HypothesisCapOverrideType0):
            cap_override = self.cap_override.to_dict()
        else:
            cap_override = self.cap_override

        version = self.version

        news_coverage: dict[str, Any] | None | Unset
        if isinstance(self.news_coverage, Unset):
            news_coverage = UNSET
        elif isinstance(self.news_coverage, CoverageStats):
            news_coverage = self.news_coverage.to_dict()
        else:
            news_coverage = self.news_coverage

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "label": label,
                "statement": statement,
                "type": type_,
                "prior": prior,
                "priorBasis": prior_basis,
                "probability": probability,
                "estimativeTerm": estimative_term,
                "confidence": confidence,
                "status": status,
            }
        )
        if confidence_basis is not UNSET:
            field_dict["confidenceBasis"] = confidence_basis
        if ledger_outcome is not UNSET:
            field_dict["ledgerOutcome"] = ledger_outcome
        if change is not UNSET:
            field_dict["change"] = change
        if linked_finding_ids is not UNSET:
            field_dict["linkedFindingIds"] = linked_finding_ids
        if coincidence_baseline is not UNSET:
            field_dict["coincidenceBaseline"] = coincidence_baseline
        if alternatives is not UNSET:
            field_dict["alternatives"] = alternatives
        if probability_cap is not UNSET:
            field_dict["probabilityCap"] = probability_cap
        if cap_override is not UNSET:
            field_dict["capOverride"] = cap_override
        if version is not UNSET:
            field_dict["version"] = version
        if news_coverage is not UNSET:
            field_dict["newsCoverage"] = news_coverage

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.coverage_stats import CoverageStats
        from ..models.hypothesis_cap_override_type_0 import HypothesisCapOverrideType0
        from ..models.hypothesis_change_type_0 import HypothesisChangeType0
        from ..models.hypothesis_coincidence_baseline_type_0 import (
            HypothesisCoincidenceBaselineType0,
        )
        from ..models.hypothesis_confidence_basis import HypothesisConfidenceBasis
        from ..models.hypothesis_prior_basis import HypothesisPriorBasis

        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        label = d.pop("label")

        statement = d.pop("statement")

        type_ = HypothesisType(d.pop("type"))

        prior = d.pop("prior")

        prior_basis = HypothesisPriorBasis.from_dict(d.pop("priorBasis"))

        probability = d.pop("probability")

        estimative_term = IcdTerm(d.pop("estimativeTerm"))

        confidence = ConfidenceLevel(d.pop("confidence"))

        status = HypothesisStatus(d.pop("status"))

        _confidence_basis = d.pop("confidenceBasis", UNSET)
        confidence_basis: HypothesisConfidenceBasis | Unset
        if isinstance(_confidence_basis, Unset):
            confidence_basis = UNSET
        else:
            confidence_basis = HypothesisConfidenceBasis.from_dict(_confidence_basis)

        def _parse_ledger_outcome(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ledger_outcome = _parse_ledger_outcome(d.pop("ledgerOutcome", UNSET))

        def _parse_change(data: object) -> HypothesisChangeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                change_type_0 = HypothesisChangeType0.from_dict(data)

                return change_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisChangeType0 | None | Unset, data)

        change = _parse_change(d.pop("change", UNSET))

        linked_finding_ids = cast(list[str], d.pop("linkedFindingIds", UNSET))

        def _parse_coincidence_baseline(
            data: object,
        ) -> HypothesisCoincidenceBaselineType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                coincidence_baseline_type_0 = (
                    HypothesisCoincidenceBaselineType0.from_dict(data)
                )

                return coincidence_baseline_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisCoincidenceBaselineType0 | None | Unset, data)

        coincidence_baseline = _parse_coincidence_baseline(
            d.pop("coincidenceBaseline", UNSET)
        )

        alternatives = cast(list[str], d.pop("alternatives", UNSET))

        def _parse_probability_cap(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability_cap = _parse_probability_cap(d.pop("probabilityCap", UNSET))

        def _parse_cap_override(
            data: object,
        ) -> HypothesisCapOverrideType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                cap_override_type_0 = HypothesisCapOverrideType0.from_dict(data)

                return cap_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisCapOverrideType0 | None | Unset, data)

        cap_override = _parse_cap_override(d.pop("capOverride", UNSET))

        version = d.pop("version", UNSET)

        def _parse_news_coverage(data: object) -> CoverageStats | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                news_coverage_type_0 = CoverageStats.from_dict(data)

                return news_coverage_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CoverageStats | None | Unset, data)

        news_coverage = _parse_news_coverage(d.pop("newsCoverage", UNSET))

        hypothesis = cls(
            id=id,
            set_id=set_id,
            label=label,
            statement=statement,
            type_=type_,
            prior=prior,
            prior_basis=prior_basis,
            probability=probability,
            estimative_term=estimative_term,
            confidence=confidence,
            status=status,
            confidence_basis=confidence_basis,
            ledger_outcome=ledger_outcome,
            change=change,
            linked_finding_ids=linked_finding_ids,
            coincidence_baseline=coincidence_baseline,
            alternatives=alternatives,
            probability_cap=probability_cap,
            cap_override=cap_override,
            version=version,
            news_coverage=news_coverage,
        )

        hypothesis.additional_properties = d
        return hypothesis

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
