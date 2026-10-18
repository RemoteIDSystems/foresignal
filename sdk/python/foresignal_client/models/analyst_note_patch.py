from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.analyst_note_patch_status import AnalystNotePatchStatus

T = TypeVar("T", bound="AnalystNotePatch")


@_attrs_define
class AnalystNotePatch:
    """Note status change

    Attributes:
        status (AnalystNotePatchStatus):
    """

    status: AnalystNotePatchStatus

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "status": status,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        status = AnalystNotePatchStatus(d.pop("status"))

        analyst_note_patch = cls(
            status=status,
        )

        return analyst_note_patch
