from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ingest_policy_all_mode import IngestPolicyAllMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestPolicyAll")


@_attrs_define
class IngestPolicyAll:
    """ALL policy: every full-hash change is persisted (CHANGE), plus keyframes

    Attributes:
        mode (IngestPolicyAllMode):
        keyframe (str | Unset): Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL).
            ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 240s.
    """

    mode: IngestPolicyAllMode
    keyframe: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        keyframe = self.keyframe

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
            }
        )
        if keyframe is not UNSET:
            field_dict["keyframe"] = keyframe

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mode = IngestPolicyAllMode(d.pop("mode"))

        keyframe = d.pop("keyframe", UNSET)

        ingest_policy_all = cls(
            mode=mode,
            keyframe=keyframe,
        )

        ingest_policy_all.additional_properties = d
        return ingest_policy_all

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
