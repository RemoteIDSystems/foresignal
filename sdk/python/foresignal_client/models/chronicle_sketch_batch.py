from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_sketch_batch_access_decisions_item import (
        ChronicleSketchBatchAccessDecisionsItem,
    )
    from ..models.chronicle_sketch_batch_access_requests_item import (
        ChronicleSketchBatchAccessRequestsItem,
    )
    from ..models.chronicle_sketch_package import ChronicleSketchPackage


T = TypeVar("T", bound="ChronicleSketchBatch")


@_attrs_define
class ChronicleSketchBatch:
    """Signed batch of sketch packages sent over the 113 peer channel

    Attributes:
        instance_id (str):
        network_id (str):
        sketches (list[ChronicleSketchPackage]):
        withdrawn (list[str] | Unset): Sketch ids no longer valid
        access_requests (list[ChronicleSketchBatchAccessRequestsItem] | Unset): Access requests relayed to the instance
            holding the sketch (134); never the requester's chronicle
        access_decisions (list[ChronicleSketchBatchAccessDecisionsItem] | Unset): Owners' decisions on relayed requests,
            sent back to the requester's instance (134); status only
    """

    instance_id: str
    network_id: str
    sketches: list[ChronicleSketchPackage]
    withdrawn: list[str] | Unset = UNSET
    access_requests: list[ChronicleSketchBatchAccessRequestsItem] | Unset = UNSET
    access_decisions: list[ChronicleSketchBatchAccessDecisionsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        network_id = self.network_id

        sketches = []
        for sketches_item_data in self.sketches:
            sketches_item = sketches_item_data.to_dict()
            sketches.append(sketches_item)

        withdrawn: list[str] | Unset = UNSET
        if not isinstance(self.withdrawn, Unset):
            withdrawn = self.withdrawn

        access_requests: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.access_requests, Unset):
            access_requests = []
            for access_requests_item_data in self.access_requests:
                access_requests_item = access_requests_item_data.to_dict()
                access_requests.append(access_requests_item)

        access_decisions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.access_decisions, Unset):
            access_decisions = []
            for access_decisions_item_data in self.access_decisions:
                access_decisions_item = access_decisions_item_data.to_dict()
                access_decisions.append(access_decisions_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "networkId": network_id,
                "sketches": sketches,
            }
        )
        if withdrawn is not UNSET:
            field_dict["withdrawn"] = withdrawn
        if access_requests is not UNSET:
            field_dict["accessRequests"] = access_requests
        if access_decisions is not UNSET:
            field_dict["accessDecisions"] = access_decisions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_sketch_batch_access_decisions_item import (
            ChronicleSketchBatchAccessDecisionsItem,
        )
        from ..models.chronicle_sketch_batch_access_requests_item import (
            ChronicleSketchBatchAccessRequestsItem,
        )
        from ..models.chronicle_sketch_package import ChronicleSketchPackage

        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        network_id = d.pop("networkId")

        sketches = []
        _sketches = d.pop("sketches")
        for sketches_item_data in _sketches:
            sketches_item = ChronicleSketchPackage.from_dict(sketches_item_data)

            sketches.append(sketches_item)

        withdrawn = cast(list[str], d.pop("withdrawn", UNSET))

        _access_requests = d.pop("accessRequests", UNSET)
        access_requests: list[ChronicleSketchBatchAccessRequestsItem] | Unset = UNSET
        if _access_requests is not UNSET:
            access_requests = []
            for access_requests_item_data in _access_requests:
                access_requests_item = ChronicleSketchBatchAccessRequestsItem.from_dict(
                    access_requests_item_data
                )

                access_requests.append(access_requests_item)

        _access_decisions = d.pop("accessDecisions", UNSET)
        access_decisions: list[ChronicleSketchBatchAccessDecisionsItem] | Unset = UNSET
        if _access_decisions is not UNSET:
            access_decisions = []
            for access_decisions_item_data in _access_decisions:
                access_decisions_item = (
                    ChronicleSketchBatchAccessDecisionsItem.from_dict(
                        access_decisions_item_data
                    )
                )

                access_decisions.append(access_decisions_item)

        chronicle_sketch_batch = cls(
            instance_id=instance_id,
            network_id=network_id,
            sketches=sketches,
            withdrawn=withdrawn,
            access_requests=access_requests,
            access_decisions=access_decisions,
        )

        chronicle_sketch_batch.additional_properties = d
        return chronicle_sketch_batch

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
