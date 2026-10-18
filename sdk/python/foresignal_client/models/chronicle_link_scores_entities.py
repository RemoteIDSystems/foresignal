from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleLinkScoresEntities")


@_attrs_define
class ChronicleLinkScoresEntities:
    """Overlap estimate for one key type

    Attributes:
        jaccard (float | Unset):
        containment (float | Unset):
        shared_estimate (float | Unset):
    """

    jaccard: float | Unset = UNSET
    containment: float | Unset = UNSET
    shared_estimate: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        jaccard = self.jaccard

        containment = self.containment

        shared_estimate = self.shared_estimate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if jaccard is not UNSET:
            field_dict["jaccard"] = jaccard
        if containment is not UNSET:
            field_dict["containment"] = containment
        if shared_estimate is not UNSET:
            field_dict["sharedEstimate"] = shared_estimate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        jaccard = d.pop("jaccard", UNSET)

        containment = d.pop("containment", UNSET)

        shared_estimate = d.pop("sharedEstimate", UNSET)

        chronicle_link_scores_entities = cls(
            jaccard=jaccard,
            containment=containment,
            shared_estimate=shared_estimate,
        )

        chronicle_link_scores_entities.additional_properties = d
        return chronicle_link_scores_entities

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
