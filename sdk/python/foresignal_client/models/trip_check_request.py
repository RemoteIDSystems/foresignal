from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TripCheckRequest")


@_attrs_define
class TripCheckRequest:
    """Run the pre-trip check now (114)

    Attributes:
        dry_run (bool | Unset): Return the briefing without delivering Default: False.
        occurrence (datetime.datetime | None | Unset): Occurrence departure time (default the next occurrence)
    """

    dry_run: bool | Unset = False
    occurrence: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        dry_run = self.dry_run

        occurrence: None | str | Unset
        if isinstance(self.occurrence, Unset):
            occurrence = UNSET
        elif isinstance(self.occurrence, datetime.datetime):
            occurrence = self.occurrence.isoformat()
        else:
            occurrence = self.occurrence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if dry_run is not UNSET:
            field_dict["dryRun"] = dry_run
        if occurrence is not UNSET:
            field_dict["occurrence"] = occurrence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        dry_run = d.pop("dryRun", UNSET)

        def _parse_occurrence(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                occurrence_type_0 = datetime.datetime.fromisoformat(data)

                return occurrence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        occurrence = _parse_occurrence(d.pop("occurrence", UNSET))

        trip_check_request = cls(
            dry_run=dry_run,
            occurrence=occurrence,
        )

        trip_check_request.additional_properties = d
        return trip_check_request

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
