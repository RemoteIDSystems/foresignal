from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.analyst_profile_input_allowed_actions_item import (
    AnalystProfileInputAllowedActionsItem,
)
from ..models.analyst_profile_input_effort import AnalystProfileInputEffort
from ..models.analyst_profile_input_notify_min_priority import (
    AnalystProfileInputNotifyMinPriority,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analyst_profile_input_schedule import AnalystProfileInputSchedule
    from ..models.analyst_profile_input_scope import AnalystProfileInputScope


T = TypeVar("T", bound="AnalystProfileInput")


@_attrs_define
class AnalystProfileInput:
    """Analyst profile create or replace body

    Attributes:
        name (str):
        enabled (bool | Unset):  Default: True.
        schedule (AnalystProfileInputSchedule | Unset): 5-field cron with an IANA timezone, or `every` (≥ 10m); default
            every 30m
        scope (AnalystProfileInputScope | Unset): Inputs the analyst looks at
        lookback (None | str | Unset): Default: since the last run, capped at 7d; history tools may reach back 90d
            Example: 7d.
        instructions (None | str | Unset):
        provider_id (None | str | Unset): LLM provider; null uses the chat role
        effort (AnalystProfileInputEffort | Unset):  Default: AnalystProfileInputEffort.LOW.
        max_runs_per_day (int | Unset):  Default: 48.
        max_tokens_per_run (int | Unset):  Default: 60000.
        max_tool_calls (int | Unset):  Default: 16.
        max_wall_seconds (int | Unset):  Default: 180.
        allow_actions (bool | Unset): Requires the caller to hold agent scope; restricted tokens cannot own an action-
            enabled profile Default: False.
        allowed_actions (list[AnalystProfileInputAllowedActionsItem] | Unset): Extensible (128 adds proposal types)
        max_auto_actions_per_day (int | Unset):  Default: 5.
        channel_ids (list[str] | Unset):
        notify_min_priority (AnalystProfileInputNotifyMinPriority | Unset):  Default:
            AnalystProfileInputNotifyMinPriority.NORMAL.
    """

    name: str
    enabled: bool | Unset = True
    schedule: AnalystProfileInputSchedule | Unset = UNSET
    scope: AnalystProfileInputScope | Unset = UNSET
    lookback: None | str | Unset = UNSET
    instructions: None | str | Unset = UNSET
    provider_id: None | str | Unset = UNSET
    effort: AnalystProfileInputEffort | Unset = AnalystProfileInputEffort.LOW
    max_runs_per_day: int | Unset = 48
    max_tokens_per_run: int | Unset = 60000
    max_tool_calls: int | Unset = 16
    max_wall_seconds: int | Unset = 180
    allow_actions: bool | Unset = False
    allowed_actions: list[AnalystProfileInputAllowedActionsItem] | Unset = UNSET
    max_auto_actions_per_day: int | Unset = 5
    channel_ids: list[str] | Unset = UNSET
    notify_min_priority: AnalystProfileInputNotifyMinPriority | Unset = (
        AnalystProfileInputNotifyMinPriority.NORMAL
    )

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        enabled = self.enabled

        schedule: dict[str, Any] | Unset = UNSET
        if not isinstance(self.schedule, Unset):
            schedule = self.schedule.to_dict()

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

        effort: str | Unset = UNSET
        if not isinstance(self.effort, Unset):
            effort = self.effort.value

        max_runs_per_day = self.max_runs_per_day

        max_tokens_per_run = self.max_tokens_per_run

        max_tool_calls = self.max_tool_calls

        max_wall_seconds = self.max_wall_seconds

        allow_actions = self.allow_actions

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

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "name": name,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if schedule is not UNSET:
            field_dict["schedule"] = schedule
        if scope is not UNSET:
            field_dict["scope"] = scope
        if lookback is not UNSET:
            field_dict["lookback"] = lookback
        if instructions is not UNSET:
            field_dict["instructions"] = instructions
        if provider_id is not UNSET:
            field_dict["providerId"] = provider_id
        if effort is not UNSET:
            field_dict["effort"] = effort
        if max_runs_per_day is not UNSET:
            field_dict["maxRunsPerDay"] = max_runs_per_day
        if max_tokens_per_run is not UNSET:
            field_dict["maxTokensPerRun"] = max_tokens_per_run
        if max_tool_calls is not UNSET:
            field_dict["maxToolCalls"] = max_tool_calls
        if max_wall_seconds is not UNSET:
            field_dict["maxWallSeconds"] = max_wall_seconds
        if allow_actions is not UNSET:
            field_dict["allowActions"] = allow_actions
        if allowed_actions is not UNSET:
            field_dict["allowedActions"] = allowed_actions
        if max_auto_actions_per_day is not UNSET:
            field_dict["maxAutoActionsPerDay"] = max_auto_actions_per_day
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if notify_min_priority is not UNSET:
            field_dict["notifyMinPriority"] = notify_min_priority

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analyst_profile_input_schedule import AnalystProfileInputSchedule
        from ..models.analyst_profile_input_scope import AnalystProfileInputScope

        d = dict(src_dict)
        name = d.pop("name")

        enabled = d.pop("enabled", UNSET)

        _schedule = d.pop("schedule", UNSET)
        schedule: AnalystProfileInputSchedule | Unset
        if isinstance(_schedule, Unset):
            schedule = UNSET
        else:
            schedule = AnalystProfileInputSchedule.from_dict(_schedule)

        _scope = d.pop("scope", UNSET)
        scope: AnalystProfileInputScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = AnalystProfileInputScope.from_dict(_scope)

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

        _effort = d.pop("effort", UNSET)
        effort: AnalystProfileInputEffort | Unset
        if isinstance(_effort, Unset):
            effort = UNSET
        else:
            effort = AnalystProfileInputEffort(_effort)

        max_runs_per_day = d.pop("maxRunsPerDay", UNSET)

        max_tokens_per_run = d.pop("maxTokensPerRun", UNSET)

        max_tool_calls = d.pop("maxToolCalls", UNSET)

        max_wall_seconds = d.pop("maxWallSeconds", UNSET)

        allow_actions = d.pop("allowActions", UNSET)

        _allowed_actions = d.pop("allowedActions", UNSET)
        allowed_actions: list[AnalystProfileInputAllowedActionsItem] | Unset = UNSET
        if _allowed_actions is not UNSET:
            allowed_actions = []
            for allowed_actions_item_data in _allowed_actions:
                allowed_actions_item = AnalystProfileInputAllowedActionsItem(
                    allowed_actions_item_data
                )

                allowed_actions.append(allowed_actions_item)

        max_auto_actions_per_day = d.pop("maxAutoActionsPerDay", UNSET)

        channel_ids = cast(list[str], d.pop("channelIds", UNSET))

        _notify_min_priority = d.pop("notifyMinPriority", UNSET)
        notify_min_priority: AnalystProfileInputNotifyMinPriority | Unset
        if isinstance(_notify_min_priority, Unset):
            notify_min_priority = UNSET
        else:
            notify_min_priority = AnalystProfileInputNotifyMinPriority(
                _notify_min_priority
            )

        analyst_profile_input = cls(
            name=name,
            enabled=enabled,
            schedule=schedule,
            scope=scope,
            lookback=lookback,
            instructions=instructions,
            provider_id=provider_id,
            effort=effort,
            max_runs_per_day=max_runs_per_day,
            max_tokens_per_run=max_tokens_per_run,
            max_tool_calls=max_tool_calls,
            max_wall_seconds=max_wall_seconds,
            allow_actions=allow_actions,
            allowed_actions=allowed_actions,
            max_auto_actions_per_day=max_auto_actions_per_day,
            channel_ids=channel_ids,
            notify_min_priority=notify_min_priority,
        )

        return analyst_profile_input
