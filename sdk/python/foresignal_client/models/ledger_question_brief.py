from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..models.ledger_question_brief_status import LedgerQuestionBriefStatus
from ..models.ledger_question_brief_type import LedgerQuestionBriefType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ledger_question_brief_distribution_type_0 import (
        LedgerQuestionBriefDistributionType0,
    )
    from ..models.ledger_question_brief_forecasters_item import (
        LedgerQuestionBriefForecastersItem,
    )
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="LedgerQuestionBrief")


@_attrs_define
class LedgerQuestionBrief:
    """Compact view of a 122 ledger question with its ensemble, issuer and cutoff

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        type_ (LedgerQuestionBriefType):
        status (LedgerQuestionBriefStatus):
        issuer (str): `foresignal-ensemble` or a forecaster id Example: foresignal-ensemble.
        ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        closes_at (datetime.datetime | None | Unset):
        resolve_by (datetime.datetime | None | Unset):
        resolution_criteria (None | str | Unset):
        probability (float | None | Unset):
        estimative_term (IcdTerm | None | Unset):
        distribution (LedgerQuestionBriefDistributionType0 | None | Unset):
        quantiles (None | Quantiles | Unset):
        forecasters (list[LedgerQuestionBriefForecastersItem] | Unset):
        knowledge_cutoff (datetime.date | None | Unset):
        base_rate (float | None | Unset):
        updated_at (datetime.datetime | None | Unset):
    """

    id: str
    title: str
    type_: LedgerQuestionBriefType
    status: LedgerQuestionBriefStatus
    issuer: str
    ref: str | Unset = UNSET
    closes_at: datetime.datetime | None | Unset = UNSET
    resolve_by: datetime.datetime | None | Unset = UNSET
    resolution_criteria: None | str | Unset = UNSET
    probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    distribution: LedgerQuestionBriefDistributionType0 | None | Unset = UNSET
    quantiles: None | Quantiles | Unset = UNSET
    forecasters: list[LedgerQuestionBriefForecastersItem] | Unset = UNSET
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ledger_question_brief_distribution_type_0 import (
            LedgerQuestionBriefDistributionType0,
        )
        from ..models.quantiles import Quantiles

        id = self.id

        title = self.title

        type_ = self.type_.value

        status = self.status.value

        issuer = self.issuer

        ref = self.ref

        closes_at: None | str | Unset
        if isinstance(self.closes_at, Unset):
            closes_at = UNSET
        elif isinstance(self.closes_at, datetime.datetime):
            closes_at = self.closes_at.isoformat()
        else:
            closes_at = self.closes_at

        resolve_by: None | str | Unset
        if isinstance(self.resolve_by, Unset):
            resolve_by = UNSET
        elif isinstance(self.resolve_by, datetime.datetime):
            resolve_by = self.resolve_by.isoformat()
        else:
            resolve_by = self.resolve_by

        resolution_criteria: None | str | Unset
        if isinstance(self.resolution_criteria, Unset):
            resolution_criteria = UNSET
        else:
            resolution_criteria = self.resolution_criteria

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        estimative_term: None | str | Unset
        if isinstance(self.estimative_term, Unset):
            estimative_term = UNSET
        elif isinstance(self.estimative_term, IcdTerm):
            estimative_term = self.estimative_term.value
        else:
            estimative_term = self.estimative_term

        distribution: dict[str, Any] | None | Unset
        if isinstance(self.distribution, Unset):
            distribution = UNSET
        elif isinstance(self.distribution, LedgerQuestionBriefDistributionType0):
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

        forecasters: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.forecasters, Unset):
            forecasters = []
            for forecasters_item_data in self.forecasters:
                forecasters_item = forecasters_item_data.to_dict()
                forecasters.append(forecasters_item)

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "type": type_,
                "status": status,
                "issuer": issuer,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if closes_at is not UNSET:
            field_dict["closesAt"] = closes_at
        if resolve_by is not UNSET:
            field_dict["resolveBy"] = resolve_by
        if resolution_criteria is not UNSET:
            field_dict["resolutionCriteria"] = resolution_criteria
        if probability is not UNSET:
            field_dict["probability"] = probability
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term
        if distribution is not UNSET:
            field_dict["distribution"] = distribution
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles
        if forecasters is not UNSET:
            field_dict["forecasters"] = forecasters
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ledger_question_brief_distribution_type_0 import (
            LedgerQuestionBriefDistributionType0,
        )
        from ..models.ledger_question_brief_forecasters_item import (
            LedgerQuestionBriefForecastersItem,
        )
        from ..models.quantiles import Quantiles

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        type_ = LedgerQuestionBriefType(d.pop("type"))

        status = LedgerQuestionBriefStatus(d.pop("status"))

        issuer = d.pop("issuer")

        ref = d.pop("ref", UNSET)

        def _parse_closes_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                closes_at_type_0 = datetime.datetime.fromisoformat(data)

                return closes_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        closes_at = _parse_closes_at(d.pop("closesAt", UNSET))

        def _parse_resolve_by(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                resolve_by_type_0 = datetime.datetime.fromisoformat(data)

                return resolve_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        resolve_by = _parse_resolve_by(d.pop("resolveBy", UNSET))

        def _parse_resolution_criteria(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolution_criteria = _parse_resolution_criteria(
            d.pop("resolutionCriteria", UNSET)
        )

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

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

        def _parse_distribution(
            data: object,
        ) -> LedgerQuestionBriefDistributionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                distribution_type_0 = LedgerQuestionBriefDistributionType0.from_dict(
                    data
                )

                return distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LedgerQuestionBriefDistributionType0 | None | Unset, data)

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

        _forecasters = d.pop("forecasters", UNSET)
        forecasters: list[LedgerQuestionBriefForecastersItem] | Unset = UNSET
        if _forecasters is not UNSET:
            forecasters = []
            for forecasters_item_data in _forecasters:
                forecasters_item = LedgerQuestionBriefForecastersItem.from_dict(
                    forecasters_item_data
                )

                forecasters.append(forecasters_item)

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

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        ledger_question_brief = cls(
            id=id,
            title=title,
            type_=type_,
            status=status,
            issuer=issuer,
            ref=ref,
            closes_at=closes_at,
            resolve_by=resolve_by,
            resolution_criteria=resolution_criteria,
            probability=probability,
            estimative_term=estimative_term,
            distribution=distribution,
            quantiles=quantiles,
            forecasters=forecasters,
            knowledge_cutoff=knowledge_cutoff,
            base_rate=base_rate,
            updated_at=updated_at,
        )

        ledger_question_brief.additional_properties = d
        return ledger_question_brief

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
