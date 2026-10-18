from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemRastersGlm")


@_attrs_define
class SystemRastersGlm:
    """GLM flash ring and archive (048): flashes in the places-AOI ring (last 60 minutes), archive bytes on disk (counted
    toward the disk quota) and the coverage end of the newest processed GLM file (null before the first one since
    startup)

        Attributes:
            ring_flashes (int | Unset):
            archive_bytes (int | Unset):
            newest_file_at (datetime.datetime | None | Unset):
    """

    ring_flashes: int | Unset = UNSET
    archive_bytes: int | Unset = UNSET
    newest_file_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ring_flashes = self.ring_flashes

        archive_bytes = self.archive_bytes

        newest_file_at: None | str | Unset
        if isinstance(self.newest_file_at, Unset):
            newest_file_at = UNSET
        elif isinstance(self.newest_file_at, datetime.datetime):
            newest_file_at = self.newest_file_at.isoformat()
        else:
            newest_file_at = self.newest_file_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if ring_flashes is not UNSET:
            field_dict["ringFlashes"] = ring_flashes
        if archive_bytes is not UNSET:
            field_dict["archiveBytes"] = archive_bytes
        if newest_file_at is not UNSET:
            field_dict["newestFileAt"] = newest_file_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ring_flashes = d.pop("ringFlashes", UNSET)

        archive_bytes = d.pop("archiveBytes", UNSET)

        def _parse_newest_file_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                newest_file_at_type_0 = datetime.datetime.fromisoformat(data)

                return newest_file_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        newest_file_at = _parse_newest_file_at(d.pop("newestFileAt", UNSET))

        system_rasters_glm = cls(
            ring_flashes=ring_flashes,
            archive_bytes=archive_bytes,
            newest_file_at=newest_file_at,
        )

        system_rasters_glm.additional_properties = d
        return system_rasters_glm

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
