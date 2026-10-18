from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ImportReport")


@_attrs_define
class ImportReport:
    """
    Attributes:
        sources_applied (list[str]):
        sources_unknown (list[str]):
        triggers_upserted (int | Unset):
        channels_upserted (int | Unset):
        briefings_upserted (int | Unset):
        custom_sources_upserted (int | Unset):
        llm_providers_upserted (int | Unset):
        warnings (list[str] | Unset):
        places_upserted (int | Unset):
        views_upserted (int | Unset):
        watchlists_upserted (int | Unset):
        push_providers_upserted (int | Unset):
    """

    sources_applied: list[str]
    sources_unknown: list[str]
    triggers_upserted: int | Unset = UNSET
    channels_upserted: int | Unset = UNSET
    briefings_upserted: int | Unset = UNSET
    custom_sources_upserted: int | Unset = UNSET
    llm_providers_upserted: int | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    places_upserted: int | Unset = UNSET
    views_upserted: int | Unset = UNSET
    watchlists_upserted: int | Unset = UNSET
    push_providers_upserted: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources_applied = self.sources_applied

        sources_unknown = self.sources_unknown

        triggers_upserted = self.triggers_upserted

        channels_upserted = self.channels_upserted

        briefings_upserted = self.briefings_upserted

        custom_sources_upserted = self.custom_sources_upserted

        llm_providers_upserted = self.llm_providers_upserted

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        places_upserted = self.places_upserted

        views_upserted = self.views_upserted

        watchlists_upserted = self.watchlists_upserted

        push_providers_upserted = self.push_providers_upserted

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourcesApplied": sources_applied,
                "sourcesUnknown": sources_unknown,
            }
        )
        if triggers_upserted is not UNSET:
            field_dict["triggersUpserted"] = triggers_upserted
        if channels_upserted is not UNSET:
            field_dict["channelsUpserted"] = channels_upserted
        if briefings_upserted is not UNSET:
            field_dict["briefingsUpserted"] = briefings_upserted
        if custom_sources_upserted is not UNSET:
            field_dict["customSourcesUpserted"] = custom_sources_upserted
        if llm_providers_upserted is not UNSET:
            field_dict["llmProvidersUpserted"] = llm_providers_upserted
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if places_upserted is not UNSET:
            field_dict["placesUpserted"] = places_upserted
        if views_upserted is not UNSET:
            field_dict["viewsUpserted"] = views_upserted
        if watchlists_upserted is not UNSET:
            field_dict["watchlistsUpserted"] = watchlists_upserted
        if push_providers_upserted is not UNSET:
            field_dict["pushProvidersUpserted"] = push_providers_upserted

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sources_applied = cast(list[str], d.pop("sourcesApplied"))

        sources_unknown = cast(list[str], d.pop("sourcesUnknown"))

        triggers_upserted = d.pop("triggersUpserted", UNSET)

        channels_upserted = d.pop("channelsUpserted", UNSET)

        briefings_upserted = d.pop("briefingsUpserted", UNSET)

        custom_sources_upserted = d.pop("customSourcesUpserted", UNSET)

        llm_providers_upserted = d.pop("llmProvidersUpserted", UNSET)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        places_upserted = d.pop("placesUpserted", UNSET)

        views_upserted = d.pop("viewsUpserted", UNSET)

        watchlists_upserted = d.pop("watchlistsUpserted", UNSET)

        push_providers_upserted = d.pop("pushProvidersUpserted", UNSET)

        import_report = cls(
            sources_applied=sources_applied,
            sources_unknown=sources_unknown,
            triggers_upserted=triggers_upserted,
            channels_upserted=channels_upserted,
            briefings_upserted=briefings_upserted,
            custom_sources_upserted=custom_sources_upserted,
            llm_providers_upserted=llm_providers_upserted,
            warnings=warnings,
            places_upserted=places_upserted,
            views_upserted=views_upserted,
            watchlists_upserted=watchlists_upserted,
            push_providers_upserted=push_providers_upserted,
        )

        import_report.additional_properties = d
        return import_report

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
