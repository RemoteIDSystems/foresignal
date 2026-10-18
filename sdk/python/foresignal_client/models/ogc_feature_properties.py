from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcFeatureProperties")


@_attrs_define
class OgcFeatureProperties:
    """
    Attributes:
        title (None | str | Unset):
        source_id (str | Unset):
        observed_at (datetime.datetime | Unset):
        event_time (datetime.datetime | None | Unset):
        version (int | Unset):
    """

    title: None | str | Unset = UNSET
    source_id: str | Unset = UNSET
    observed_at: datetime.datetime | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    version: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        source_id = self.source_id

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if title is not UNSET:
            field_dict["title"] = title
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        source_id = d.pop("sourceId", UNSET)

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        def _parse_event_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                event_time_type_0 = datetime.datetime.fromisoformat(data)

                return event_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

        version = d.pop("version", UNSET)

        ogc_feature_properties = cls(
            title=title,
            source_id=source_id,
            observed_at=observed_at,
            event_time=event_time,
            version=version,
        )

        ogc_feature_properties.additional_properties = d
        return ogc_feature_properties

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
