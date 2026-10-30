from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EditionSummary")


@_attrs_define
class EditionSummary:
    """One of the caller's editions: a briefing the caller owns (NP-003)

    Attributes:
        briefing_id (UUID):
        name (str):
        enabled (bool):
        schedule (str): 5-field cron
        timezone (str):
        output_language (None | str | Unset): The language the edition is written and spoken in
        next_run_at (datetime.datetime | None | Unset):
        last_run_at (datetime.datetime | None | Unset):
        last_run_status (None | str | Unset):
        latest_run_id (None | Unset | UUID): The latest completed run, or null before the first
        site_url (None | str | Unset): The edition site (`getEditionSite`), or null when hosting is off
    """

    briefing_id: UUID
    name: str
    enabled: bool
    schedule: str
    timezone: str
    output_language: None | str | Unset = UNSET
    next_run_at: datetime.datetime | None | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    last_run_status: None | str | Unset = UNSET
    latest_run_id: None | Unset | UUID = UNSET
    site_url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        briefing_id = str(self.briefing_id)

        name = self.name

        enabled = self.enabled

        schedule = self.schedule

        timezone = self.timezone

        output_language: None | str | Unset
        if isinstance(self.output_language, Unset):
            output_language = UNSET
        else:
            output_language = self.output_language

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

        latest_run_id: None | str | Unset
        if isinstance(self.latest_run_id, Unset):
            latest_run_id = UNSET
        elif isinstance(self.latest_run_id, UUID):
            latest_run_id = str(self.latest_run_id)
        else:
            latest_run_id = self.latest_run_id

        site_url: None | str | Unset
        if isinstance(self.site_url, Unset):
            site_url = UNSET
        else:
            site_url = self.site_url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "briefingId": briefing_id,
                "name": name,
                "enabled": enabled,
                "schedule": schedule,
                "timezone": timezone,
            }
        )
        if output_language is not UNSET:
            field_dict["outputLanguage"] = output_language
        if next_run_at is not UNSET:
            field_dict["nextRunAt"] = next_run_at
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if last_run_status is not UNSET:
            field_dict["lastRunStatus"] = last_run_status
        if latest_run_id is not UNSET:
            field_dict["latestRunId"] = latest_run_id
        if site_url is not UNSET:
            field_dict["siteUrl"] = site_url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        briefing_id = UUID(d.pop("briefingId"))

        name = d.pop("name")

        enabled = d.pop("enabled")

        schedule = d.pop("schedule")

        timezone = d.pop("timezone")

        def _parse_output_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        output_language = _parse_output_language(d.pop("outputLanguage", UNSET))

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

        def _parse_latest_run_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                latest_run_id_type_0 = UUID(data)

                return latest_run_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        latest_run_id = _parse_latest_run_id(d.pop("latestRunId", UNSET))

        def _parse_site_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        site_url = _parse_site_url(d.pop("siteUrl", UNSET))

        edition_summary = cls(
            briefing_id=briefing_id,
            name=name,
            enabled=enabled,
            schedule=schedule,
            timezone=timezone,
            output_language=output_language,
            next_run_at=next_run_at,
            last_run_at=last_run_at,
            last_run_status=last_run_status,
            latest_run_id=latest_run_id,
            site_url=site_url,
        )

        edition_summary.additional_properties = d
        return edition_summary

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
