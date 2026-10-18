from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.investigation_item_item_type import InvestigationItemItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.investigation_item_snapshot_type_0 import (
        InvestigationItemSnapshotType0,
    )


T = TypeVar("T", bound="InvestigationItem")


@_attrs_define
class InvestigationItem:
    """An item pinned in an investigation with a snapshot taken when added, so evidence survives retention

    Attributes:
        id (str):
        item_type (InvestigationItemItemType):
        ref (str): Item reference: event id, alert id, `{sourceId}/{externalId}`, anomaly id, place id, briefing run id;
            129 ObjectRef grammar accepted from 129 on Example: usgs-earthquakes/us7000abcd.
        added_at (datetime.datetime):
        added_by (str):
        seq (int | None | Unset): Pinned version seq (records)
        snapshot (InvestigationItemSnapshotType0 | None | Unset): ≤ 64 KB, taken when added
        note (None | str | Unset):
        object_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    id: str
    item_type: InvestigationItemItemType
    ref: str
    added_at: datetime.datetime
    added_by: str
    seq: int | None | Unset = UNSET
    snapshot: InvestigationItemSnapshotType0 | None | Unset = UNSET
    note: None | str | Unset = UNSET
    object_ref: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.investigation_item_snapshot_type_0 import (
            InvestigationItemSnapshotType0,
        )

        id = self.id

        item_type = self.item_type.value

        ref = self.ref

        added_at = self.added_at.isoformat()

        added_by = self.added_by

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        snapshot: dict[str, Any] | None | Unset
        if isinstance(self.snapshot, Unset):
            snapshot = UNSET
        elif isinstance(self.snapshot, InvestigationItemSnapshotType0):
            snapshot = self.snapshot.to_dict()
        else:
            snapshot = self.snapshot

        note: None | str | Unset
        if isinstance(self.note, Unset):
            note = UNSET
        else:
            note = self.note

        object_ref = self.object_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "itemType": item_type,
                "ref": ref,
                "addedAt": added_at,
                "addedBy": added_by,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if snapshot is not UNSET:
            field_dict["snapshot"] = snapshot
        if note is not UNSET:
            field_dict["note"] = note
        if object_ref is not UNSET:
            field_dict["objectRef"] = object_ref

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.investigation_item_snapshot_type_0 import (
            InvestigationItemSnapshotType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        item_type = InvestigationItemItemType(d.pop("itemType"))

        ref = d.pop("ref")

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        added_by = d.pop("addedBy")

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        def _parse_snapshot(
            data: object,
        ) -> InvestigationItemSnapshotType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                snapshot_type_0 = InvestigationItemSnapshotType0.from_dict(data)

                return snapshot_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(InvestigationItemSnapshotType0 | None | Unset, data)

        snapshot = _parse_snapshot(d.pop("snapshot", UNSET))

        def _parse_note(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        note = _parse_note(d.pop("note", UNSET))

        object_ref = d.pop("objectRef", UNSET)

        investigation_item = cls(
            id=id,
            item_type=item_type,
            ref=ref,
            added_at=added_at,
            added_by=added_by,
            seq=seq,
            snapshot=snapshot,
            note=note,
            object_ref=object_ref,
        )

        investigation_item.additional_properties = d
        return investigation_item

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
