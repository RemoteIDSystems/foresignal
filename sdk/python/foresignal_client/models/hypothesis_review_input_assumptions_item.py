from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_review_input_assumptions_item_status import (
    HypothesisReviewInputAssumptionsItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisReviewInputAssumptionsItem")


@_attrs_define
class HypothesisReviewInputAssumptionsItem:
    """Assumption status change

    Attributes:
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (HypothesisReviewInputAssumptionsItemStatus):
        rationale (None | str | Unset):
    """

    assumption_id: str
    status: HypothesisReviewInputAssumptionsItemStatus
    rationale: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        assumption_id = self.assumption_id

        status = self.status.value

        rationale: None | str | Unset
        if isinstance(self.rationale, Unset):
            rationale = UNSET
        else:
            rationale = self.rationale

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "assumptionId": assumption_id,
                "status": status,
            }
        )
        if rationale is not UNSET:
            field_dict["rationale"] = rationale

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        assumption_id = d.pop("assumptionId")

        status = HypothesisReviewInputAssumptionsItemStatus(d.pop("status"))

        def _parse_rationale(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rationale = _parse_rationale(d.pop("rationale", UNSET))

        hypothesis_review_input_assumptions_item = cls(
            assumption_id=assumption_id,
            status=status,
            rationale=rationale,
        )

        hypothesis_review_input_assumptions_item.additional_properties = d
        return hypothesis_review_input_assumptions_item

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
