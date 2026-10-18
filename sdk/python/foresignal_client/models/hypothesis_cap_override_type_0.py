from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="HypothesisCapOverrideType0")


@_attrs_define
class HypothesisCapOverrideType0:
    """Logged operator override of the cap

    Attributes:
        by (str):
        at (datetime.datetime):
        rationale (str):
    """

    by: str
    at: datetime.datetime
    rationale: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        by = self.by

        at = self.at.isoformat()

        rationale = self.rationale

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "by": by,
                "at": at,
                "rationale": rationale,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        by = d.pop("by")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        rationale = d.pop("rationale")

        hypothesis_cap_override_type_0 = cls(
            by=by,
            at=at,
            rationale=rationale,
        )

        hypothesis_cap_override_type_0.additional_properties = d
        return hypothesis_cap_override_type_0

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
