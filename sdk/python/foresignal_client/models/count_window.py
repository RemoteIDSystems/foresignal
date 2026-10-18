from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.count_window_op import CountWindowOp
from ..types import UNSET, Unset

T = TypeVar("T", bound="CountWindow")


@_attrs_define
class CountWindow:
    """Count rule window: fires when N distinct entities are seen in the window

    Attributes:
        window (str): Window length, 1m to 7d. ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 30m.
        op (CountWindowOp):
        threshold (int):
        step (str | Unset): Evaluation period; default min(window/4, 5m), minimum 30s. ISO-8601 duration or shorthand
            (30m, 6h, 7d).
        distinct_by (str | Unset): `entity` (098 entity, else descriptor entityKeys, else source+externalId),
            `externalId`, or a payload path. Versions are never counted. Default: 'entity'.
        group_by (None | str | Unset): null | `source` | `place` | `h3:<res 3..8>` | `admin1` | `country` | a payload
            path Example: place.
        max_groups (int | Unset):  Default: 500.
    """

    window: str
    op: CountWindowOp
    threshold: int
    step: str | Unset = UNSET
    distinct_by: str | Unset = "entity"
    group_by: None | str | Unset = UNSET
    max_groups: int | Unset = 500
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        window = self.window

        op = self.op.value

        threshold = self.threshold

        step = self.step

        distinct_by = self.distinct_by

        group_by: None | str | Unset
        if isinstance(self.group_by, Unset):
            group_by = UNSET
        else:
            group_by = self.group_by

        max_groups = self.max_groups

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "window": window,
                "op": op,
                "threshold": threshold,
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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        window = d.pop("window")

        op = CountWindowOp(d.pop("op"))

        threshold = d.pop("threshold")

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

        count_window = cls(
            window=window,
            op=op,
            threshold=threshold,
            step=step,
            distinct_by=distinct_by,
            group_by=group_by,
            max_groups=max_groups,
        )

        count_window.additional_properties = d
        return count_window

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
