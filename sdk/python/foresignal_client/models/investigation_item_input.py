from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.investigation_item_input_item_type import InvestigationItemInputItemType
from ..types import UNSET, Unset

T = TypeVar("T", bound="InvestigationItemInput")


@_attrs_define
class InvestigationItemInput:
    """Item to add; adding an alert sets its 017 state to `investigating` when that state exists; 130: evidence items,
    attachments and any object reference, snapshotted from the envelope, raising the investigation TLP to the strictest
    item label

        Attributes:
            item_type (InvestigationItemInputItemType):
            ref (str): Item reference; for evidence-item, attachment and object an object reference (or the bare id of an
                evidence item or attachment), which the caller must be able to see
            seq (int | Unset): Pin a record version; default the current version
            note (str | Unset):
    """

    item_type: InvestigationItemInputItemType
    ref: str
    seq: int | Unset = UNSET
    note: str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        item_type = self.item_type.value

        ref = self.ref

        seq = self.seq

        note = self.note

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "itemType": item_type,
                "ref": ref,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        item_type = InvestigationItemInputItemType(d.pop("itemType"))

        ref = d.pop("ref")

        seq = d.pop("seq", UNSET)

        note = d.pop("note", UNSET)

        investigation_item_input = cls(
            item_type=item_type,
            ref=ref,
            seq=seq,
            note=note,
        )

        return investigation_item_input
