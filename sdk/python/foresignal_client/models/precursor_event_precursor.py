from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_event_precursor_kind import PrecursorEventPrecursorKind
from ..models.precursor_event_precursor_status import PrecursorEventPrecursorStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorEventPrecursor")


@_attrs_define
class PrecursorEventPrecursor:
    """Precursor summary

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (PrecursorEventPrecursorKind):
        status (PrecursorEventPrecursorStatus):
        description (str):
        status_reason (None | str | Unset):
        lift (float | None | Unset):
    """

    id: str
    kind: PrecursorEventPrecursorKind
    status: PrecursorEventPrecursorStatus
    description: str
    status_reason: None | str | Unset = UNSET
    lift: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        kind = self.kind.value

        status = self.status.value

        description = self.description

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        lift: float | None | Unset
        if isinstance(self.lift, Unset):
            lift = UNSET
        else:
            lift = self.lift

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "status": status,
                "description": description,
            }
        )
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if lift is not UNSET:
            field_dict["lift"] = lift

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        kind = PrecursorEventPrecursorKind(d.pop("kind"))

        status = PrecursorEventPrecursorStatus(d.pop("status"))

        description = d.pop("description")

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        def _parse_lift(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lift = _parse_lift(d.pop("lift", UNSET))

        precursor_event_precursor = cls(
            id=id,
            kind=kind,
            status=status,
            description=description,
            status_reason=status_reason,
            lift=lift,
        )

        precursor_event_precursor.additional_properties = d
        return precursor_event_precursor

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
