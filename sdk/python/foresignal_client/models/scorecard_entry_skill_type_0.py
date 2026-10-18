from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="ScorecardEntrySkillType0")


@_attrs_define
class ScorecardEntrySkillType0:
    """Skill 1 − S / S_ref against an out-of-sample reference on the same questions

    Attributes:
        value (float):
        reference (str): Reference forecaster (`base-rate` or `climatology`)
        n_shared (int):
        interval (None | StatInterval | Unset):
    """

    value: float
    reference: str
    n_shared: int
    interval: None | StatInterval | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.stat_interval import StatInterval

        value = self.value

        reference = self.reference

        n_shared = self.n_shared

        interval: dict[str, Any] | None | Unset
        if isinstance(self.interval, Unset):
            interval = UNSET
        elif isinstance(self.interval, StatInterval):
            interval = self.interval.to_dict()
        else:
            interval = self.interval

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "value": value,
                "reference": reference,
                "nShared": n_shared,
            }
        )
        if interval is not UNSET:
            field_dict["interval"] = interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)
        value = d.pop("value")

        reference = d.pop("reference")

        n_shared = d.pop("nShared")

        def _parse_interval(data: object) -> None | StatInterval | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_type_0 = StatInterval.from_dict(data)

                return interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StatInterval | Unset, data)

        interval = _parse_interval(d.pop("interval", UNSET))

        scorecard_entry_skill_type_0 = cls(
            value=value,
            reference=reference,
            n_shared=n_shared,
            interval=interval,
        )

        scorecard_entry_skill_type_0.additional_properties = d
        return scorecard_entry_skill_type_0

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
