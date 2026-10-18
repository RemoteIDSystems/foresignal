from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_profile_allowed_actions_item import (
    AnalystProfileAllowedActionsItem,
)
from ..models.analyst_profile_effort import AnalystProfileEffort
from ..models.analyst_profile_notify_min_priority import AnalystProfileNotifyMinPriority
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_profile_schedule import AnalystProfileSchedule
    from ..models.analyst_profile_scope import AnalystProfileScope


T = TypeVar("T", bound="AnalystProfile")


@_attrs_define
class AnalystProfile:
    """A scheduled analyst profile with scope, model, budgets, action permissions and delivery

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        enabled (bool):  Default: True.
        schedule (AnalystProfileSchedule): 5-field cron with an IANA timezone, or `every` (≥ 10m); default every 30m
        effort (AnalystProfileEffort):  Default: AnalystProfileEffort.LOW.
        allow_actions (bool): Requires the caller to hold agent scope; restricted tokens cannot own an action-enabled
            profile Default: False.
        owner_token_id (str):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        scope (AnalystProfileScope | Unset): Inputs the analyst looks at
        lookback (None | str | Unset): Default: since the last run, capped at 7d; history tools may reach back 90d
            Example: 7d.
        instructions (None | str | Unset):
        provider_id (None | str | Unset): LLM provider; null uses the chat role
        max_runs_per_day (int | Unset):  Default: 48.
        max_tokens_per_run (int | Unset):  Default: 60000.
        max_tool_calls (int | Unset):  Default: 16.
        max_wall_seconds (int | Unset):  Default: 180.
        allowed_actions (list[AnalystProfileAllowedActionsItem] | Unset): Extensible (128 adds proposal types)
        max_auto_actions_per_day (int | Unset):  Default: 5.
        channel_ids (list[str] | Unset):
        notify_min_priority (AnalystProfileNotifyMinPriority | Unset):  Default: AnalystProfileNotifyMinPriority.NORMAL.
        next_run_at (datetime.datetime | None | Unset):
        last_run_at (datetime.datetime | None | Unset):
    """

    id: str
    name: str
    schedule: AnalystProfileSchedule
    owner_token_id: str
    created_at: datetime.datetime
    updated_at: datetime.datetime
    enabled: bool = True
    effort: AnalystProfileEffort = AnalystProfileEffort.LOW
    allow_actions: bool = False
    scope: AnalystProfileScope | Unset = UNSET
    lookback: None | str | Unset = UNSET
    instructions: None | str | Unset = UNSET
    provider_id: None | str | Unset = UNSET
    max_runs_per_day: int | Unset = 48
    max_tokens_per_run: int | Unset = 60000
    max_tool_calls: int | Unset = 16
    max_wall_seconds: int | Unset = 180
    allowed_actions: list[AnalystProfileAllowedActionsItem] | Unset = UNSET
    max_auto_actions_per_day: int | Unset = 5
    channel_ids: list[str] | Unset = UNSET
    notify_min_priority: AnalystProfileNotifyMinPriority | Unset = (
        AnalystProfileNotifyMinPriority.NORMAL
    )
    next_run_at: datetime.datetime | None | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        enabled = self.enabled

        schedule = self.schedule.to_dict()

        effort = self.effort.value

        allow_actions = self.allow_actions

        owner_token_id = self.owner_token_id

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        scope: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scope, Unset):
            scope = self.scope.to_dict()

        lookback: None | str | Unset
        if isinstance(self.lookback, Unset):
            lookback = UNSET
        else:
            lookback = self.lookback

        instructions: None | str | Unset
        if isinstance(self.instructions, Unset):
            instructions = UNSET
        else:
            instructions = self.instructions

        provider_id: None | str | Unset
        if isinstance(self.provider_id, Unset):
            provider_id = UNSET
        else:
            provider_id = self.provider_id

        max_runs_per_day = self.max_runs_per_day

        max_tokens_per_run = self.max_tokens_per_run

        max_tool_calls = self.max_tool_calls

        max_wall_seconds = self.max_wall_seconds

        allowed_actions: list[str] | Unset = UNSET
        if not isinstance(self.allowed_actions, Unset):
            allowed_actions = []
            for allowed_actions_item_data in self.allowed_actions:
                allowed_actions_item = allowed_actions_item_data.value
                allowed_actions.append(allowed_actions_item)

        max_auto_actions_per_day = self.max_auto_actions_per_day

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = self.channel_ids

        notify_min_priority: str | Unset = UNSET
        if not isinstance(self.notify_min_priority, Unset):
            notify_min_priority = self.notify_min_priority.value

        next_run_at: None | str | Unset
        if isinstance(self.next_run_at, Unset):
            next_run_at = UNSET
        elif isinstance(self.next_run_at, datetime.datetime):
            next_run_at = self.next_run_at.isoformat()
        else:
            next_run_at = self.next_run_at

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "enabled": enabled,
                "schedule": schedule,
                "effort": effort,
                "allowActions": allow_actions,
                "ownerTokenId": owner_token_id,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if scope is not UNSET:
            field_dict["scope"] = scope
        if lookback is not UNSET:
            field_dict["lookback"] = lookback
        if instructions is not UNSET:
            field_dict["instructions"] = instructions
        if provider_id is not UNSET:
            field_dict["providerId"] = provider_id
        if max_runs_per_day is not UNSET:
            field_dict["maxRunsPerDay"] = max_runs_per_day
        if max_tokens_per_run is not UNSET:
            field_dict["maxTokensPerRun"] = max_tokens_per_run
        if max_tool_calls is not UNSET:
            field_dict["maxToolCalls"] = max_tool_calls
        if max_wall_seconds is not UNSET:
            field_dict["maxWallSeconds"] = max_wall_seconds
        if allowed_actions is not UNSET:
            field_dict["allowedActions"] = allowed_actions
        if max_auto_actions_per_day is not UNSET:
            field_dict["maxAutoActionsPerDay"] = max_auto_actions_per_day
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if notify_min_priority is not UNSET:
            field_dict["notifyMinPriority"] = notify_min_priority
        if next_run_at is not UNSET:
            field_dict["nextRunAt"] = next_run_at
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_profile_schedule import AnalystProfileSchedule
        from ..models.analyst_profile_scope import AnalystProfileScope

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        enabled = d.pop("enabled")

        schedule = AnalystProfileSchedule.from_dict(d.pop("schedule"))

        effort = AnalystProfileEffort(d.pop("effort"))

        allow_actions = d.pop("allowActions")

        owner_token_id = d.pop("ownerTokenId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        _scope = d.pop("scope", UNSET)
        scope: AnalystProfileScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = AnalystProfileScope.from_dict(_scope)

        def _parse_lookback(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lookback = _parse_lookback(d.pop("lookback", UNSET))

        def _parse_instructions(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        instructions = _parse_instructions(d.pop("instructions", UNSET))

        def _parse_provider_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        provider_id = _parse_provider_id(d.pop("providerId", UNSET))

        max_runs_per_day = d.pop("maxRunsPerDay", UNSET)

        max_tokens_per_run = d.pop("maxTokensPerRun", UNSET)

        max_tool_calls = d.pop("maxToolCalls", UNSET)

        max_wall_seconds = d.pop("maxWallSeconds", UNSET)

        _allowed_actions = d.pop("allowedActions", UNSET)
        allowed_actions: list[AnalystProfileAllowedActionsItem] | Unset = UNSET
        if _allowed_actions is not UNSET:
            allowed_actions = []
            for allowed_actions_item_data in _allowed_actions:
                allowed_actions_item = AnalystProfileAllowedActionsItem(
                    allowed_actions_item_data
                )

                allowed_actions.append(allowed_actions_item)

        max_auto_actions_per_day = d.pop("maxAutoActionsPerDay", UNSET)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        _notify_min_priority = d.pop("notifyMinPriority", UNSET)
        notify_min_priority: AnalystProfileNotifyMinPriority | Unset
        if isinstance(_notify_min_priority, Unset):
            notify_min_priority = UNSET
        else:
            notify_min_priority = AnalystProfileNotifyMinPriority(_notify_min_priority)

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

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        analyst_profile = cls(
            id=id,
            name=name,
            enabled=enabled,
            schedule=schedule,
            effort=effort,
            allow_actions=allow_actions,
            owner_token_id=owner_token_id,
            created_at=created_at,
            updated_at=updated_at,
            scope=scope,
            lookback=lookback,
            instructions=instructions,
            provider_id=provider_id,
            max_runs_per_day=max_runs_per_day,
            max_tokens_per_run=max_tokens_per_run,
            max_tool_calls=max_tool_calls,
            max_wall_seconds=max_wall_seconds,
            allowed_actions=allowed_actions,
            max_auto_actions_per_day=max_auto_actions_per_day,
            channel_ids=channel_ids,
            notify_min_priority=notify_min_priority,
            next_run_at=next_run_at,
            last_run_at=last_run_at,
        )

        analyst_profile.additional_properties = d
        return analyst_profile

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
