from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EditionSite")


@_attrs_define
class EditionSite:
    """Where an edition's hosted site is reachable (NP-005). Each URL serves the latest successful run as a standalone HTML
    page.

        Attributes:
            briefing_id (UUID):
            url (str): The canonical site URL (`/v1/editions/{briefingId}/site`)
            feed_key_url (None | str | Unset): The site under the edition's feed key
                (`/v1/k/{key}/editions/{briefingId}/site`, 035), or null when it has none
            share_url (None | str | Unset): The read-only, revocable share link (112) to the site, or null when it has none
            latest_run_id (None | Unset | UUID): The run the site shows, or null before the first successful run
            updated_at (datetime.datetime | None | Unset): When that run finished
    """

    briefing_id: UUID
    url: str
    feed_key_url: None | str | Unset = UNSET
    share_url: None | str | Unset = UNSET
    latest_run_id: None | Unset | UUID = UNSET
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        briefing_id = str(self.briefing_id)

        url = self.url

        feed_key_url: None | str | Unset
        if isinstance(self.feed_key_url, Unset):
            feed_key_url = UNSET
        else:
            feed_key_url = self.feed_key_url

        share_url: None | str | Unset
        if isinstance(self.share_url, Unset):
            share_url = UNSET
        else:
            share_url = self.share_url

        latest_run_id: None | str | Unset
        if isinstance(self.latest_run_id, Unset):
            latest_run_id = UNSET
        elif isinstance(self.latest_run_id, UUID):
            latest_run_id = str(self.latest_run_id)
        else:
            latest_run_id = self.latest_run_id

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "briefingId": briefing_id,
                "url": url,
            }
        )
        if feed_key_url is not UNSET:
            field_dict["feedKeyUrl"] = feed_key_url
        if share_url is not UNSET:
            field_dict["shareUrl"] = share_url
        if latest_run_id is not UNSET:
            field_dict["latestRunId"] = latest_run_id
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        briefing_id = UUID(d.pop("briefingId"))

        url = d.pop("url")

        def _parse_feed_key_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        feed_key_url = _parse_feed_key_url(d.pop("feedKeyUrl", UNSET))

        def _parse_share_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        share_url = _parse_share_url(d.pop("shareUrl", UNSET))

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

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        edition_site = cls(
            briefing_id=briefing_id,
            url=url,
            feed_key_url=feed_key_url,
            share_url=share_url,
            latest_run_id=latest_run_id,
            updated_at=updated_at,
        )

        edition_site.additional_properties = d
        return edition_site

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
