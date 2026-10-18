from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPlatformDeletionSync")


@_attrs_define
class SocialPlatformDeletionSync:
    """Deletion sync state

    Attributes:
        enabled (bool):
        last_run_at (datetime.datetime | None | Unset):
        tombstoned_last_30_d (int | Unset):
    """

    enabled: bool
    last_run_at: datetime.datetime | None | Unset = UNSET
    tombstoned_last_30_d: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        tombstoned_last_30_d = self.tombstoned_last_30_d

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
            }
        )
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if tombstoned_last_30_d is not UNSET:
            field_dict["tombstonedLast30d"] = tombstoned_last_30_d

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled")

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

        tombstoned_last_30_d = d.pop("tombstonedLast30d", UNSET)

        social_platform_deletion_sync = cls(
            enabled=enabled,
            last_run_at=last_run_at,
            tombstoned_last_30_d=tombstoned_last_30_d,
        )

        social_platform_deletion_sync.additional_properties = d
        return social_platform_deletion_sync

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
