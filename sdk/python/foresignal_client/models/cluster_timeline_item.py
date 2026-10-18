from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cluster_timeline_item_type import ClusterTimelineItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="ClusterTimelineItem")


@_attrs_define
class ClusterTimelineItem:
    """One item of a cluster's combined timeline, deduplicated by global evidence key

    Attributes:
        evidence_key (str): Global evidence key, e.g. `ev:{family}:{originator}:{upstreamId}`
        at (datetime.datetime):
        type_ (ClusterTimelineItemType):
        title (str):
        held_by (list[str]): Visible chronicles holding this item
        ref (None | str | Unset):
        envelope (None | ObjectEnvelope | Unset):
    """

    evidence_key: str
    at: datetime.datetime
    type_: ClusterTimelineItemType
    title: str
    held_by: list[str]
    ref: None | str | Unset = UNSET
    envelope: None | ObjectEnvelope | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.object_envelope import ObjectEnvelope

        evidence_key = self.evidence_key

        at = self.at.isoformat()

        type_ = self.type_.value

        title = self.title

        held_by = self.held_by

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "evidenceKey": evidence_key,
                "at": at,
                "type": type_,
                "title": title,
                "heldBy": held_by,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if envelope is not UNSET:
            field_dict["envelope"] = envelope

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        evidence_key = d.pop("evidenceKey")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        type_ = ClusterTimelineItemType(d.pop("type"))

        title = d.pop("title")

        held_by = cast(list[str], d.pop("heldBy"))

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

        cluster_timeline_item = cls(
            evidence_key=evidence_key,
            at=at,
            type_=type_,
            title=title,
            held_by=held_by,
            ref=ref,
            envelope=envelope,
        )

        cluster_timeline_item.additional_properties = d
        return cluster_timeline_item

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
