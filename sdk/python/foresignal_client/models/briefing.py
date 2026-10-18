from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_input_mode import BriefingInputMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0
    from ..models.briefing_scope import BriefingScope


T = TypeVar("T", bound="Briefing")


@_attrs_define
class Briefing:
    """
    Attributes:
        name (str):
        schedule (str): 5-field cron Example: 0 7 * * *.
        id (UUID):
        created_at (datetime.datetime):
        enabled (bool | Unset):  Default: True.
        timezone (str | Unset):  Default: 'UTC'. Example: America/Chicago.
        scope (BriefingScope | Unset):
        lookback (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        mode (BriefingInputMode | Unset):  Default: BriefingInputMode.DIGEST.
        instructions (None | str | Unset):
        channel_ids (list[UUID] | Unset):
        max_items_per_kind (int | Unset):  Default: 5.
        audio (BriefingInputAudioType0 | None | Unset):
        updated_at (datetime.datetime | Unset):
        next_run_at (datetime.datetime | None | Unset):
        last_run_at (datetime.datetime | None | Unset):
        last_run_status (None | str | Unset):
        warnings (list[str] | Unset): Non-fatal validation warnings, e.g. a channel not subscribed to `briefing`
    """

    name: str
    schedule: str
    id: UUID
    created_at: datetime.datetime
    enabled: bool | Unset = True
    timezone: str | Unset = "UTC"
    scope: BriefingScope | Unset = UNSET
    lookback: None | str | Unset = UNSET
    mode: BriefingInputMode | Unset = BriefingInputMode.DIGEST
    instructions: None | str | Unset = UNSET
    channel_ids: list[UUID] | Unset = UNSET
    max_items_per_kind: int | Unset = 5
    audio: BriefingInputAudioType0 | None | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    next_run_at: datetime.datetime | None | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    last_run_status: None | str | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0

        name = self.name

        schedule = self.schedule

        id = str(self.id)

        created_at = self.created_at.isoformat()

        enabled = self.enabled

        timezone = self.timezone

        scope: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scope, Unset):
            scope = self.scope.to_dict()

        lookback: None | str | Unset
        if isinstance(self.lookback, Unset):
            lookback = UNSET
        else:
            lookback = self.lookback

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        instructions: None | str | Unset
        if isinstance(self.instructions, Unset):
            instructions = UNSET
        else:
            instructions = self.instructions

        channel_ids: list[str] | Unset = UNSET
        if not isinstance(self.channel_ids, Unset):
            channel_ids = []
            for channel_ids_item_data in self.channel_ids:
                channel_ids_item = str(channel_ids_item_data)
                channel_ids.append(channel_ids_item)

        max_items_per_kind = self.max_items_per_kind

        audio: dict[str, Any] | None | Unset
        if isinstance(self.audio, Unset):
            audio = UNSET
        elif isinstance(self.audio, BriefingInputAudioType0):
            audio = self.audio.to_dict()
        else:
            audio = self.audio

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

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

        last_run_status: None | str | Unset
        if isinstance(self.last_run_status, Unset):
            last_run_status = UNSET
        else:
            last_run_status = self.last_run_status

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "schedule": schedule,
                "id": id,
                "createdAt": created_at,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if scope is not UNSET:
            field_dict["scope"] = scope
        if lookback is not UNSET:
            field_dict["lookback"] = lookback
        if mode is not UNSET:
            field_dict["mode"] = mode
        if instructions is not UNSET:
            field_dict["instructions"] = instructions
        if channel_ids is not UNSET:
            field_dict["channelIds"] = channel_ids
        if max_items_per_kind is not UNSET:
            field_dict["maxItemsPerKind"] = max_items_per_kind
        if audio is not UNSET:
            field_dict["audio"] = audio
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if next_run_at is not UNSET:
            field_dict["nextRunAt"] = next_run_at
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if last_run_status is not UNSET:
            field_dict["lastRunStatus"] = last_run_status
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0
        from ..models.briefing_scope import BriefingScope

        d = dict(src_dict)
        name = d.pop("name")

        schedule = d.pop("schedule")

        id = UUID(d.pop("id"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        enabled = d.pop("enabled", UNSET)

        timezone = d.pop("timezone", UNSET)

        _scope = d.pop("scope", UNSET)
        scope: BriefingScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = BriefingScope.from_dict(_scope)

        def _parse_lookback(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lookback = _parse_lookback(d.pop("lookback", UNSET))

        _mode = d.pop("mode", UNSET)
        mode: BriefingInputMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = BriefingInputMode(_mode)

        def _parse_instructions(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        instructions = _parse_instructions(d.pop("instructions", UNSET))

        _channel_ids = d.pop("channelIds", UNSET)
        channel_ids: list[UUID] | Unset = UNSET
        if _channel_ids is not UNSET:
            channel_ids = []
            for channel_ids_item_data in _channel_ids:
                channel_ids_item = UUID(channel_ids_item_data)

                channel_ids.append(channel_ids_item)

        max_items_per_kind = d.pop("maxItemsPerKind", UNSET)

        def _parse_audio(data: object) -> BriefingInputAudioType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                audio_type_0 = BriefingInputAudioType0.from_dict(data)

                return audio_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BriefingInputAudioType0 | None | Unset, data)

        audio = _parse_audio(d.pop("audio", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

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

        def _parse_last_run_status(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_run_status = _parse_last_run_status(d.pop("lastRunStatus", UNSET))

        warnings = cast(list[str], d.pop("warnings", UNSET))

        briefing = cls(
            name=name,
            schedule=schedule,
            id=id,
            created_at=created_at,
            enabled=enabled,
            timezone=timezone,
            scope=scope,
            lookback=lookback,
            mode=mode,
            instructions=instructions,
            channel_ids=channel_ids,
            max_items_per_kind=max_items_per_kind,
            audio=audio,
            updated_at=updated_at,
            next_run_at=next_run_at,
            last_run_at=last_run_at,
            last_run_status=last_run_status,
            warnings=warnings,
        )

        briefing.additional_properties = d
        return briefing

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
