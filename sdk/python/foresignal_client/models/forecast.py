from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_citations_item import ForecastCitationsItem
    from ..models.forecast_claims_item import ForecastClaimsItem
    from ..models.forecast_distribution_type_0 import ForecastDistributionType0
    from ..models.forecast_inputs import ForecastInputs
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="Forecast")


@_attrs_define
class Forecast:
    """An immutable, timestamped forecast; a new one supersedes it

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster_id (str):
        forecaster_type (str): Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
        made_at (datetime.datetime): Server-set
        as_of_seq (int): Server-set knowledge cutoff in seq
        recalibrated (bool): True for Platt-recalibrated ensemble inputs
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        knowledge_cutoff (datetime.date | None | Unset): Model knowledge cutoff for LLM forecasters
        probability (float | None | Unset): Binary questions
        distribution (ForecastDistributionType0 | None | Unset): Multiple/ordered: outcome label → probability, summing
            to 1 ± 1e-6
        quantiles (None | Quantiles | Unset): Numeric: p05, p10, p25, p50, p75, p90, p95, monotone
        estimative_term (IcdTerm | None | Unset):
        rationale (None | str | Unset): Markdown
        claims (list[ForecastClaimsItem] | Unset): Typed claims (LLM forecasters)
        citations (list[ForecastCitationsItem] | Unset):
        inputs (ForecastInputs | Unset): Inputs: `baseRateIds`, `analogIds`, `precursorId`, `model`, `provider`,
            `effort`, `samples`, `promptHash`, `rawProbability` (before LLM caps), `weights` (ensemble)
        supersedes_id (None | str | Unset):
    """

    id: str
    question_id: str
    forecaster_id: str
    forecaster_type: str
    made_at: datetime.datetime
    as_of_seq: int
    recalibrated: bool
    manifest_id: str
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    probability: float | None | Unset = UNSET
    distribution: ForecastDistributionType0 | None | Unset = UNSET
    quantiles: None | Quantiles | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    rationale: None | str | Unset = UNSET
    claims: list[ForecastClaimsItem] | Unset = UNSET
    citations: list[ForecastCitationsItem] | Unset = UNSET
    inputs: ForecastInputs | Unset = UNSET
    supersedes_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_distribution_type_0 import ForecastDistributionType0
        from ..models.quantiles import Quantiles

        id = self.id

        question_id = self.question_id

        forecaster_id = self.forecaster_id

        forecaster_type = self.forecaster_type

        made_at = self.made_at.isoformat()

        as_of_seq = self.as_of_seq

        recalibrated = self.recalibrated

        manifest_id = self.manifest_id

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        distribution: dict[str, Any] | None | Unset
        if isinstance(self.distribution, Unset):
            distribution = UNSET
        elif isinstance(self.distribution, ForecastDistributionType0):
            distribution = self.distribution.to_dict()
        else:
            distribution = self.distribution

        quantiles: dict[str, Any] | None | Unset
        if isinstance(self.quantiles, Unset):
            quantiles = UNSET
        elif isinstance(self.quantiles, Quantiles):
            quantiles = self.quantiles.to_dict()
        else:
            quantiles = self.quantiles

        estimative_term: None | str | Unset
        if isinstance(self.estimative_term, Unset):
            estimative_term = UNSET
        elif isinstance(self.estimative_term, IcdTerm):
            estimative_term = self.estimative_term.value
        else:
            estimative_term = self.estimative_term

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        claims: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.claims, Unset):
            claims = []
            for claims_item_data in self.claims:
                claims_item = claims_item_data.to_dict()
                claims.append(claims_item)

        citations: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = []
            for citations_item_data in self.citations:
                citations_item = citations_item_data.to_dict()
                citations.append(citations_item)

        inputs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.inputs, Unset):
            inputs = self.inputs.to_dict()

        supersedes_id: None | str | Unset
        if isinstance(self.supersedes_id, Unset):
            supersedes_id = UNSET
        else:
            supersedes_id = self.supersedes_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "questionId": question_id,
                "forecasterId": forecaster_id,
                "forecasterType": forecaster_type,
                "madeAt": made_at,
                "asOfSeq": as_of_seq,
                "recalibrated": recalibrated,
                "manifestId": manifest_id,
            }
        )
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff
        if probability is not UNSET:
            field_dict["probability"] = probability
        if distribution is not UNSET:
            field_dict["distribution"] = distribution
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term
        if rationale is not UNSET:
            field_dict["rationale"] = rationale
        if claims is not UNSET:
            field_dict["claims"] = claims
        if citations is not UNSET:
            field_dict["citations"] = citations
        if inputs is not UNSET:
            field_dict["inputs"] = inputs
        if supersedes_id is not UNSET:
            field_dict["supersedesId"] = supersedes_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_citations_item import ForecastCitationsItem
        from ..models.forecast_claims_item import ForecastClaimsItem
        from ..models.forecast_distribution_type_0 import ForecastDistributionType0
        from ..models.forecast_inputs import ForecastInputs
        from ..models.quantiles import Quantiles

        d = dict(src_dict)
        id = d.pop("id")

        question_id = d.pop("questionId")

        forecaster_id = d.pop("forecasterId")

        forecaster_type = d.pop("forecasterType")

        made_at = datetime.datetime.fromisoformat(d.pop("madeAt"))

        as_of_seq = d.pop("asOfSeq")

        recalibrated = d.pop("recalibrated")

        manifest_id = d.pop("manifestId")

        def _parse_knowledge_cutoff(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                knowledge_cutoff_type_0 = datetime.date.fromisoformat(data)

                return knowledge_cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        knowledge_cutoff = _parse_knowledge_cutoff(d.pop("knowledgeCutoff", UNSET))

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_distribution(
            data: object,
        ) -> ForecastDistributionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                distribution_type_0 = ForecastDistributionType0.from_dict(data)

                return distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastDistributionType0 | None | Unset, data)

        distribution = _parse_distribution(d.pop("distribution", UNSET))

        def _parse_quantiles(data: object) -> None | Quantiles | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quantiles_type_0 = Quantiles.from_dict(data)

                return quantiles_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quantiles | Unset, data)

        quantiles = _parse_quantiles(d.pop("quantiles", UNSET))

        def _parse_estimative_term(data: object) -> IcdTerm | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                estimative_term_type_0 = IcdTerm(data)

                return estimative_term_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IcdTerm | None | Unset, data)

        estimative_term = _parse_estimative_term(d.pop("estimativeTerm", UNSET))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        _claims = d.pop("claims", UNSET)
        claims: list[ForecastClaimsItem] | Unset = UNSET
        if _claims is not UNSET:
            claims = []
            for claims_item_data in _claims:
                claims_item = ForecastClaimsItem.from_dict(claims_item_data)

                claims.append(claims_item)

        _citations = d.pop("citations", UNSET)
        citations: list[ForecastCitationsItem] | Unset = UNSET
        if _citations is not UNSET:
            citations = []
            for citations_item_data in _citations:
                citations_item = ForecastCitationsItem.from_dict(citations_item_data)

                citations.append(citations_item)

        _inputs = d.pop("inputs", UNSET)
        inputs: ForecastInputs | Unset
        if isinstance(_inputs, Unset):
            inputs = UNSET
        else:
            inputs = ForecastInputs.from_dict(_inputs)

        def _parse_supersedes_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        supersedes_id = _parse_supersedes_id(d.pop("supersedesId", UNSET))

        forecast = cls(
            id=id,
            question_id=question_id,
            forecaster_id=forecaster_id,
            forecaster_type=forecaster_type,
            made_at=made_at,
            as_of_seq=as_of_seq,
            recalibrated=recalibrated,
            manifest_id=manifest_id,
            knowledge_cutoff=knowledge_cutoff,
            probability=probability,
            distribution=distribution,
            quantiles=quantiles,
            estimative_term=estimative_term,
            rationale=rationale,
            claims=claims,
            citations=citations,
            inputs=inputs,
            supersedes_id=supersedes_id,
        )

        forecast.additional_properties = d
        return forecast

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
