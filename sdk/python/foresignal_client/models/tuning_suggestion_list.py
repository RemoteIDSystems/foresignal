from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.tuning_suggestion_record import TuningSuggestionRecord


T = TypeVar("T", bound="TuningSuggestionList")


@_attrs_define
class TuningSuggestionList:
    """Tuning suggestions for one rule (TuningSuggester 1.1.0, 115): persisted records with their lifecycle

    Attributes:
        rule_id (UUID):
        since (datetime.datetime):
        suggester_version (str):  Example: 1.0.0.
        suggestions (list[TuningSuggestionRecord]):
        simulation_job_id (None | str | Unset): Backtest job id when simulate=true
        job_id (None | str | Unset): Alias of simulationJobId (103)
    """

    rule_id: UUID
    since: datetime.datetime
    suggester_version: str
    suggestions: list[TuningSuggestionRecord]
    simulation_job_id: None | str | Unset = UNSET
    job_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = str(self.rule_id)

        since = self.since.isoformat()

        suggester_version = self.suggester_version

        suggestions = []
        for suggestions_item_data in self.suggestions:
            suggestions_item = suggestions_item_data.to_dict()
            suggestions.append(suggestions_item)

        simulation_job_id: None | str | Unset
        if isinstance(self.simulation_job_id, Unset):
            simulation_job_id = UNSET
        else:
            simulation_job_id = self.simulation_job_id

        job_id: None | str | Unset
        if isinstance(self.job_id, Unset):
            job_id = UNSET
        else:
            job_id = self.job_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "since": since,
                "suggesterVersion": suggester_version,
                "suggestions": suggestions,
            }
        )
        if simulation_job_id is not UNSET:
            field_dict["simulationJobId"] = simulation_job_id
        if job_id is not UNSET:
            field_dict["jobId"] = job_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.tuning_suggestion_record import TuningSuggestionRecord

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        since = datetime.datetime.fromisoformat(d.pop("since"))

        suggester_version = d.pop("suggesterVersion")

        suggestions = []
        _suggestions = d.pop("suggestions")
        for suggestions_item_data in _suggestions:
            suggestions_item = TuningSuggestionRecord.from_dict(suggestions_item_data)

            suggestions.append(suggestions_item)

        def _parse_simulation_job_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        simulation_job_id = _parse_simulation_job_id(d.pop("simulationJobId", UNSET))

        def _parse_job_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        job_id = _parse_job_id(d.pop("jobId", UNSET))

        tuning_suggestion_list = cls(
            rule_id=rule_id,
            since=since,
            suggester_version=suggester_version,
            suggestions=suggestions,
            simulation_job_id=simulation_job_id,
            job_id=job_id,
        )

        tuning_suggestion_list.additional_properties = d
        return tuning_suggestion_list

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
