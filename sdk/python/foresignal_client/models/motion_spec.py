from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MotionSpec")


@_attrs_define
class MotionSpec:
    """Payload fields used for dead reckoning (never significant)

    Attributes:
        speed_field (str): Top-level payload key holding speed Example: gs.
        speed_unit (str): Unit of the speed field Example: KNOTS.
        course_field (None | str | Unset): Top-level payload key holding course over ground (degrees) Example: track.
    """

    speed_field: str
    speed_unit: str
    course_field: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        speed_field = self.speed_field

        speed_unit = self.speed_unit

        course_field: None | str | Unset
        if isinstance(self.course_field, Unset):
            course_field = UNSET
        else:
            course_field = self.course_field

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "speedField": speed_field,
                "speedUnit": speed_unit,
            }
        )
        if course_field is not UNSET:
            field_dict["courseField"] = course_field

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        speed_field = d.pop("speedField")

        speed_unit = d.pop("speedUnit")

        def _parse_course_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        course_field = _parse_course_field(d.pop("courseField", UNSET))

        motion_spec = cls(
            speed_field=speed_field,
            speed_unit=speed_unit,
            course_field=course_field,
        )

        motion_spec.additional_properties = d
        return motion_spec

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
