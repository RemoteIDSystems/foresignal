from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.inbox_item_category import InboxItemCategory
from ..models.severity import Severity
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="InboxItem")


@_attrs_define
class InboxItem:
    """One item in the caller's personal inbox (mentions, shares, received reports, chronicle proposals, assessments,
    replies)

        Attributes:
            id (int):
            event (str): Event type, e.g. `mention`, `share.received`, `report.received`, `package.received`,
                `chronicle.proposed`, `chronicle.cap_warning`, `chronicle.gap`, `chronicle.link.found`,
                `chronicle.access.requested`, `chronicle.access.granted`, `chronicle.access.declined`,
                `chronicle.discoverability_lowered`, `saved-search.matches`, `assessment.published`, `comment.reply`
            category (InboxItemCategory):
            title (str):
            created_at (datetime.datetime):
            read (bool):
            severity (Severity | Unset):
            body (None | str | Unset):
            ref (None | str | Unset):
            envelope (None | ObjectEnvelope | Unset):
            actor (None | PrincipalSummary | Unset):
            tlp (None | Tlp | Unset):
    """

    id: int
    event: str
    category: InboxItemCategory
    title: str
    created_at: datetime.datetime
    read: bool
    severity: Severity | Unset = UNSET
    body: None | str | Unset = UNSET
    ref: None | str | Unset = UNSET
    envelope: None | ObjectEnvelope | Unset = UNSET
    actor: None | PrincipalSummary | Unset = UNSET
    tlp: None | Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.principal_summary import PrincipalSummary

        id = self.id

        event = self.event

        category = self.category.value

        title = self.title

        created_at = self.created_at.isoformat()

        read = self.read

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        body: None | str | Unset
        if isinstance(self.body, Unset):
            body = UNSET
        else:
            body = self.body

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        envelope: dict[str, Any] | None | Unset
        if isinstance(self.envelope, Unset):
            envelope = UNSET
        elif isinstance(self.envelope, ObjectEnvelope):
            envelope = self.envelope.to_dict()
        else:
            envelope = self.envelope

        actor: dict[str, Any] | None | Unset
        if isinstance(self.actor, Unset):
            actor = UNSET
        elif isinstance(self.actor, PrincipalSummary):
            actor = self.actor.to_dict()
        else:
            actor = self.actor

        tlp: None | str | Unset
        if isinstance(self.tlp, Unset):
            tlp = UNSET
        elif isinstance(self.tlp, Tlp):
            tlp = self.tlp.value
        else:
            tlp = self.tlp

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "event": event,
                "category": category,
                "title": title,
                "createdAt": created_at,
                "read": read,
            }
        )
        if severity is not UNSET:
            field_dict["severity"] = severity
        if body is not UNSET:
            field_dict["body"] = body
        if ref is not UNSET:
            field_dict["ref"] = ref
        if envelope is not UNSET:
            field_dict["envelope"] = envelope
        if actor is not UNSET:
            field_dict["actor"] = actor
        if tlp is not UNSET:
            field_dict["tlp"] = tlp

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        id = d.pop("id")

        event = d.pop("event")

        category = InboxItemCategory(d.pop("category"))

        title = d.pop("title")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        read = d.pop("read")

        _severity = d.pop("severity", UNSET)
        severity: Severity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = Severity(_severity)

        def _parse_body(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body = _parse_body(d.pop("body", UNSET))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_envelope(data: object) -> None | ObjectEnvelope | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                envelope_type_0 = ObjectEnvelope.from_dict(data)

                return envelope_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ObjectEnvelope | Unset, data)

        envelope = _parse_envelope(d.pop("envelope", UNSET))

        def _parse_actor(data: object) -> None | PrincipalSummary | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                actor_type_0 = PrincipalSummary.from_dict(data)

                return actor_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrincipalSummary | Unset, data)

        actor = _parse_actor(d.pop("actor", UNSET))

        def _parse_tlp(data: object) -> None | Tlp | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                tlp_type_0 = Tlp(data)

                return tlp_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Tlp | Unset, data)

        tlp = _parse_tlp(d.pop("tlp", UNSET))

        inbox_item = cls(
            id=id,
            event=event,
            category=category,
            title=title,
            created_at=created_at,
            read=read,
            severity=severity,
            body=body,
            ref=ref,
            envelope=envelope,
            actor=actor,
            tlp=tlp,
        )

        inbox_item.additional_properties = d
        return inbox_item

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
