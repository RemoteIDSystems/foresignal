from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stream_ticket_request_purpose import StreamTicketRequestPurpose
from ..types import UNSET, Unset

T = TypeVar("T", bound="StreamTicketRequest")


@_attrs_define
class StreamTicketRequest:
    """Request for a stream or tile ticket (023)

    Attributes:
        purpose (StreamTicketRequestPurpose):
        paths (list[str] | Unset): Optional narrower set of paths (within the purpose's paths) the ticket may be used on
        ttl (str | Unset): Tile tickets only: lifetime, default 10m, max 1h (ISO-8601 or shorthand) Example: 10m.
    """

    purpose: StreamTicketRequestPurpose
    paths: list[str] | Unset = UNSET
    ttl: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        purpose = self.purpose.value

        paths: list[str] | Unset = UNSET
        if not isinstance(self.paths, Unset):
            paths = self.paths

        ttl = self.ttl

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "purpose": purpose,
            }
        )
        if paths is not UNSET:
            field_dict["paths"] = paths
        if ttl is not UNSET:
            field_dict["ttl"] = ttl

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        purpose = StreamTicketRequestPurpose(d.pop("purpose"))

        paths = cast(list[str], d.pop("paths", UNSET))

        ttl = d.pop("ttl", UNSET)

        stream_ticket_request = cls(
            purpose=purpose,
            paths=paths,
            ttl=ttl,
        )

        stream_ticket_request.additional_properties = d
        return stream_ticket_request

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
