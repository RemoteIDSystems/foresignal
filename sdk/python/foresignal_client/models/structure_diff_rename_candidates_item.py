from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.structure_diff_rename_candidates_item_reason import (
    StructureDiffRenameCandidatesItemReason,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="StructureDiffRenameCandidatesItem")


@_attrs_define
class StructureDiffRenameCandidatesItem:
    """Removed/added path pair under the same parent

    Attributes:
        from_ (str):
        to (str):
        score (float):
        reason (StructureDiffRenameCandidatesItemReason):
        read_by_parser (bool | Unset):
    """

    from_: str
    to: str
    score: float
    reason: StructureDiffRenameCandidatesItemReason
    read_by_parser: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_

        to = self.to

        score = self.score

        reason = self.reason.value

        read_by_parser = self.read_by_parser

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "score": score,
                "reason": reason,
            }
        )
        if read_by_parser is not UNSET:
            field_dict["readByParser"] = read_by_parser

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_ = d.pop("from")

        to = d.pop("to")

        score = d.pop("score")

        reason = StructureDiffRenameCandidatesItemReason(d.pop("reason"))

        read_by_parser = d.pop("readByParser", UNSET)

        structure_diff_rename_candidates_item = cls(
            from_=from_,
            to=to,
            score=score,
            reason=reason,
            read_by_parser=read_by_parser,
        )

        structure_diff_rename_candidates_item.additional_properties = d
        return structure_diff_rename_candidates_item

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
