from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.chronicle_sketch_batch_result_rejected_item import (
        ChronicleSketchBatchResultRejectedItem,
    )


T = TypeVar("T", bound="ChronicleSketchBatchResult")


@_attrs_define
class ChronicleSketchBatchResult:
    """Outcome of receiving a sketch batch

    Attributes:
        accepted (int):
        rejected (list[ChronicleSketchBatchResultRejectedItem]):
    """

    accepted: int
    rejected: list[ChronicleSketchBatchResultRejectedItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        accepted = self.accepted

        rejected = []
        for rejected_item_data in self.rejected:
            rejected_item = rejected_item_data.to_dict()
            rejected.append(rejected_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "accepted": accepted,
                "rejected": rejected,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_sketch_batch_result_rejected_item import (
            ChronicleSketchBatchResultRejectedItem,
        )

        d = dict(src_dict)
        accepted = d.pop("accepted")

        rejected = []
        _rejected = d.pop("rejected")
        for rejected_item_data in _rejected:
            rejected_item = ChronicleSketchBatchResultRejectedItem.from_dict(
                rejected_item_data
            )

            rejected.append(rejected_item)

        chronicle_sketch_batch_result = cls(
            accepted=accepted,
            rejected=rejected,
        )

        chronicle_sketch_batch_result.additional_properties = d
        return chronicle_sketch_batch_result

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
