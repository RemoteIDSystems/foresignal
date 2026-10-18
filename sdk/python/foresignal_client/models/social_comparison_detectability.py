from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialComparisonDetectability")


@_attrs_define
class SocialComparisonDetectability:
    """canSeeAbsence for the claim's family, place and window

    Attributes:
        p (float):
        lo (float):
        hi (float):
        methods (list[str]):
        ok (bool):
        reason (None | str | Unset):
    """

    p: float
    lo: float
    hi: float
    methods: list[str]
    ok: bool
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p = self.p

        lo = self.lo

        hi = self.hi

        methods = self.methods

        ok = self.ok

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "p": p,
                "lo": lo,
                "hi": hi,
                "methods": methods,
                "ok": ok,
            }
        )
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        p = d.pop("p")

        lo = d.pop("lo")

        hi = d.pop("hi")

        methods = cast(list[str], d.pop("methods"))

        ok = d.pop("ok")

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        social_comparison_detectability = cls(
            p=p,
            lo=lo,
            hi=hi,
            methods=methods,
            ok=ok,
            reason=reason,
        )

        social_comparison_detectability.additional_properties = d
        return social_comparison_detectability

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
