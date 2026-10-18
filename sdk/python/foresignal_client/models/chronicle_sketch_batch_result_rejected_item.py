from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ChronicleSketchBatchResultRejectedItem")


@_attrs_define
class ChronicleSketchBatchResultRejectedItem:
    """Rejected sketch

    Attributes:
        sketch_id (str):
        reason (str):
    """

    sketch_id: str
    reason: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sketch_id = self.sketch_id

        reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sketchId": sketch_id,
                "reason": reason,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sketch_id = d.pop("sketchId")

        reason = d.pop("reason")

        chronicle_sketch_batch_result_rejected_item = cls(
            sketch_id=sketch_id,
            reason=reason,
        )

        chronicle_sketch_batch_result_rejected_item.additional_properties = d
        return chronicle_sketch_batch_result_rejected_item

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
