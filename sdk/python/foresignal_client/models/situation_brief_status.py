from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_brief_status_status import SituationBriefStatusStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationBriefStatus")


@_attrs_define
class SituationBriefStatus:
    """Accepted brief regeneration request

    Attributes:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (SituationBriefStatusStatus):
        requested_at (datetime.datetime):
        previous_brief_generated_at (datetime.datetime | None | Unset):
    """

    situation_id: str
    status: SituationBriefStatusStatus
    requested_at: datetime.datetime
    previous_brief_generated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        situation_id = self.situation_id

        status = self.status.value

        requested_at = self.requested_at.isoformat()

        previous_brief_generated_at: None | str | Unset
        if isinstance(self.previous_brief_generated_at, Unset):
            previous_brief_generated_at = UNSET
        elif isinstance(self.previous_brief_generated_at, datetime.datetime):
            previous_brief_generated_at = self.previous_brief_generated_at.isoformat()
        else:
            previous_brief_generated_at = self.previous_brief_generated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "situationId": situation_id,
                "status": status,
                "requestedAt": requested_at,
            }
        )
        if previous_brief_generated_at is not UNSET:
            field_dict["previousBriefGeneratedAt"] = previous_brief_generated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        situation_id = d.pop("situationId")

        status = SituationBriefStatusStatus(d.pop("status"))

        requested_at = datetime.datetime.fromisoformat(d.pop("requestedAt"))

        def _parse_previous_brief_generated_at(
            data: object,
        ) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_brief_generated_at_type_0 = datetime.datetime.fromisoformat(
                    data
                )

                return previous_brief_generated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        previous_brief_generated_at = _parse_previous_brief_generated_at(
            d.pop("previousBriefGeneratedAt", UNSET)
        )

        situation_brief_status = cls(
            situation_id=situation_id,
            status=status,
            requested_at=requested_at,
            previous_brief_generated_at=previous_brief_generated_at,
        )

        situation_brief_status.additional_properties = d
        return situation_brief_status

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
