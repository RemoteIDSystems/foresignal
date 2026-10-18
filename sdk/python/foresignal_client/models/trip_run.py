from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.trip_run_status import TripRunStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.trip_hazard import TripHazard


T = TypeVar("T", bound="TripRun")


@_attrs_define
class TripRun:
    """A pre-trip check run (114)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        occurrence (datetime.datetime):
        status (TripRunStatus):
        clear (bool): No hazards found ('route clear')
        hazards (list[TripHazard]):
        created_at (datetime.datetime):
        briefing (str | Unset): Markdown trip briefing (029 digest renderer)
        delivered_at (datetime.datetime | None | Unset):
    """

    id: str
    trip_id: str
    occurrence: datetime.datetime
    status: TripRunStatus
    clear: bool
    hazards: list[TripHazard]
    created_at: datetime.datetime
    briefing: str | Unset = UNSET
    delivered_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        trip_id = self.trip_id

        occurrence = self.occurrence.isoformat()

        status = self.status.value

        clear = self.clear

        hazards = []
        for hazards_item_data in self.hazards:
            hazards_item = hazards_item_data.to_dict()
            hazards.append(hazards_item)

        created_at = self.created_at.isoformat()

        briefing = self.briefing

        delivered_at: None | str | Unset
        if isinstance(self.delivered_at, Unset):
            delivered_at = UNSET
        elif isinstance(self.delivered_at, datetime.datetime):
            delivered_at = self.delivered_at.isoformat()
        else:
            delivered_at = self.delivered_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "tripId": trip_id,
                "occurrence": occurrence,
                "status": status,
                "clear": clear,
                "hazards": hazards,
                "createdAt": created_at,
            }
        )
        if briefing is not UNSET:
            field_dict["briefing"] = briefing
        if delivered_at is not UNSET:
            field_dict["deliveredAt"] = delivered_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.trip_hazard import TripHazard

        d = dict(src_dict)
        id = d.pop("id")

        trip_id = d.pop("tripId")

        occurrence = datetime.datetime.fromisoformat(d.pop("occurrence"))

        status = TripRunStatus(d.pop("status"))

        clear = d.pop("clear")

        hazards = []
        _hazards = d.pop("hazards")
        for hazards_item_data in _hazards:
            hazards_item = TripHazard.from_dict(hazards_item_data)

            hazards.append(hazards_item)

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        briefing = d.pop("briefing", UNSET)

        def _parse_delivered_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                delivered_at_type_0 = datetime.datetime.fromisoformat(data)

                return delivered_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        delivered_at = _parse_delivered_at(d.pop("deliveredAt", UNSET))

        trip_run = cls(
            id=id,
            trip_id=trip_id,
            occurrence=occurrence,
            status=status,
            clear=clear,
            hazards=hazards,
            created_at=created_at,
            briefing=briefing,
            delivered_at=delivered_at,
        )

        trip_run.additional_properties = d
        return trip_run

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
