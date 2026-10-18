from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_evaluation_profiles_item_runs_by_status import (
        AnalystEvaluationProfilesItemRunsByStatus,
    )
    from ..models.analyst_evaluation_profiles_item_tokens_per_day_item import (
        AnalystEvaluationProfilesItemTokensPerDayItem,
    )


T = TypeVar("T", bound="AnalystEvaluationProfilesItem")


@_attrs_define
class AnalystEvaluationProfilesItem:
    """Aggregates for one profile

    Attributes:
        profile_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        runs_by_status (AnalystEvaluationProfilesItemRunsByStatus):
        noop_share (float | Unset):
        tokens_per_day (list[AnalystEvaluationProfilesItemTokensPerDayItem] | Unset):
        proposal_acceptance_rate (float | None | Unset): confirmed ÷ decided
        notes_dismissed_share (float | None | Unset):
        useful_ratio (float | None | Unset):
        wrong_ratio (float | None | Unset):
        citations_dropped_per_run (float | None | Unset):
    """

    profile_id: str
    runs_by_status: AnalystEvaluationProfilesItemRunsByStatus
    noop_share: float | Unset = UNSET
    tokens_per_day: list[AnalystEvaluationProfilesItemTokensPerDayItem] | Unset = UNSET
    proposal_acceptance_rate: float | None | Unset = UNSET
    notes_dismissed_share: float | None | Unset = UNSET
    useful_ratio: float | None | Unset = UNSET
    wrong_ratio: float | None | Unset = UNSET
    citations_dropped_per_run: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        profile_id = self.profile_id

        runs_by_status = self.runs_by_status.to_dict()

        noop_share = self.noop_share

        tokens_per_day: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.tokens_per_day, Unset):
            tokens_per_day = []
            for tokens_per_day_item_data in self.tokens_per_day:
                tokens_per_day_item = tokens_per_day_item_data.to_dict()
                tokens_per_day.append(tokens_per_day_item)

        proposal_acceptance_rate: float | None | Unset
        if isinstance(self.proposal_acceptance_rate, Unset):
            proposal_acceptance_rate = UNSET
        else:
            proposal_acceptance_rate = self.proposal_acceptance_rate

        notes_dismissed_share: float | None | Unset
        if isinstance(self.notes_dismissed_share, Unset):
            notes_dismissed_share = UNSET
        else:
            notes_dismissed_share = self.notes_dismissed_share

        useful_ratio: float | None | Unset
        if isinstance(self.useful_ratio, Unset):
            useful_ratio = UNSET
        else:
            useful_ratio = self.useful_ratio

        wrong_ratio: float | None | Unset
        if isinstance(self.wrong_ratio, Unset):
            wrong_ratio = UNSET
        else:
            wrong_ratio = self.wrong_ratio

        citations_dropped_per_run: float | None | Unset
        if isinstance(self.citations_dropped_per_run, Unset):
            citations_dropped_per_run = UNSET
        else:
            citations_dropped_per_run = self.citations_dropped_per_run

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "profileId": profile_id,
                "runsByStatus": runs_by_status,
            }
        )
        if noop_share is not UNSET:
            field_dict["noopShare"] = noop_share
        if tokens_per_day is not UNSET:
            field_dict["tokensPerDay"] = tokens_per_day
        if proposal_acceptance_rate is not UNSET:
            field_dict["proposalAcceptanceRate"] = proposal_acceptance_rate
        if notes_dismissed_share is not UNSET:
            field_dict["notesDismissedShare"] = notes_dismissed_share
        if useful_ratio is not UNSET:
            field_dict["usefulRatio"] = useful_ratio
        if wrong_ratio is not UNSET:
            field_dict["wrongRatio"] = wrong_ratio
        if citations_dropped_per_run is not UNSET:
            field_dict["citationsDroppedPerRun"] = citations_dropped_per_run

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_evaluation_profiles_item_runs_by_status import (
            AnalystEvaluationProfilesItemRunsByStatus,
        )
        from ..models.analyst_evaluation_profiles_item_tokens_per_day_item import (
            AnalystEvaluationProfilesItemTokensPerDayItem,
        )

        d = dict(src_dict)
        profile_id = d.pop("profileId")

        runs_by_status = AnalystEvaluationProfilesItemRunsByStatus.from_dict(
            d.pop("runsByStatus")
        )

        noop_share = d.pop("noopShare", UNSET)

        _tokens_per_day = d.pop("tokensPerDay", UNSET)
        tokens_per_day: list[AnalystEvaluationProfilesItemTokensPerDayItem] | Unset = (
            UNSET
        )
        if _tokens_per_day is not UNSET:
            tokens_per_day = []
            for tokens_per_day_item_data in _tokens_per_day:
                tokens_per_day_item = (
                    AnalystEvaluationProfilesItemTokensPerDayItem.from_dict(
                        tokens_per_day_item_data
                    )
                )

                tokens_per_day.append(tokens_per_day_item)

        def _parse_proposal_acceptance_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        proposal_acceptance_rate = _parse_proposal_acceptance_rate(
            d.pop("proposalAcceptanceRate", UNSET)
        )

        def _parse_notes_dismissed_share(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        notes_dismissed_share = _parse_notes_dismissed_share(
            d.pop("notesDismissedShare", UNSET)
        )

        def _parse_useful_ratio(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        useful_ratio = _parse_useful_ratio(d.pop("usefulRatio", UNSET))

        def _parse_wrong_ratio(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        wrong_ratio = _parse_wrong_ratio(d.pop("wrongRatio", UNSET))

        def _parse_citations_dropped_per_run(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        citations_dropped_per_run = _parse_citations_dropped_per_run(
            d.pop("citationsDroppedPerRun", UNSET)
        )

        analyst_evaluation_profiles_item = cls(
            profile_id=profile_id,
            runs_by_status=runs_by_status,
            noop_share=noop_share,
            tokens_per_day=tokens_per_day,
            proposal_acceptance_rate=proposal_acceptance_rate,
            notes_dismissed_share=notes_dismissed_share,
            useful_ratio=useful_ratio,
            wrong_ratio=wrong_ratio,
            citations_dropped_per_run=citations_dropped_per_run,
        )

        analyst_evaluation_profiles_item.additional_properties = d
        return analyst_evaluation_profiles_item

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
