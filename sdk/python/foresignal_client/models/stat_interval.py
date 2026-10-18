from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stat_interval_method import StatIntervalMethod

T = TypeVar("T", bound="StatInterval")


@_attrs_define
class StatInterval:
    """A two-sided statistical interval for a rate, proportion or difference

    Attributes:
        lower (float):
        upper (float):
        level (float): Coverage level Default: 0.95.
        method (StatIntervalMethod): How it was computed
    """

    lower: float
    upper: float
    method: StatIntervalMethod
    level: float = 0.95
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lower = self.lower

        upper = self.upper

        level = self.level

        method = self.method.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lower": lower,
                "upper": upper,
                "level": level,
                "method": method,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lower = d.pop("lower")

        upper = d.pop("upper")

        level = d.pop("level")

        method = StatIntervalMethod(d.pop("method"))

        stat_interval = cls(
            lower=lower,
            upper=upper,
            level=level,
            method=method,
        )

        stat_interval.additional_properties = d
        return stat_interval

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
