from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_thread_item_type import ChronicleThreadItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_thread_item_gap_type_0 import ChronicleThreadItemGapType0
    from ..models.chronicle_thread_item_snapshot_type_0 import (
        ChronicleThreadItemSnapshotType0,
    )
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="ChronicleThreadItem")


@_attrs_define
class ChronicleThreadItem:
    """One notable item in a chronicle's thread (also the payload of SSE `chronicle.item`)

    Attributes:
        id (int):
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ChronicleThreadItemType):
        at (datetime.datetime):
        title (str):
        seq (int | None | Unset):
        layer_id (None | str | Unset):
        summary (None | str | Unset):
        ref (None | str | Unset):
        envelope (None | ObjectEnvelope | Unset):
        snapshot (ChronicleThreadItemSnapshotType0 | None | Unset): Snapshot kept in `chronicle_event` so the item
            survives its owner's retention
        gap (ChronicleThreadItemGapType0 | None | Unset):
        backfilled (bool | Unset):
    """

    id: int
    chronicle_id: str
    type_: ChronicleThreadItemType
    at: datetime.datetime
    title: str
    seq: int | None | Unset = UNSET
    layer_id: None | str | Unset = UNSET
    summary: None | str | Unset = UNSET
    ref: None | str | Unset = UNSET
    envelope: None | ObjectEnvelope | Unset = UNSET
    snapshot: ChronicleThreadItemSnapshotType0 | None | Unset = UNSET
    gap: ChronicleThreadItemGapType0 | None | Unset = UNSET
    backfilled: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_thread_item_gap_type_0 import (
            ChronicleThreadItemGapType0,
        )
        from ..models.chronicle_thread_item_snapshot_type_0 import (
            ChronicleThreadItemSnapshotType0,
        )
        from ..models.object_envelope import ObjectEnvelope

        id = self.id

        chronicle_id = self.chronicle_id

        type_ = self.type_.value

        at = self.at.isoformat()

        title = self.title

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        layer_id: None | str | Unset
        if isinstance(self.layer_id, Unset):
            layer_id = UNSET
        else:
            layer_id = self.layer_id

        summary: None | str | Unset
        if isinstance(self.summary, Unset):
            summary = UNSET
        else:
            summary = self.summary

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

        snapshot: dict[str, Any] | None | Unset
        if isinstance(self.snapshot, Unset):
            snapshot = UNSET
        elif isinstance(self.snapshot, ChronicleThreadItemSnapshotType0):
            snapshot = self.snapshot.to_dict()
        else:
            snapshot = self.snapshot

        gap: dict[str, Any] | None | Unset
        if isinstance(self.gap, Unset):
            gap = UNSET
        elif isinstance(self.gap, ChronicleThreadItemGapType0):
            gap = self.gap.to_dict()
        else:
            gap = self.gap

        backfilled = self.backfilled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "chronicleId": chronicle_id,
                "type": type_,
                "at": at,
                "title": title,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if layer_id is not UNSET:
            field_dict["layerId"] = layer_id
        if summary is not UNSET:
            field_dict["summary"] = summary
        if ref is not UNSET:
            field_dict["ref"] = ref
        if envelope is not UNSET:
            field_dict["envelope"] = envelope
        if snapshot is not UNSET:
            field_dict["snapshot"] = snapshot
        if gap is not UNSET:
            field_dict["gap"] = gap
        if backfilled is not UNSET:
            field_dict["backfilled"] = backfilled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_thread_item_gap_type_0 import (
            ChronicleThreadItemGapType0,
        )
        from ..models.chronicle_thread_item_snapshot_type_0 import (
            ChronicleThreadItemSnapshotType0,
        )
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        id = d.pop("id")

        chronicle_id = d.pop("chronicleId")

        type_ = ChronicleThreadItemType(d.pop("type"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        title = d.pop("title")

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_layer_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        layer_id = _parse_layer_id(d.pop("layerId", UNSET))

        def _parse_summary(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        summary = _parse_summary(d.pop("summary", UNSET))

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

        def _parse_snapshot(
            data: object,
        ) -> ChronicleThreadItemSnapshotType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                snapshot_type_0 = ChronicleThreadItemSnapshotType0.from_dict(data)

                return snapshot_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleThreadItemSnapshotType0 | None | Unset, data)

        snapshot = _parse_snapshot(d.pop("snapshot", UNSET))

        def _parse_gap(data: object) -> ChronicleThreadItemGapType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                gap_type_0 = ChronicleThreadItemGapType0.from_dict(data)

                return gap_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleThreadItemGapType0 | None | Unset, data)

        gap = _parse_gap(d.pop("gap", UNSET))

        backfilled = d.pop("backfilled", UNSET)

        chronicle_thread_item = cls(
            id=id,
            chronicle_id=chronicle_id,
            type_=type_,
            at=at,
            title=title,
            seq=seq,
            layer_id=layer_id,
            summary=summary,
            ref=ref,
            envelope=envelope,
            snapshot=snapshot,
            gap=gap,
            backfilled=backfilled,
        )

        chronicle_thread_item.additional_properties = d
        return chronicle_thread_item

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
