from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PolSubjectInputParams")


@_attrs_define
class PolSubjectInputParams:
    """Stay and clustering parameters

    Attributes:
        stay_radius_m (float | Unset):  Default: 100.0.
        min_dwell (str | Unset): Minimum dwell for a stay. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
            Default: '10m'. Example: 10m.
        max_gap (str | Unset): Gap that ends a stay. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
            Default: '30m'. Example: 30m.
        eps (float | Unset): DBSCAN eps in metres Default: 150.0.
        min_pts (int | Unset): DBSCAN minimum stays Default: 3.
        lookback (str | Unset): Nightly recompute window over stored stays. ISO-8601 duration (P7D, PT15M) or shorthand
            (7d, 15m, 12h). Default: '28d'. Example: 28d.
    """

    stay_radius_m: float | Unset = 100.0
    min_dwell: str | Unset = "10m"
    max_gap: str | Unset = "30m"
    eps: float | Unset = 150.0
    min_pts: int | Unset = 3
    lookback: str | Unset = "28d"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stay_radius_m = self.stay_radius_m

        min_dwell = self.min_dwell

        max_gap = self.max_gap

        eps = self.eps

        min_pts = self.min_pts

        lookback = self.lookback

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if stay_radius_m is not UNSET:
            field_dict["stayRadiusM"] = stay_radius_m
        if min_dwell is not UNSET:
            field_dict["minDwell"] = min_dwell
        if max_gap is not UNSET:
            field_dict["maxGap"] = max_gap
        if eps is not UNSET:
            field_dict["eps"] = eps
        if min_pts is not UNSET:
            field_dict["minPts"] = min_pts
        if lookback is not UNSET:
            field_dict["lookback"] = lookback

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        stay_radius_m = d.pop("stayRadiusM", UNSET)

        min_dwell = d.pop("minDwell", UNSET)

        max_gap = d.pop("maxGap", UNSET)

        eps = d.pop("eps", UNSET)

        min_pts = d.pop("minPts", UNSET)

        lookback = d.pop("lookback", UNSET)

        pol_subject_input_params = cls(
            stay_radius_m=stay_radius_m,
            min_dwell=min_dwell,
            max_gap=max_gap,
            eps=eps,
            min_pts=min_pts,
            lookback=lookback,
        )

        pol_subject_input_params.additional_properties = d
        return pol_subject_input_params

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
