from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.kinematic_state import KinematicState


T = TypeVar("T", bound="CpaResult")


@_attrs_define
class CpaResult:
    """Closest point of approach under constant velocity (114)

    Attributes:
        a (KinematicState): Kinematic state of a record from head ⋈ live (114)
        b (KinematicState): Kinematic state of a record from head ⋈ live (114)
        t0 (datetime.datetime): Common time max(observedAt)
        tcpa_seconds (float): Clamped to [0, horizon]
        cpa_at (datetime.datetime):
        dcpa_meters (float):
        as_of (datetime.datetime):
        stale (bool):
        vertical_separation_m (float | None | Unset): When both states have altitude
        horizon (str | Unset): Search horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h). Example: 15m.
    """

    a: KinematicState
    b: KinematicState
    t0: datetime.datetime
    tcpa_seconds: float
    cpa_at: datetime.datetime
    dcpa_meters: float
    as_of: datetime.datetime
    stale: bool
    vertical_separation_m: float | None | Unset = UNSET
    horizon: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        a = self.a.to_dict()

        b = self.b.to_dict()

        t0 = self.t0.isoformat()

        tcpa_seconds = self.tcpa_seconds

        cpa_at = self.cpa_at.isoformat()

        dcpa_meters = self.dcpa_meters

        as_of = self.as_of.isoformat()

        stale = self.stale

        vertical_separation_m: float | None | Unset
        if isinstance(self.vertical_separation_m, Unset):
            vertical_separation_m = UNSET
        else:
            vertical_separation_m = self.vertical_separation_m

        horizon = self.horizon

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "a": a,
                "b": b,
                "t0": t0,
                "tcpaSeconds": tcpa_seconds,
                "cpaAt": cpa_at,
                "dcpaMeters": dcpa_meters,
                "asOf": as_of,
                "stale": stale,
            }
        )
        if vertical_separation_m is not UNSET:
            field_dict["verticalSeparationM"] = vertical_separation_m
        if horizon is not UNSET:
            field_dict["horizon"] = horizon

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.kinematic_state import KinematicState

        d = dict(src_dict)
        a = KinematicState.from_dict(d.pop("a"))

        b = KinematicState.from_dict(d.pop("b"))

        t0 = datetime.datetime.fromisoformat(d.pop("t0"))

        tcpa_seconds = d.pop("tcpaSeconds")

        cpa_at = datetime.datetime.fromisoformat(d.pop("cpaAt"))

        dcpa_meters = d.pop("dcpaMeters")

        as_of = datetime.datetime.fromisoformat(d.pop("asOf"))

        stale = d.pop("stale")

        def _parse_vertical_separation_m(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        vertical_separation_m = _parse_vertical_separation_m(
            d.pop("verticalSeparationM", UNSET)
        )

        horizon = d.pop("horizon", UNSET)

        cpa_result = cls(
            a=a,
            b=b,
            t0=t0,
            tcpa_seconds=tcpa_seconds,
            cpa_at=cpa_at,
            dcpa_meters=dcpa_meters,
            as_of=as_of,
            stale=stale,
            vertical_separation_m=vertical_separation_m,
            horizon=horizon,
        )

        cpa_result.additional_properties = d
        return cpa_result

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
