from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LedgerQuestionBriefForecastersItem")


@_attrs_define
class LedgerQuestionBriefForecastersItem:
    """Latest forecast by one forecaster

    Attributes:
        forecaster_id (str):  Example: indicator-bayes.
        probability (float | None | Unset):
        made_at (datetime.datetime | Unset):
        knowledge_cutoff (datetime.date | None | Unset):
    """

    forecaster_id: str
    probability: float | None | Unset = UNSET
    made_at: datetime.datetime | Unset = UNSET
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecaster_id = self.forecaster_id

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        made_at: str | Unset = UNSET
        if not isinstance(self.made_at, Unset):
            made_at = self.made_at.isoformat()

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
            }
        )
        if probability is not UNSET:
            field_dict["probability"] = probability
        if made_at is not UNSET:
            field_dict["madeAt"] = made_at
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        _made_at = d.pop("madeAt", UNSET)
        made_at: datetime.datetime | Unset
        if isinstance(_made_at, Unset):
            made_at = UNSET
        else:
            made_at = datetime.datetime.fromisoformat(_made_at)

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

        ledger_question_brief_forecasters_item = cls(
            forecaster_id=forecaster_id,
            probability=probability,
            made_at=made_at,
            knowledge_cutoff=knowledge_cutoff,
        )

        ledger_question_brief_forecasters_item.additional_properties = d
        return ledger_question_brief_forecasters_item

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
