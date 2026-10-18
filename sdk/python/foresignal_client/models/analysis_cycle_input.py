from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analysis_cycle_input_cadence import AnalysisCycleInputCadence
from ..models.analysis_cycle_input_notify_min_priority import (
    AnalysisCycleInputNotifyMinPriority,
)
from ..models.analysis_cycle_input_roles_item import AnalysisCycleInputRolesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analysis_cycle_input_schedule import AnalysisCycleInputSchedule
    from ..models.cycle_scope import CycleScope


T = TypeVar("T", bound="AnalysisCycleInput")


@_attrs_define
class AnalysisCycleInput:
    """Create or replace an analysis cycle

    Attributes:
        name (str):
        cadence (AnalysisCycleInputCadence):
        schedule (AnalysisCycleInputSchedule): 5-field cron and IANA timezone
        scope (CycleScope | Unset): What a cycle covers
        analyst_profile_id (None | str | Unset):
        roles (list[AnalysisCycleInputRolesItem] | Unset):
        forecaster_providers (list[str] | Unset): Provider ids for the forecaster role; default the profile's
        samples (int | Unset):  Default: 3.
        max_questions (int | Unset): Default 15 daily, 40 weekly, 60 monthly
        max_tokens_per_cycle (int | Unset): Default 300k daily, 1.2M weekly, 2M monthly
        channel_ids (list[str] | Unset):
        notify_min_priority (AnalysisCycleInputNotifyMinPriority | Unset):
        enabled (bool | Unset):
    """

    name: str
    cadence: AnalysisCycleInputCadence
    schedule: AnalysisCycleInputSchedule
    scope: CycleScope | Unset = UNSET
    analyst_profile_id: None | str | Unset = UNSET
    roles: list[AnalysisCycleInputRolesItem] | Unset = UNSET
    forecaster_providers: list[str] | Unset = UNSET
    samples: int | Unset = 3
    max_questions: int | Unset = UNSET
    max_tokens_per_cycle: int | Unset = UNSET
    channel_ids: list[str] | Unset = UNSET
    notify_min_priority: AnalysisCycleInputNotifyMinPriority | Unset = UNSET
    enabled: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        cadence = self.cadence.value

        schedule = self.schedule.to_dict()

        scope: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scope, Unset):
            scope = self.scope.to_dict()

        analyst_profile_id: None | str | Unset
        if isinstance(self.analyst_profile_id, Unset):
            analyst_profile_id = UNSET
        else:
            analyst_profile_id = self.analyst_profile_id

        roles: list[str] | Unset = UNSET
        if not isinstance(self.roles, Unset):
            roles = []
            for roles_item_data in self.roles:
                roles_item = roles_item_data.value
                roles.append(roles_item)

        forecaster_providers: list[str] | Unset = UNSET
        if not isinstance(self.forecaster_providers, Unset):
            forecaster_providers = self.forecaster_providers

        samples = self.samples

        max_questions = self.max_questions

        max_tokens_per_cycle = self.max_tokens_per_cycle

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        notify_min_priority: str | Unset = UNSET
        if not isinstance(self.notify_min_priority, Unset):
            notify_min_priority = self.notify_min_priority.value

        enabled = self.enabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "cadence": cadence,
                "schedule": schedule,
            }
        )
        if scope is not UNSET:
            field_dict["scope"] = scope
        if analyst_profile_id is not UNSET:
            field_dict["analystProfileId"] = analyst_profile_id
        if roles is not UNSET:
            field_dict["roles"] = roles
        if forecaster_providers is not UNSET:
            field_dict["forecasterProviders"] = forecaster_providers
        if samples is not UNSET:
            field_dict["samples"] = samples
        if max_questions is not UNSET:
            field_dict["maxQuestions"] = max_questions
        if max_tokens_per_cycle is not UNSET:
            field_dict["maxTokensPerCycle"] = max_tokens_per_cycle
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if notify_min_priority is not UNSET:
            field_dict["notifyMinPriority"] = notify_min_priority
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analysis_cycle_input_schedule import AnalysisCycleInputSchedule
        from ..models.cycle_scope import CycleScope

        d = dict(src_dict)
        name = d.pop("name")

        cadence = AnalysisCycleInputCadence(d.pop("cadence"))

        schedule = AnalysisCycleInputSchedule.from_dict(d.pop("schedule"))

        _scope = d.pop("scope", UNSET)
        scope: CycleScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = CycleScope.from_dict(_scope)

        def _parse_analyst_profile_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        analyst_profile_id = _parse_analyst_profile_id(d.pop("analystProfileId", UNSET))

        _roles = d.pop("roles", UNSET)
        roles: list[AnalysisCycleInputRolesItem] | Unset = UNSET
        if _roles is not UNSET:
            roles = []
            for roles_item_data in _roles:
                roles_item = AnalysisCycleInputRolesItem(roles_item_data)

                roles.append(roles_item)

        forecaster_providers = cast(list[str], d.pop("forecasterProviders", UNSET))

        samples = d.pop("samples", UNSET)

        max_questions = d.pop("maxQuestions", UNSET)

        max_tokens_per_cycle = d.pop("maxTokensPerCycle", UNSET)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        _notify_min_priority = d.pop("notifyMinPriority", UNSET)
        notify_min_priority: AnalysisCycleInputNotifyMinPriority | Unset
        if isinstance(_notify_min_priority, Unset):
            notify_min_priority = UNSET
        else:
            notify_min_priority = AnalysisCycleInputNotifyMinPriority(
                _notify_min_priority
            )

        enabled = d.pop("enabled", UNSET)

        analysis_cycle_input = cls(
            name=name,
            cadence=cadence,
            schedule=schedule,
            scope=scope,
            analyst_profile_id=analyst_profile_id,
            roles=roles,
            forecaster_providers=forecaster_providers,
            samples=samples,
            max_questions=max_questions,
            max_tokens_per_cycle=max_tokens_per_cycle,
            channel_ids=channel_ids,
            notify_min_priority=notify_min_priority,
            enabled=enabled,
        )

        analysis_cycle_input.additional_properties = d
        return analysis_cycle_input

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
