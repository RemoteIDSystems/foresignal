from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ArchiveRunRequest")


@_attrs_define
class ArchiveRunRequest:
    """Run an archival pass now

    Attributes:
        cutoff (datetime.datetime | None | Unset): Override the configured cutoff; archive STANDARD data older than this
            (must be in the past)
        sources (list[str] | Unset): Restrict to these sources, each of which the archive must cover (400 otherwise);
            empty uses the configured allowlist
        dry_run (bool | Unset): Count what would be archived without exporting or purging; the answer is an Archive with
            status `pending` and `itemCount`, not stored Default: False.
    """

    cutoff: datetime.datetime | None | Unset = UNSET
    sources: list[str] | Unset = UNSET
    dry_run: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cutoff: None | str | Unset
        if isinstance(self.cutoff, Unset):
            cutoff = UNSET
        elif isinstance(self.cutoff, datetime.datetime):
            cutoff = self.cutoff.isoformat()
        else:
            cutoff = self.cutoff

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        dry_run = self.dry_run

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cutoff is not UNSET:
            field_dict["cutoff"] = cutoff
        if sources is not UNSET:
            field_dict["sources"] = sources
        if dry_run is not UNSET:
            field_dict["dryRun"] = dry_run

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_cutoff(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                cutoff_type_0 = datetime.datetime.fromisoformat(data)

                return cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        cutoff = _parse_cutoff(d.pop("cutoff", UNSET))

        sources = cast(list[str], d.pop("sources", UNSET))

        dry_run = d.pop("dryRun", UNSET)

        archive_run_request = cls(
            cutoff=cutoff,
            sources=sources,
            dry_run=dry_run,
        )

        archive_run_request.additional_properties = d
        return archive_run_request

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
