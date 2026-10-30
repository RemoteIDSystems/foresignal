from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.briefing_input_mode import BriefingInputMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.breaking_news_config import BreakingNewsConfig
    from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0
    from ..models.briefing_input_site import BriefingInputSite
    from ..models.briefing_scope import BriefingScope
    from ..models.delivery_target import DeliveryTarget


T = TypeVar("T", bound="BriefingInput")


@_attrs_define
class BriefingInput:
    """
    Attributes:
        name (str):
        schedule (str): 5-field cron Example: 0 7 * * *.
        enabled (bool | Unset):  Default: True.
        timezone (str | Unset):  Default: 'UTC'. Example: America/Chicago.
        scope (BriefingScope | Unset):
        lookback (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        mode (BriefingInputMode | Unset):  Default: BriefingInputMode.DIGEST.
        instructions (None | str | Unset):
        channel_ids (list[UUID] | Unset):
        max_items_per_kind (int | Unset):  Default: 5.
        audio (BriefingInputAudioType0 | None | Unset):
        output_language (None | str | Unset): The language the edition is written and spoken in (NP-003); null for the
            owner's `UserPreferences.locale`, or the source language for a briefing without an owner
        delivery_targets (list[DeliveryTarget] | Unset): Where each run goes (NP-004). Empty or absent: every channel in
            `channelIds`, as before
        site (BriefingInputSite | Unset): The hosted edition site (NP-005)
        breaking_news (BreakingNewsConfig | None | Unset): Breaking-news special editions (NP-006); null for none
        include_provenance (bool | Unset): Each item carries the cross-linked evidence that put it in the edition
            (`provenance` on the run's citations, NP-007) Default: False.
        include_corroboration (bool | Unset): Each item carries its corroboration signal (`corroboration` on the run's
            citations, NP-007) Default: False.
    """

    name: str
    schedule: str
    enabled: bool | Unset = True
    timezone: str | Unset = "UTC"
    scope: BriefingScope | Unset = UNSET
    lookback: None | str | Unset = UNSET
    mode: BriefingInputMode | Unset = BriefingInputMode.DIGEST
    instructions: None | str | Unset = UNSET
    channel_ids: list[UUID] | Unset = UNSET
    max_items_per_kind: int | Unset = 5
    audio: BriefingInputAudioType0 | None | Unset = UNSET
    output_language: None | str | Unset = UNSET
    delivery_targets: list[DeliveryTarget] | Unset = UNSET
    site: BriefingInputSite | Unset = UNSET
    breaking_news: BreakingNewsConfig | None | Unset = UNSET
    include_provenance: bool | Unset = False
    include_corroboration: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.breaking_news_config import BreakingNewsConfig
        from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0

        name = self.name

        schedule = self.schedule

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

        output_language: None | str | Unset
        if isinstance(self.output_language, Unset):
            output_language = UNSET
        else:
            output_language = self.output_language

        delivery_targets: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.delivery_targets, Unset):
            delivery_targets = []
            for delivery_targets_item_data in self.delivery_targets:
                delivery_targets_item = delivery_targets_item_data.to_dict()
                delivery_targets.append(delivery_targets_item)

        site: dict[str, Any] | Unset = UNSET
        if not isinstance(self.site, Unset):
            site = self.site.to_dict()

        breaking_news: dict[str, Any] | None | Unset
        if isinstance(self.breaking_news, Unset):
            breaking_news = UNSET
        elif isinstance(self.breaking_news, BreakingNewsConfig):
            breaking_news = self.breaking_news.to_dict()
        else:
            breaking_news = self.breaking_news

        include_provenance = self.include_provenance

        include_corroboration = self.include_corroboration

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "schedule": schedule,
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
        if output_language is not UNSET:
            field_dict["outputLanguage"] = output_language
        if delivery_targets is not UNSET:
            field_dict["deliveryTargets"] = delivery_targets
        if site is not UNSET:
            field_dict["site"] = site
        if breaking_news is not UNSET:
            field_dict["breakingNews"] = breaking_news
        if include_provenance is not UNSET:
            field_dict["includeProvenance"] = include_provenance
        if include_corroboration is not UNSET:
            field_dict["includeCorroboration"] = include_corroboration

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.breaking_news_config import BreakingNewsConfig
        from ..models.briefing_input_audio_type_0 import BriefingInputAudioType0
        from ..models.briefing_input_site import BriefingInputSite
        from ..models.briefing_scope import BriefingScope
        from ..models.delivery_target import DeliveryTarget

        d = dict(src_dict)
        name = d.pop("name")

        schedule = d.pop("schedule")

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

        def _parse_output_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        output_language = _parse_output_language(d.pop("outputLanguage", UNSET))

        _delivery_targets = d.pop("deliveryTargets", UNSET)
        delivery_targets: list[DeliveryTarget] | Unset = UNSET
        if _delivery_targets is not UNSET:
            delivery_targets = []
            for delivery_targets_item_data in _delivery_targets:
                delivery_targets_item = DeliveryTarget.from_dict(
                    delivery_targets_item_data
                )

                delivery_targets.append(delivery_targets_item)

        _site = d.pop("site", UNSET)
        site: BriefingInputSite | Unset
        if isinstance(_site, Unset):
            site = UNSET
        else:
            site = BriefingInputSite.from_dict(_site)

        def _parse_breaking_news(data: object) -> BreakingNewsConfig | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                breaking_news_type_0 = BreakingNewsConfig.from_dict(data)

                return breaking_news_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BreakingNewsConfig | None | Unset, data)

        breaking_news = _parse_breaking_news(d.pop("breakingNews", UNSET))

        include_provenance = d.pop("includeProvenance", UNSET)

        include_corroboration = d.pop("includeCorroboration", UNSET)

        briefing_input = cls(
            name=name,
            schedule=schedule,
            enabled=enabled,
            timezone=timezone,
            scope=scope,
            lookback=lookback,
            mode=mode,
            instructions=instructions,
            channel_ids=channel_ids,
            max_items_per_kind=max_items_per_kind,
            audio=audio,
            output_language=output_language,
            delivery_targets=delivery_targets,
            site=site,
            breaking_news=breaking_news,
            include_provenance=include_provenance,
            include_corroboration=include_corroboration,
        )

        briefing_input.additional_properties = d
        return briefing_input

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
