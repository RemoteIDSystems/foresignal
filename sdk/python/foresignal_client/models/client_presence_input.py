from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.client_presence_input_kind import ClientPresenceInputKind
from ..models.client_presence_input_state import ClientPresenceInputState
from ..types import UNSET, Unset

T = TypeVar("T", bound="ClientPresenceInput")


@_attrs_define
class ClientPresenceInput:
    """Presence heartbeat from a client (033)

    Attributes:
        kind (ClientPresenceInputKind):
        state (ClientPresenceInputState):
        ttl_seconds (int | Unset):  Default: 120.
    """

    kind: ClientPresenceInputKind
    state: ClientPresenceInputState
    ttl_seconds: int | Unset = 120
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        state = self.state.value

        ttl_seconds = self.ttl_seconds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "state": state,
            }
        )
        if ttl_seconds is not UNSET:
            field_dict["ttlSeconds"] = ttl_seconds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = ClientPresenceInputKind(d.pop("kind"))

        state = ClientPresenceInputState(d.pop("state"))

        ttl_seconds = d.pop("ttlSeconds", UNSET)

        client_presence_input = cls(
            kind=kind,
            state=state,
            ttl_seconds=ttl_seconds,
        )

        client_presence_input.additional_properties = d
        return client_presence_input

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
