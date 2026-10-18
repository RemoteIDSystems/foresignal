from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stream_ticket_purpose import StreamTicketPurpose
from ..types import UNSET, Unset

T = TypeVar("T", bound="StreamTicket")


@_attrs_define
class StreamTicket:
    """A short-lived ticket inheriting the caller's token, scope and restrictions (023)

    Attributes:
        ticket (str): `fst_` + 24 random bytes (base64url)
        purpose (StreamTicketPurpose):
        expires_at (datetime.datetime): Stream: deadline for the first connection (60 s after minting; afterwards
            reconnects work up to 5 min after the last disconnect and 24 h in total). Tiles: end of the ticket's lifetime.
        paths (list[str] | Unset):
    """

    ticket: str
    purpose: StreamTicketPurpose
    expires_at: datetime.datetime
    paths: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ticket = self.ticket

        purpose = self.purpose.value

        expires_at = self.expires_at.isoformat()

        paths: list[str] | Unset = UNSET
        if not isinstance(self.paths, Unset):
            paths = self.paths

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ticket": ticket,
                "purpose": purpose,
                "expiresAt": expires_at,
            }
        )
        if paths is not UNSET:
            field_dict["paths"] = paths

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ticket = d.pop("ticket")

        purpose = StreamTicketPurpose(d.pop("purpose"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        paths = cast(list[str], d.pop("paths", UNSET))

        stream_ticket = cls(
            ticket=ticket,
            purpose=purpose,
            expires_at=expires_at,
            paths=paths,
        )

        stream_ticket.additional_properties = d
        return stream_ticket

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
