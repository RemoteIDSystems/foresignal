from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AbsenceWindow")


@_attrs_define
class AbsenceWindow:
    """Absence rule window: fires when nothing matches for the window while every input source passes the coverage gate

    Attributes:
        window (str): Window length, 1m to 7d. ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 30m.
        step (str | Unset): Evaluation period; default min(window/4, 5m), minimum 30s. ISO-8601 duration or shorthand
            (30m, 6h, 7d).
        distinct_by (str | Unset): `entity` (098 entity, else descriptor entityKeys, else source+externalId),
            `externalId`, or a payload path. Versions are never counted. Default: 'entity'.
        group_by (None | str | Unset): null | `source` | `place` | `h3:<res 3..8>` | `admin1` | `country` | a payload
            path Example: place.
        max_groups (int | Unset):  Default: 500.
        coverage_min (float | Unset): Minimum OK/NOT_MODIFIED fetch share in the window Default: 0.8.
    """

    window: str
    step: str | Unset = UNSET
    distinct_by: str | Unset = "entity"
    group_by: None | str | Unset = UNSET
    max_groups: int | Unset = 500
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

        coverage_min = d.pop("coverageMin", UNSET)

        absence_window = cls(
            window=window,
            step=step,
            distinct_by=distinct_by,
            group_by=group_by,
            max_groups=max_groups,
            coverage_min=coverage_min,
        )

        absence_window.additional_properties = d
        return absence_window

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
