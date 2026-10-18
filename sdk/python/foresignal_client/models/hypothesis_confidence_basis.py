from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisConfidenceBasis")


@_attrs_define
class HypothesisConfidenceBasis:
    """Inputs to the rule-based confidence (`ConfidenceAssessor` 1.2.0)

    Attributes:
        observed_indicators (int | Unset):
        families (int | Unset):
        evidence_quality (None | str | Unset):
        source_reliability (None | str | Unset): Worst 109 reliability grade among sources
        coverage (None | str | Unset):
        reporting_density (None | str | Unset): `ok` or `LOW_REPORTING_DENSITY`
        forecaster_agreement (float | None | Unset):
        broken_linchpin (bool | Unset):
        rule (None | str | Unset): The rule that set the level
        news_coverage (None | str | Unset): `ok` or `SKEWED_COVERAGE` (133)
        circular_reporting (None | str | Unset): `ok` or `CIRCULAR_REPORTING` (134): support of one origin counted once
        circular_supports (int | None | Unset): Live ACH rows of the set marked CIRCULAR_REPORTING (134)
    """

    observed_indicators: int | Unset = UNSET
    families: int | Unset = UNSET
    evidence_quality: None | str | Unset = UNSET
    source_reliability: None | str | Unset = UNSET
    coverage: None | str | Unset = UNSET
    reporting_density: None | str | Unset = UNSET
    forecaster_agreement: float | None | Unset = UNSET
    broken_linchpin: bool | Unset = UNSET
    rule: None | str | Unset = UNSET
    news_coverage: None | str | Unset = UNSET
    circular_reporting: None | str | Unset = UNSET
    circular_supports: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        observed_indicators = self.observed_indicators

        families = self.families

        evidence_quality: None | str | Unset
        if isinstance(self.evidence_quality, Unset):
            evidence_quality = UNSET
        else:
            evidence_quality = self.evidence_quality

        source_reliability: None | str | Unset
        if isinstance(self.source_reliability, Unset):
            source_reliability = UNSET
        else:
            source_reliability = self.source_reliability

        coverage: None | str | Unset
        if isinstance(self.coverage, Unset):
            coverage = UNSET
        else:
            coverage = self.coverage

        reporting_density: None | str | Unset
        if isinstance(self.reporting_density, Unset):
            reporting_density = UNSET
        else:
            reporting_density = self.reporting_density

        forecaster_agreement: float | None | Unset
        if isinstance(self.forecaster_agreement, Unset):
            forecaster_agreement = UNSET
        else:
            forecaster_agreement = self.forecaster_agreement

        broken_linchpin = self.broken_linchpin

        rule: None | str | Unset
        if isinstance(self.rule, Unset):
            rule = UNSET
        else:
            rule = self.rule

        news_coverage: None | str | Unset
        if isinstance(self.news_coverage, Unset):
            news_coverage = UNSET
        else:
            news_coverage = self.news_coverage

        circular_reporting: None | str | Unset
        if isinstance(self.circular_reporting, Unset):
            circular_reporting = UNSET
        else:
            circular_reporting = self.circular_reporting

        circular_supports: int | None | Unset
        if isinstance(self.circular_supports, Unset):
            circular_supports = UNSET
        else:
            circular_supports = self.circular_supports

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if observed_indicators is not UNSET:
            field_dict["observedIndicators"] = observed_indicators
        if families is not UNSET:
            field_dict["families"] = families
        if evidence_quality is not UNSET:
            field_dict["evidenceQuality"] = evidence_quality
        if source_reliability is not UNSET:
            field_dict["sourceReliability"] = source_reliability
        if coverage is not UNSET:
            field_dict["coverage"] = coverage
        if reporting_density is not UNSET:
            field_dict["reportingDensity"] = reporting_density
        if forecaster_agreement is not UNSET:
            field_dict["forecasterAgreement"] = forecaster_agreement
        if broken_linchpin is not UNSET:
            field_dict["brokenLinchpin"] = broken_linchpin
        if rule is not UNSET:
            field_dict["rule"] = rule
        if news_coverage is not UNSET:
            field_dict["newsCoverage"] = news_coverage
        if circular_reporting is not UNSET:
            field_dict["circularReporting"] = circular_reporting
        if circular_supports is not UNSET:
            field_dict["circularSupports"] = circular_supports

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        observed_indicators = d.pop("observedIndicators", UNSET)

        families = d.pop("families", UNSET)

        def _parse_evidence_quality(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_quality = _parse_evidence_quality(d.pop("evidenceQuality", UNSET))

        def _parse_source_reliability(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source_reliability = _parse_source_reliability(
            d.pop("sourceReliability", UNSET)
        )

        def _parse_coverage(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        coverage = _parse_coverage(d.pop("coverage", UNSET))

        def _parse_reporting_density(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reporting_density = _parse_reporting_density(d.pop("reportingDensity", UNSET))

        def _parse_forecaster_agreement(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        forecaster_agreement = _parse_forecaster_agreement(
            d.pop("forecasterAgreement", UNSET)
        )

        broken_linchpin = d.pop("brokenLinchpin", UNSET)

        def _parse_rule(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rule = _parse_rule(d.pop("rule", UNSET))

        def _parse_news_coverage(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        news_coverage = _parse_news_coverage(d.pop("newsCoverage", UNSET))

        def _parse_circular_reporting(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        circular_reporting = _parse_circular_reporting(
            d.pop("circularReporting", UNSET)
        )

        def _parse_circular_supports(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        circular_supports = _parse_circular_supports(d.pop("circularSupports", UNSET))

        hypothesis_confidence_basis = cls(
            observed_indicators=observed_indicators,
            families=families,
            evidence_quality=evidence_quality,
            source_reliability=source_reliability,
            coverage=coverage,
            reporting_density=reporting_density,
            forecaster_agreement=forecaster_agreement,
            broken_linchpin=broken_linchpin,
            rule=rule,
            news_coverage=news_coverage,
            circular_reporting=circular_reporting,
            circular_supports=circular_supports,
        )

        hypothesis_confidence_basis.additional_properties = d
        return hypothesis_confidence_basis

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
