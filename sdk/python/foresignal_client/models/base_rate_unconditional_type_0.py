from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="BaseRateUnconditionalType0")


@_attrs_define
class BaseRateUnconditionalType0:
    """Unconditional rate alongside a conditional one

    Attributes:
        rate_per_window (float): Mean events per window
        p_at_least_one (float):
        interval (StatInterval): A two-sided statistical interval for a rate, proportion or difference
        n_windows (int | Unset): Windows behind the unconditional rate
    """

    rate_per_window: float
    p_at_least_one: float
    interval: StatInterval
    n_windows: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rate_per_window = self.rate_per_window

        p_at_least_one = self.p_at_least_one

        interval = self.interval.to_dict()

        n_windows = self.n_windows

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ratePerWindow": rate_per_window,
                "pAtLeastOne": p_at_least_one,
                "interval": interval,
            }
        )
        if n_windows is not UNSET:
            field_dict["nWindows"] = n_windows

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)
        rate_per_window = d.pop("ratePerWindow")

        p_at_least_one = d.pop("pAtLeastOne")

        interval = StatInterval.from_dict(d.pop("interval"))

        n_windows = d.pop("nWindows", UNSET)

        base_rate_unconditional_type_0 = cls(
            rate_per_window=rate_per_window,
            p_at_least_one=p_at_least_one,
            interval=interval,
            n_windows=n_windows,
        )

        base_rate_unconditional_type_0.additional_properties = d
        return base_rate_unconditional_type_0

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
