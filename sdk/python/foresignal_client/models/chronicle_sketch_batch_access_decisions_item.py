from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_sketch_batch_access_decisions_item_delivered_as_type_1 import (
    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1,
)
from ..models.chronicle_sketch_batch_access_decisions_item_delivered_as_type_2_type_1 import (
    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1,
)
from ..models.chronicle_sketch_batch_access_decisions_item_delivered_as_type_3_type_1 import (
    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1,
)
from ..models.chronicle_sketch_batch_access_decisions_item_status import (
    ChronicleSketchBatchAccessDecisionsItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleSketchBatchAccessDecisionsItem")


@_attrs_define
class ChronicleSketchBatchAccessDecisionsItem:
    """A relayed decision

    Attributes:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ChronicleSketchBatchAccessDecisionsItemStatus):
        delivered_as (ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1 |
            ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1 |
            ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1 | None | Unset):
        package_id (None | str | Unset):
    """

    request_id: str
    status: ChronicleSketchBatchAccessDecisionsItemStatus
    delivered_as: (
        ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1
        | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1
        | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1
        | None
        | Unset
    ) = UNSET
    package_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        request_id = self.request_id

        status = self.status.value

        delivered_as: None | str | Unset
        if isinstance(self.delivered_as, Unset):
            delivered_as = UNSET
        elif (
            isinstance(
                self.delivered_as,
                ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1,
            )
            or isinstance(
                self.delivered_as,
                ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1,
            )
            or isinstance(
                self.delivered_as,
                ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1,
            )
        ):
            delivered_as = self.delivered_as.value
        else:
            delivered_as = self.delivered_as

        package_id: None | str | Unset
        if isinstance(self.package_id, Unset):
            package_id = UNSET
        else:
            package_id = self.package_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "requestId": request_id,
                "status": status,
            }
        )
        if delivered_as is not UNSET:
            field_dict["deliveredAs"] = delivered_as
        if package_id is not UNSET:
            field_dict["packageId"] = package_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        request_id = d.pop("requestId")

        status = ChronicleSketchBatchAccessDecisionsItemStatus(d.pop("status"))

        def _parse_delivered_as(
            data: object,
        ) -> (
            ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1
            | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1
            | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                delivered_as_type_1 = (
                    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1(data)
                )

                return delivered_as_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                delivered_as_type_2_type_1 = (
                    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1(data)
                )

                return delivered_as_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                delivered_as_type_3_type_1 = (
                    ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1(data)
                )

                return delivered_as_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                ChronicleSketchBatchAccessDecisionsItemDeliveredAsType1
                | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType2Type1
                | ChronicleSketchBatchAccessDecisionsItemDeliveredAsType3Type1
                | None
                | Unset,
                data,
            )

        delivered_as = _parse_delivered_as(d.pop("deliveredAs", UNSET))

        def _parse_package_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        package_id = _parse_package_id(d.pop("packageId", UNSET))

        chronicle_sketch_batch_access_decisions_item = cls(
            request_id=request_id,
            status=status,
            delivered_as=delivered_as,
            package_id=package_id,
        )

        chronicle_sketch_batch_access_decisions_item.additional_properties = d
        return chronicle_sketch_batch_access_decisions_item

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
