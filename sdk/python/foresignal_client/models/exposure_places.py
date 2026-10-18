from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exposure_places_status import ExposurePlacesStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.exposure_places_items_item import ExposurePlacesItemsItem


T = TypeVar("T", bound="ExposurePlaces")


@_attrs_define
class ExposurePlaces:
    """Places (036) inside the area

    Attributes:
        status (ExposurePlacesStatus):
        attribution (list[Attribution] | Unset):
        items (list[ExposurePlacesItemsItem] | Unset):
    """

    status: ExposurePlacesStatus
    attribution: list[Attribution] | Unset = UNSET
    items: list[ExposurePlacesItemsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if items is not UNSET:
            field_dict["items"] = items

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.exposure_places_items_item import ExposurePlacesItemsItem

        d = dict(src_dict)
        status = ExposurePlacesStatus(d.pop("status"))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        _items = d.pop("items", UNSET)
        items: list[ExposurePlacesItemsItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = ExposurePlacesItemsItem.from_dict(items_item_data)

                items.append(items_item)

        exposure_places = cls(
            status=status,
            attribution=attribution,
            items=items,
        )

        exposure_places.additional_properties = d
        return exposure_places

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
