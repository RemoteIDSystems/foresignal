from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorWindowStatsReverseType0")


@_attrs_define
class PrecursorWindowStatsReverseType0:
    """Reverse-direction test (consequent leading)

    Attributes:
        co_movement (bool): True when the reverse is as strong: co-movement, not precedence
        p (float | None | Unset):
        statistic (float | None | Unset):
    """

    co_movement: bool
    p: float | None | Unset = UNSET
    statistic: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        co_movement = self.co_movement

        p: float | None | Unset
        if isinstance(self.p, Unset):
            p = UNSET
        else:
            p = self.p

        statistic: float | None | Unset
        if isinstance(self.statistic, Unset):
            statistic = UNSET
        else:
            statistic = self.statistic

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "coMovement": co_movement,
            }
        )
        if p is not UNSET:
            field_dict["p"] = p
        if statistic is not UNSET:
            field_dict["statistic"] = statistic

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        co_movement = d.pop("coMovement")

        def _parse_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p = _parse_p(d.pop("p", UNSET))

        def _parse_statistic(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        statistic = _parse_statistic(d.pop("statistic", UNSET))

        precursor_window_stats_reverse_type_0 = cls(
            co_movement=co_movement,
            p=p,
            statistic=statistic,
        )

        precursor_window_stats_reverse_type_0.additional_properties = d
        return precursor_window_stats_reverse_type_0

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
