from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analysis_cycle_cadence import AnalysisCycleCadence
from ..models.analysis_cycle_notify_min_priority import AnalysisCycleNotifyMinPriority
from ..models.analysis_cycle_roles_item import AnalysisCycleRolesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_cycle_schedule import AnalysisCycleSchedule
    from ..models.cycle_scope import CycleScope


T = TypeVar("T", bound="AnalysisCycle")


@_attrs_define
class AnalysisCycle:
    """A daily, weekly or monthly analysis cycle

    Attributes:
        name (str):
        cadence (AnalysisCycleCadence):
        schedule (AnalysisCycleSchedule): 5-field cron and IANA timezone
        scope (CycleScope): What a cycle covers
        roles (list[AnalysisCycleRolesItem]):
        samples (int):  Default: 3.
        enabled (bool):
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        created_at (datetime.datetime):
        analyst_profile_id (None | str | Unset):
        forecaster_providers (list[str] | Unset): Provider ids for the forecaster role; default the profile's
        max_questions (int | Unset): Default 15 daily, 40 weekly, 60 monthly
        max_tokens_per_cycle (int | Unset): Default 300k daily, 1.2M weekly, 2M monthly
        channel_ids (list[str] | Unset):
        notify_min_priority (AnalysisCycleNotifyMinPriority | Unset):
        built_in (bool | Unset):
        next_run_at (datetime.datetime | None | Unset):
        last_run_id (None | str | Unset):
        updated_at (datetime.datetime | Unset):
    """

    name: str
    cadence: AnalysisCycleCadence
    schedule: AnalysisCycleSchedule
    scope: CycleScope
    roles: list[AnalysisCycleRolesItem]
    enabled: bool
    id: str
    created_at: datetime.datetime
    samples: int = 3
    analyst_profile_id: None | str | Unset = UNSET
    forecaster_providers: list[str] | Unset = UNSET
    max_questions: int | Unset = UNSET
    max_tokens_per_cycle: int | Unset = UNSET
    channel_ids: list[str] | Unset = UNSET
    notify_min_priority: AnalysisCycleNotifyMinPriority | Unset = UNSET
    built_in: bool | Unset = UNSET
    next_run_at: datetime.datetime | None | Unset = UNSET
    last_run_id: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        cadence = self.cadence.value

        schedule = self.schedule.to_dict()

        scope = self.scope.to_dict()

        roles = []
        for roles_item_data in self.roles:
            roles_item = roles_item_data.value
            roles.append(roles_item)

        samples = self.samples

        enabled = self.enabled

        id = self.id

        created_at = self.created_at.isoformat()

        analyst_profile_id: None | str | Unset
        if isinstance(self.analyst_profile_id, Unset):
            analyst_profile_id = UNSET
        else:
            analyst_profile_id = self.analyst_profile_id

        forecaster_providers: list[str] | Unset = UNSET
        if not isinstance(self.forecaster_providers, Unset):
            forecaster_providers = self.forecaster_providers

        max_questions = self.max_questions

        max_tokens_per_cycle = self.max_tokens_per_cycle

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        notify_min_priority: str | Unset = UNSET
        if not isinstance(self.notify_min_priority, Unset):
            notify_min_priority = self.notify_min_priority.value

        built_in = self.built_in

        next_run_at: None | str | Unset
        if isinstance(self.next_run_at, Unset):
            next_run_at = UNSET
        elif isinstance(self.next_run_at, datetime.datetime):
            next_run_at = self.next_run_at.isoformat()
        else:
            next_run_at = self.next_run_at

        last_run_id: None | str | Unset
        if isinstance(self.last_run_id, Unset):
            last_run_id = UNSET
        else:
            last_run_id = self.last_run_id

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "cadence": cadence,
                "schedule": schedule,
                "scope": scope,
                "roles": roles,
                "samples": samples,
                "enabled": enabled,
                "id": id,
                "createdAt": created_at,
            }
        )
        if analyst_profile_id is not UNSET:
            field_dict["analystProfileId"] = analyst_profile_id
        if forecaster_providers is not UNSET:
            field_dict["forecasterProviders"] = forecaster_providers
        if max_questions is not UNSET:
            field_dict["maxQuestions"] = max_questions
        if max_tokens_per_cycle is not UNSET:
            field_dict["maxTokensPerCycle"] = max_tokens_per_cycle
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if notify_min_priority is not UNSET:
            field_dict["notifyMinPriority"] = notify_min_priority
        if built_in is not UNSET:
            field_dict["builtIn"] = built_in
        if next_run_at is not UNSET:
            field_dict["nextRunAt"] = next_run_at
        if last_run_id is not UNSET:
            field_dict["lastRunId"] = last_run_id
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_cycle_schedule import AnalysisCycleSchedule
        from ..models.cycle_scope import CycleScope

        d = dict(src_dict)
        name = d.pop("name")

        cadence = AnalysisCycleCadence(d.pop("cadence"))

        schedule = AnalysisCycleSchedule.from_dict(d.pop("schedule"))

        scope = CycleScope.from_dict(d.pop("scope"))

        roles = []
        _roles = d.pop("roles")
        for roles_item_data in _roles:
            roles_item = AnalysisCycleRolesItem(roles_item_data)

            roles.append(roles_item)

        samples = d.pop("samples")

        enabled = d.pop("enabled")

        id = d.pop("id")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_analyst_profile_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        analyst_profile_id = _parse_analyst_profile_id(d.pop("analystProfileId", UNSET))

        forecaster_providers = cast(list[str], d.pop("forecasterProviders", UNSET))

        max_questions = d.pop("maxQuestions", UNSET)

        max_tokens_per_cycle = d.pop("maxTokensPerCycle", UNSET)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        _notify_min_priority = d.pop("notifyMinPriority", UNSET)
        notify_min_priority: AnalysisCycleNotifyMinPriority | Unset
        if isinstance(_notify_min_priority, Unset):
            notify_min_priority = UNSET
        else:
            notify_min_priority = AnalysisCycleNotifyMinPriority(_notify_min_priority)

        built_in = d.pop("builtIn", UNSET)

        def _parse_next_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return next_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_run_at = _parse_next_run_at(d.pop("nextRunAt", UNSET))

        def _parse_last_run_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_run_id = _parse_last_run_id(d.pop("lastRunId", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        analysis_cycle = cls(
            name=name,
            cadence=cadence,
            schedule=schedule,
            scope=scope,
            roles=roles,
            samples=samples,
            enabled=enabled,
            id=id,
            created_at=created_at,
            analyst_profile_id=analyst_profile_id,
            forecaster_providers=forecaster_providers,
            max_questions=max_questions,
            max_tokens_per_cycle=max_tokens_per_cycle,
            channel_ids=channel_ids,
            notify_min_priority=notify_min_priority,
            built_in=built_in,
            next_run_at=next_run_at,
            last_run_id=last_run_id,
            updated_at=updated_at,
        )

        analysis_cycle.additional_properties = d
        return analysis_cycle

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
