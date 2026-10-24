from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_cue_backend import SocialCueBackend
from ..models.social_cue_status import SocialCueStatus
from ..models.social_platform_id import SocialPlatformId
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_cue_external_results_item import SocialCueExternalResultsItem
    from ..models.social_cue_query import SocialCueQuery
    from ..models.social_cue_results_item import SocialCueResultsItem
    from ..models.social_cue_trigger import SocialCueTrigger
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="SocialCue")


@_attrs_define
class SocialCue:
    """A cue-driven social search; results are a targeted sample that never feeds baselines and never corroborates its own
    trigger (`SocialCueService`, 137)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            trigger (SocialCueTrigger): What raised the cue
            backend (SocialCueBackend): `xai-x-search` (026 provider `xai`, ranked sample with citations) is off by default
            queries (list[SocialCueQuery]):
            platforms (list[SocialPlatformId]):
            window (TimeRange):
            status (SocialCueStatus):
            results (list[SocialCueResultsItem]):
            narrative_ids (list[str]):
            cost_usd (float):
            created_by (str): `system` for automatic cues, else the principal
            created_at (datetime.datetime):
            status_reason (None | str | Unset): e.g. BUDGET_EXCEEDED, PLATFORM_DISABLED
            external_results (list[SocialCueExternalResultsItem] | Unset): One item per EDP provider searched
            max_cost_usd (float | Unset):
            started_at (datetime.datetime | None | Unset):
            completed_at (datetime.datetime | None | Unset):
            manifest_id (None | str | Unset):
    """

    id: str
    trigger: SocialCueTrigger
    backend: SocialCueBackend
    queries: list[SocialCueQuery]
    platforms: list[SocialPlatformId]
    window: TimeRange
    status: SocialCueStatus
    results: list[SocialCueResultsItem]
    narrative_ids: list[str]
    cost_usd: float
    created_by: str
    created_at: datetime.datetime
    status_reason: None | str | Unset = UNSET
    external_results: list[SocialCueExternalResultsItem] | Unset = UNSET
    max_cost_usd: float | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    completed_at: datetime.datetime | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        trigger = self.trigger.to_dict()

        backend = self.backend.value

        queries = []
        for queries_item_data in self.queries:
            queries_item = queries_item_data.to_dict()
            queries.append(queries_item)

        platforms = []
        for platforms_item_data in self.platforms:
            platforms_item = platforms_item_data.value
            platforms.append(platforms_item)

        window = self.window.to_dict()

        status = self.status.value

        results = []
        for results_item_data in self.results:
            results_item = results_item_data.to_dict()
            results.append(results_item)

        narrative_ids = self.narrative_ids

        cost_usd = self.cost_usd

        created_by = self.created_by

        created_at = self.created_at.isoformat()

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        external_results: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.external_results, Unset):
            external_results = []
            for external_results_item_data in self.external_results:
                external_results_item = external_results_item_data.to_dict()
                external_results.append(external_results_item)

        max_cost_usd = self.max_cost_usd

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        completed_at: None | str | Unset
        if isinstance(self.completed_at, Unset):
            completed_at = UNSET
        elif isinstance(self.completed_at, datetime.datetime):
            completed_at = self.completed_at.isoformat()
        else:
            completed_at = self.completed_at

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "trigger": trigger,
                "backend": backend,
                "queries": queries,
                "platforms": platforms,
                "window": window,
                "status": status,
                "results": results,
                "narrativeIds": narrative_ids,
                "costUsd": cost_usd,
                "createdBy": created_by,
                "createdAt": created_at,
            }
        )
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if external_results is not UNSET:
            field_dict["externalResults"] = external_results
        if max_cost_usd is not UNSET:
            field_dict["maxCostUsd"] = max_cost_usd
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if completed_at is not UNSET:
            field_dict["completedAt"] = completed_at
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_cue_external_results_item import (
            SocialCueExternalResultsItem,
        )
        from ..models.social_cue_query import SocialCueQuery
        from ..models.social_cue_results_item import SocialCueResultsItem
        from ..models.social_cue_trigger import SocialCueTrigger
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        id = d.pop("id")

        trigger = SocialCueTrigger.from_dict(d.pop("trigger"))

        backend = SocialCueBackend(d.pop("backend"))

        queries = []
        _queries = d.pop("queries")
        for queries_item_data in _queries:
            queries_item = SocialCueQuery.from_dict(queries_item_data)

            queries.append(queries_item)

        platforms = []
        _platforms = d.pop("platforms")
        for platforms_item_data in _platforms:
            platforms_item = SocialPlatformId(platforms_item_data)

            platforms.append(platforms_item)

        window = TimeRange.from_dict(d.pop("window"))

        status = SocialCueStatus(d.pop("status"))

        results = []
        _results = d.pop("results")
        for results_item_data in _results:
            results_item = SocialCueResultsItem.from_dict(results_item_data)

            results.append(results_item)

        narrative_ids = cast(list[str], d.pop("narrativeIds"))

        cost_usd = d.pop("costUsd")

        created_by = d.pop("createdBy")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        _external_results = d.pop("externalResults", UNSET)
        external_results: list[SocialCueExternalResultsItem] | Unset = UNSET
        if _external_results is not UNSET:
            external_results = []
            for external_results_item_data in _external_results:
                external_results_item = SocialCueExternalResultsItem.from_dict(
                    external_results_item_data
                )

                external_results.append(external_results_item)

        max_cost_usd = d.pop("maxCostUsd", UNSET)

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        def _parse_completed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                completed_at_type_0 = datetime.datetime.fromisoformat(data)

                return completed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        completed_at = _parse_completed_at(d.pop("completedAt", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        social_cue = cls(
            id=id,
            trigger=trigger,
            backend=backend,
            queries=queries,
            platforms=platforms,
            window=window,
            status=status,
            results=results,
            narrative_ids=narrative_ids,
            cost_usd=cost_usd,
            created_by=created_by,
            created_at=created_at,
            status_reason=status_reason,
            external_results=external_results,
            max_cost_usd=max_cost_usd,
            started_at=started_at,
            completed_at=completed_at,
            manifest_id=manifest_id,
        )

        social_cue.additional_properties = d
        return social_cue

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
