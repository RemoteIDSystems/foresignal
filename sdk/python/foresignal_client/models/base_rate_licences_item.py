from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.base_rate_licences_item_egress import BaseRateLicencesItemEgress
from ..types import UNSET, Unset

T = TypeVar("T", bound="BaseRateLicencesItem")


@_attrs_define
class BaseRateLicencesItem:
    """Licence of a contributing source

    Attributes:
        source (str):
        licence (str):
        egress (BaseRateLicencesItemEgress | Unset):
    """

    source: str
    licence: str
    egress: BaseRateLicencesItemEgress | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source

        licence = self.licence

        egress: str | Unset = UNSET
        if not isinstance(self.egress, Unset):
            egress = self.egress.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
                "licence": licence,
            }
        )
        if egress is not UNSET:
            field_dict["egress"] = egress

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source = d.pop("source")

        licence = d.pop("licence")

        _egress = d.pop("egress", UNSET)
        egress: BaseRateLicencesItemEgress | Unset
        if isinstance(_egress, Unset):
            egress = UNSET
        else:
            egress = BaseRateLicencesItemEgress(_egress)

        base_rate_licences_item = cls(
            source=source,
            licence=licence,
            egress=egress,
        )

        base_rate_licences_item.additional_properties = d
        return base_rate_licences_item

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
