from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.assessment_bundle_objects_item_data import (
        AssessmentBundleObjectsItemData,
    )
    from ..models.object_envelope import ObjectEnvelope
    from ..models.provenance import Provenance


T = TypeVar("T", bound="AssessmentBundleObjectsItem")


@_attrs_define
class AssessmentBundleObjectsItem:
    """Cited object

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        envelope (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        data (AssessmentBundleObjectsItemData | Unset): Full object: finding, base rate, forecast with scores,
            hypothesis revision
        provenance (Provenance | Unset):
    """

    ref: str
    envelope: ObjectEnvelope
    data: AssessmentBundleObjectsItemData | Unset = UNSET
    provenance: Provenance | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        envelope = self.envelope.to_dict()

        data: dict[str, Any] | Unset = UNSET
        if not isinstance(self.data, Unset):
            data = self.data.to_dict()

        provenance: dict[str, Any] | Unset = UNSET
        if not isinstance(self.provenance, Unset):
            provenance = self.provenance.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "envelope": envelope,
            }
        )
        if data is not UNSET:
            field_dict["data"] = data
        if provenance is not UNSET:
            field_dict["provenance"] = provenance

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.assessment_bundle_objects_item_data import (
            AssessmentBundleObjectsItemData,
        )
        from ..models.object_envelope import ObjectEnvelope
        from ..models.provenance import Provenance

        d = dict(src_dict)
        ref = d.pop("ref")

        envelope = ObjectEnvelope.from_dict(d.pop("envelope"))

        _data = d.pop("data", UNSET)
        data: AssessmentBundleObjectsItemData | Unset
        if isinstance(_data, Unset):
            data = UNSET
        else:
            data = AssessmentBundleObjectsItemData.from_dict(_data)

        _provenance = d.pop("provenance", UNSET)
        provenance: Provenance | Unset
        if isinstance(_provenance, Unset):
            provenance = UNSET
        else:
            provenance = Provenance.from_dict(_provenance)

        assessment_bundle_objects_item = cls(
            ref=ref,
            envelope=envelope,
            data=data,
            provenance=provenance,
        )

        assessment_bundle_objects_item.additional_properties = d
        return assessment_bundle_objects_item

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
