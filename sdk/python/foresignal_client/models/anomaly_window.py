from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_window_bucket import AnomalyWindowBucket
from ..models.anomaly_window_direction import AnomalyWindowDirection
from ..models.anomaly_window_sensitivity import AnomalyWindowSensitivity
from ..models.anomaly_window_slot_mode import AnomalyWindowSlotMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnomalyWindow")


@_attrs_define
class AnomalyWindow:
    """Anomaly rule window: per-bucket counts against a coverage-aware Poisson or negative-binomial baseline

    Attributes:
        window (str): Window length, 1m to 7d. ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 30m.
        step (str | Unset): Evaluation period; default min(window/4, 5m), minimum 30s. ISO-8601 duration or shorthand
            (30m, 6h, 7d).
        distinct_by (str | Unset): `entity` (098 entity, else descriptor entityKeys, else source+externalId),
            `externalId`, or a payload path. Versions are never counted. Default: 'entity'.
        group_by (None | str | Unset): null | `source` | `place` | `h3:<res 3..8>` | `admin1` | `country` | a payload
            path Example: place.
        max_groups (int | Unset):  Default: 500.
        bucket (AnomalyWindowBucket | Unset):  Default: AnomalyWindowBucket.VALUE_1.
        slot_mode (AnomalyWindowSlotMode | Unset):  Default: AnomalyWindowSlotMode.HOUR_OF_WEEK.
        direction (AnomalyWindowDirection | Unset):  Default: AnomalyWindowDirection.UP.
        sensitivity (AnomalyWindowSensitivity | Unset): α 1e-4 / 1e-3 / 1e-2 Default: AnomalyWindowSensitivity.MEDIUM.
        min_absolute_delta (float | Unset):  Default: 3.0.
        min_weeks (int | Unset):  Default: 8.
        coverage_min (float | Unset):  Default: 0.8.
    """

    window: str
    step: str | Unset = UNSET
    distinct_by: str | Unset = "entity"
    group_by: None | str | Unset = UNSET
    max_groups: int | Unset = 500
    bucket: AnomalyWindowBucket | Unset = AnomalyWindowBucket.VALUE_1
    slot_mode: AnomalyWindowSlotMode | Unset = AnomalyWindowSlotMode.HOUR_OF_WEEK
    direction: AnomalyWindowDirection | Unset = AnomalyWindowDirection.UP
    sensitivity: AnomalyWindowSensitivity | Unset = AnomalyWindowSensitivity.MEDIUM
    min_absolute_delta: float | Unset = 3.0
    min_weeks: int | Unset = 8
    coverage_min: float | Unset = 0.8
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        window = self.window

        step = self.step

        distinct_by = self.distinct_by

        group_by: None | str | Unset
        if isinstance(self.group_by, Unset):
            group_by = UNSET
        else:
            group_by = self.group_by

        max_groups = self.max_groups

        bucket: str | Unset = UNSET
        if not isinstance(self.bucket, Unset):
            bucket = self.bucket.value

        slot_mode: str | Unset = UNSET
        if not isinstance(self.slot_mode, Unset):
            slot_mode = self.slot_mode.value

        direction: str | Unset = UNSET
        if not isinstance(self.direction, Unset):
            direction = self.direction.value

        sensitivity: str | Unset = UNSET
        if not isinstance(self.sensitivity, Unset):
            sensitivity = self.sensitivity.value

        min_absolute_delta = self.min_absolute_delta

        min_weeks = self.min_weeks

        coverage_min = self.coverage_min

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "window": window,
            }
        )
        if step is not UNSET:
            field_dict["step"] = step
        if distinct_by is not UNSET:
            field_dict["distinctBy"] = distinct_by
        if group_by is not UNSET:
            field_dict["groupBy"] = group_by
        if max_groups is not UNSET:
            field_dict["maxGroups"] = max_groups
        if bucket is not UNSET:
            field_dict["bucket"] = bucket
        if slot_mode is not UNSET:
            field_dict["slotMode"] = slot_mode
        if direction is not UNSET:
            field_dict["direction"] = direction
        if sensitivity is not UNSET:
            field_dict["sensitivity"] = sensitivity
        if min_absolute_delta is not UNSET:
            field_dict["minAbsoluteDelta"] = min_absolute_delta
        if min_weeks is not UNSET:
            field_dict["minWeeks"] = min_weeks
        if coverage_min is not UNSET:
            field_dict["coverageMin"] = coverage_min

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        window = d.pop("window")

        step = d.pop("step", UNSET)

        distinct_by = d.pop("distinctBy", UNSET)

        def _parse_group_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        group_by = _parse_group_by(d.pop("groupBy", UNSET))

        max_groups = d.pop("maxGroups", UNSET)

        _bucket = d.pop("bucket", UNSET)
        bucket: AnomalyWindowBucket | Unset
        if isinstance(_bucket, Unset):
            bucket = UNSET
        else:
            bucket = AnomalyWindowBucket(_bucket)

        _slot_mode = d.pop("slotMode", UNSET)
        slot_mode: AnomalyWindowSlotMode | Unset
        if isinstance(_slot_mode, Unset):
            slot_mode = UNSET
        else:
            slot_mode = AnomalyWindowSlotMode(_slot_mode)

        _direction = d.pop("direction", UNSET)
        direction: AnomalyWindowDirection | Unset
        if isinstance(_direction, Unset):
            direction = UNSET
        else:
            direction = AnomalyWindowDirection(_direction)

        _sensitivity = d.pop("sensitivity", UNSET)
        sensitivity: AnomalyWindowSensitivity | Unset
        if isinstance(_sensitivity, Unset):
            sensitivity = UNSET
        else:
            sensitivity = AnomalyWindowSensitivity(_sensitivity)

        min_absolute_delta = d.pop("minAbsoluteDelta", UNSET)

        min_weeks = d.pop("minWeeks", UNSET)

        coverage_min = d.pop("coverageMin", UNSET)

        anomaly_window = cls(
            window=window,
            step=step,
            distinct_by=distinct_by,
            group_by=group_by,
            max_groups=max_groups,
            bucket=bucket,
            slot_mode=slot_mode,
            direction=direction,
            sensitivity=sensitivity,
            min_absolute_delta=min_absolute_delta,
            min_weeks=min_weeks,
            coverage_min=coverage_min,
        )

        anomaly_window.additional_properties = d
        return anomaly_window

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
