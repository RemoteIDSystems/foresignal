from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.client_presence_kind import ClientPresenceKind
from ..models.client_presence_state import ClientPresenceState
from ..types import UNSET, Unset

T = TypeVar("T", bound="ClientPresence")


@_attrs_define
class ClientPresence:
    """A client's presence, used to defer phone notifications while a desktop is active (033)

    Attributes:
        device_key (str):
        kind (ClientPresenceKind):
        state (ClientPresenceState):
        expires_at (datetime.datetime):
        updated_at (datetime.datetime | Unset):
    """

    device_key: str
    kind: ClientPresenceKind
    state: ClientPresenceState
    expires_at: datetime.datetime
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        device_key = self.device_key

        kind = self.kind.value

        state = self.state.value

        expires_at = self.expires_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "deviceKey": device_key,
                "kind": kind,
                "state": state,
                "expiresAt": expires_at,
            }
        )
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        device_key = d.pop("deviceKey")

        kind = ClientPresenceKind(d.pop("kind"))

        state = ClientPresenceState(d.pop("state"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        client_presence = cls(
            device_key=device_key,
            kind=kind,
            state=state,
            expires_at=expires_at,
            updated_at=updated_at,
        )

        client_presence.additional_properties = d
        return client_presence

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
