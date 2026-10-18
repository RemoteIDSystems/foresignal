from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.taxii_envelope_objects_item import TaxiiEnvelopeObjectsItem


T = TypeVar("T", bound="TaxiiEnvelope")


@_attrs_define
class TaxiiEnvelope:
    """TAXII 2.1 envelope of STIX objects

    Attributes:
        more (bool | Unset):
        next_ (None | str | Unset):
        objects (list[TaxiiEnvelopeObjectsItem] | Unset):
    """

    more: bool | Unset = UNSET
    next_: None | str | Unset = UNSET
    objects: list[TaxiiEnvelopeObjectsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        more = self.more

        next_: None | str | Unset
        if isinstance(self.next_, Unset):
            next_ = UNSET
        else:
            next_ = self.next_

        objects: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.objects, Unset):
            objects = []
            for objects_item_data in self.objects:
                objects_item = objects_item_data.to_dict()
                objects.append(objects_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if more is not UNSET:
            field_dict["more"] = more
        if next_ is not UNSET:
            field_dict["next"] = next_
        if objects is not UNSET:
            field_dict["objects"] = objects

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.taxii_envelope_objects_item import TaxiiEnvelopeObjectsItem

        d = dict(src_dict)
        more = d.pop("more", UNSET)

        def _parse_next_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        next_ = _parse_next_(d.pop("next", UNSET))

        _objects = d.pop("objects", UNSET)
        objects: list[TaxiiEnvelopeObjectsItem] | Unset = UNSET
        if _objects is not UNSET:
            objects = []
            for objects_item_data in _objects:
                objects_item = TaxiiEnvelopeObjectsItem.from_dict(objects_item_data)

                objects.append(objects_item)

        taxii_envelope = cls(
            more=more,
            next_=next_,
            objects=objects,
        )

        taxii_envelope.additional_properties = d
        return taxii_envelope

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
