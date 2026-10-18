from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_patch_status import PrecursorPatchStatus

T = TypeVar("T", bound="PrecursorPatch")


@_attrs_define
class PrecursorPatch:
    """JSON merge patch: retire with a reason, or pin as operator-validated

    Attributes:
        status (PrecursorPatchStatus):
        reason (str):
    """

    status: PrecursorPatchStatus
    reason: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
                "reason": reason,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        status = PrecursorPatchStatus(d.pop("status"))

        reason = d.pop("reason")

        precursor_patch = cls(
            status=status,
            reason=reason,
        )

        precursor_patch.additional_properties = d
        return precursor_patch

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
