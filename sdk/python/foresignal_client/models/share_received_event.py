from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.share_received_event_role import ShareReceivedEventRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ShareReceivedEvent")


@_attrs_define
class ShareReceivedEvent:
    """SSE `share.received` on the `me` topic: an object was shared with the caller

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        role (ShareReceivedEventRole):
        shared_by (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        at (datetime.datetime):
        envelope (ObjectEnvelope | Unset): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        message (None | str | Unset):
    """

    ref: str
    role: ShareReceivedEventRole
    shared_by: PrincipalSummary
    at: datetime.datetime
    envelope: ObjectEnvelope | Unset = UNSET
    message: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        role = self.role.value

        shared_by = self.shared_by.to_dict()

        at = self.at.isoformat()

        envelope: dict[str, Any] | Unset = UNSET
        if not isinstance(self.envelope, Unset):
            envelope = self.envelope.to_dict()

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "role": role,
                "sharedBy": shared_by,
                "at": at,
            }
        )
        if envelope is not UNSET:
            field_dict["envelope"] = envelope
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        ref = d.pop("ref")

        role = ShareReceivedEventRole(d.pop("role"))

        shared_by = PrincipalSummary.from_dict(d.pop("sharedBy"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        _envelope = d.pop("envelope", UNSET)
        envelope: ObjectEnvelope | Unset
        if isinstance(_envelope, Unset):
            envelope = UNSET
        else:
            envelope = ObjectEnvelope.from_dict(_envelope)

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        share_received_event = cls(
            ref=ref,
            role=role,
            shared_by=shared_by,
            at=at,
            envelope=envelope,
            message=message,
        )

        share_received_event.additional_properties = d
        return share_received_event

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
