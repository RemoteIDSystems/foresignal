from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.context_summary_insights_item_kind import ContextSummaryInsightsItemKind
from ..models.context_summary_insights_item_text_origin import (
    ContextSummaryInsightsItemTextOrigin,
)
from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="ContextSummaryInsightsItem")


@_attrs_define
class ContextSummaryInsightsItem:
    """Finding, forecast or band change intersecting the viewport

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (ContextSummaryInsightsItemKind):
        title (str):
        probability (float | None | Unset):
        estimative_term (IcdTerm | None | Unset):
        issuer (None | str | Unset): `official`, `foresignal-ensemble` or a forecaster id
        base_rate (float | None | Unset):
        knowledge_cutoff (datetime.date | None | Unset):
        at (datetime.datetime | None | Unset):
        text_origin (ContextSummaryInsightsItemTextOrigin | Unset):
    """

    ref: str
    kind: ContextSummaryInsightsItemKind
    title: str
    probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    issuer: None | str | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    at: datetime.datetime | None | Unset = UNSET
    text_origin: ContextSummaryInsightsItemTextOrigin | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        kind = self.kind.value

        title = self.title

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

        issuer: None | str | Unset
        if isinstance(self.issuer, Unset):
            issuer = UNSET
        else:
            issuer = self.issuer

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        at: None | str | Unset
        if isinstance(self.at, Unset):
            at = UNSET
        elif isinstance(self.at, datetime.datetime):
            at = self.at.isoformat()
        else:
            at = self.at

        text_origin: str | Unset = UNSET
        if not isinstance(self.text_origin, Unset):
            text_origin = self.text_origin.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "kind": kind,
                "title": title,
            }
        )
        if probability is not UNSET:
            field_dict["probability"] = probability
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term
        if issuer is not UNSET:
            field_dict["issuer"] = issuer
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff
        if at is not UNSET:
            field_dict["at"] = at
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        kind = ContextSummaryInsightsItemKind(d.pop("kind"))

        title = d.pop("title")

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

        def _parse_issuer(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        issuer = _parse_issuer(d.pop("issuer", UNSET))

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

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

        def _parse_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                at_type_0 = datetime.datetime.fromisoformat(data)

                return at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        at = _parse_at(d.pop("at", UNSET))

        _text_origin = d.pop("textOrigin", UNSET)
        text_origin: ContextSummaryInsightsItemTextOrigin | Unset
        if isinstance(_text_origin, Unset):
            text_origin = UNSET
        else:
            text_origin = ContextSummaryInsightsItemTextOrigin(_text_origin)

        context_summary_insights_item = cls(
            ref=ref,
            kind=kind,
            title=title,
            probability=probability,
            estimative_term=estimative_term,
            issuer=issuer,
            base_rate=base_rate,
            knowledge_cutoff=knowledge_cutoff,
            at=at,
            text_origin=text_origin,
        )

        context_summary_insights_item.additional_properties = d
        return context_summary_insights_item

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
