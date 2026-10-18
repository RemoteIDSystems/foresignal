from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialVolumePoint")


@_attrs_define
class SocialVolumePoint:
    """One interval of a mention-volume series against its seasonal baseline

    Attributes:
        t (datetime.datetime):
        observed (float):
        complete (bool): Collection was complete for the interval
        baseline (float | None | Unset): Hour-of-week median over the trailing 8 weeks, scaled by the platform-language
            denominator
        mad (float | None | Unset):
        velocity_ratio (float | None | Unset): observed / baseline
        z_robust (float | None | Unset):
    """

    t: datetime.datetime
    observed: float
    complete: bool
    baseline: float | None | Unset = UNSET
    mad: float | None | Unset = UNSET
    velocity_ratio: float | None | Unset = UNSET
    z_robust: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        observed = self.observed

        complete = self.complete

        baseline: float | None | Unset
        if isinstance(self.baseline, Unset):
            baseline = UNSET
        else:
            baseline = self.baseline

        mad: float | None | Unset
        if isinstance(self.mad, Unset):
            mad = UNSET
        else:
            mad = self.mad

        velocity_ratio: float | None | Unset
        if isinstance(self.velocity_ratio, Unset):
            velocity_ratio = UNSET
        else:
            velocity_ratio = self.velocity_ratio

        z_robust: float | None | Unset
        if isinstance(self.z_robust, Unset):
            z_robust = UNSET
        else:
            z_robust = self.z_robust

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "observed": observed,
                "complete": complete,
            }
        )
        if baseline is not UNSET:
            field_dict["baseline"] = baseline
        if mad is not UNSET:
            field_dict["mad"] = mad
        if velocity_ratio is not UNSET:
            field_dict["velocityRatio"] = velocity_ratio
        if z_robust is not UNSET:
            field_dict["zRobust"] = z_robust

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        observed = d.pop("observed")

        complete = d.pop("complete")

        def _parse_baseline(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        baseline = _parse_baseline(d.pop("baseline", UNSET))

        def _parse_mad(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mad = _parse_mad(d.pop("mad", UNSET))

        def _parse_velocity_ratio(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        velocity_ratio = _parse_velocity_ratio(d.pop("velocityRatio", UNSET))

        def _parse_z_robust(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        z_robust = _parse_z_robust(d.pop("zRobust", UNSET))

        social_volume_point = cls(
            t=t,
            observed=observed,
            complete=complete,
            baseline=baseline,
            mad=mad,
            velocity_ratio=velocity_ratio,
            z_robust=z_robust,
        )

        social_volume_point.additional_properties = d
        return social_volume_point

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
