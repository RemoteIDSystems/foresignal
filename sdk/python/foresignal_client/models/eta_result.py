from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.eta_result_target import EtaResultTarget
    from ..models.kinematic_state import KinematicState


T = TypeVar("T", bound="EtaResult")


@_attrs_define
class EtaResult:
    """ETA of a record to a target point or place (114)

    Attributes:
        record (KinematicState): Kinematic state of a record from head ⋈ live (114)
        target (EtaResultTarget): Target
        distance_meters (float):
        approaching (bool): bearingDiff ≤ 45° and speed ≥ 0.5 m/s
        as_of (datetime.datetime):
        stale (bool):
        bearing_deg (float | Unset):
        bearing_diff_deg (float | Unset):
        eta_seconds (float | None | Unset):
        eta_at (datetime.datetime | None | Unset):
    """

    record: KinematicState
    target: EtaResultTarget
    distance_meters: float
    approaching: bool
    as_of: datetime.datetime
    stale: bool
    bearing_deg: float | Unset = UNSET
    bearing_diff_deg: float | Unset = UNSET
    eta_seconds: float | None | Unset = UNSET
    eta_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        record = self.record.to_dict()

        target = self.target.to_dict()

        distance_meters = self.distance_meters

        approaching = self.approaching

        as_of = self.as_of.isoformat()

        stale = self.stale

        bearing_deg = self.bearing_deg

        bearing_diff_deg = self.bearing_diff_deg

        eta_seconds: float | None | Unset
        if isinstance(self.eta_seconds, Unset):
            eta_seconds = UNSET
        else:
            eta_seconds = self.eta_seconds

        eta_at: None | str | Unset
        if isinstance(self.eta_at, Unset):
            eta_at = UNSET
        elif isinstance(self.eta_at, datetime.datetime):
            eta_at = self.eta_at.isoformat()
        else:
            eta_at = self.eta_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "record": record,
                "target": target,
                "distanceMeters": distance_meters,
                "approaching": approaching,
                "asOf": as_of,
                "stale": stale,
            }
        )
        if bearing_deg is not UNSET:
            field_dict["bearingDeg"] = bearing_deg
        if bearing_diff_deg is not UNSET:
            field_dict["bearingDiffDeg"] = bearing_diff_deg
        if eta_seconds is not UNSET:
            field_dict["etaSeconds"] = eta_seconds
        if eta_at is not UNSET:
            field_dict["etaAt"] = eta_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.eta_result_target import EtaResultTarget
        from ..models.kinematic_state import KinematicState

        d = dict(src_dict)
        record = KinematicState.from_dict(d.pop("record"))

        target = EtaResultTarget.from_dict(d.pop("target"))

        distance_meters = d.pop("distanceMeters")

        approaching = d.pop("approaching")

        as_of = datetime.datetime.fromisoformat(d.pop("asOf"))

        stale = d.pop("stale")

        bearing_deg = d.pop("bearingDeg", UNSET)

        bearing_diff_deg = d.pop("bearingDiffDeg", UNSET)

        def _parse_eta_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        eta_seconds = _parse_eta_seconds(d.pop("etaSeconds", UNSET))

        def _parse_eta_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                eta_at_type_0 = datetime.datetime.fromisoformat(data)

                return eta_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        eta_at = _parse_eta_at(d.pop("etaAt", UNSET))

        eta_result = cls(
            record=record,
            target=target,
            distance_meters=distance_meters,
            approaching=approaching,
            as_of=as_of,
            stale=stale,
            bearing_deg=bearing_deg,
            bearing_diff_deg=bearing_diff_deg,
            eta_seconds=eta_seconds,
            eta_at=eta_at,
        )

        eta_result.additional_properties = d
        return eta_result

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
