from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_comparison_contradiction_type_0_reason import (
    SocialComparisonContradictionType0Reason,
)

T = TypeVar("T", bound="SocialComparisonContradictionType0")


@_attrs_define
class SocialComparisonContradictionType0:
    """Why the claim is contradicted

    Attributes:
        reason (SocialComparisonContradictionType0Reason):
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    reason: SocialComparisonContradictionType0Reason
    ref: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reason = self.reason.value

        ref = self.ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reason": reason,
                "ref": ref,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        reason = SocialComparisonContradictionType0Reason(d.pop("reason"))

        ref = d.pop("ref")

        social_comparison_contradiction_type_0 = cls(
            reason=reason,
            ref=ref,
        )

        social_comparison_contradiction_type_0.additional_properties = d
        return social_comparison_contradiction_type_0

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
