from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.approach_condition_type import ApproachConditionType

if TYPE_CHECKING:
    from ..models.approach_condition_target import ApproachConditionTarget


T = TypeVar("T", bound="ApproachCondition")


@_attrs_define
class ApproachCondition:
    """Trigger condition `approach`: fires `enter` when dcpa ≤ withinMeters and tcpa ≤ withinMinutes; evaluated in Java by
    the persist gate; stale or ambiguous state is conservative (114)

        Attributes:
            type_ (ApproachConditionType):
            target (ApproachConditionTarget): Exactly one of placeId, point or record
            within_meters (float):
            within_minutes (float):
    """

    type_: ApproachConditionType
    target: ApproachConditionTarget
    within_meters: float
    within_minutes: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        target = self.target.to_dict()

        within_meters = self.within_meters

        within_minutes = self.within_minutes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "target": target,
                "withinMeters": within_meters,
                "withinMinutes": within_minutes,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.approach_condition_target import ApproachConditionTarget

        d = dict(src_dict)
        type_ = ApproachConditionType(d.pop("type"))

        target = ApproachConditionTarget.from_dict(d.pop("target"))

        within_meters = d.pop("withinMeters")

        within_minutes = d.pop("withinMinutes")

        approach_condition = cls(
            type_=type_,
            target=target,
            within_meters=within_meters,
            within_minutes=within_minutes,
        )

        approach_condition.additional_properties = d
        return approach_condition

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
