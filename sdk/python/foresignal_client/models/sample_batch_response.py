from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.sample_response import SampleResponse


T = TypeVar("T", bound="SampleBatchResponse")


@_attrs_define
class SampleBatchResponse:
    """Batch sample results, one per requested point in order

    Attributes:
        items (list[SampleResponse]):
        server_time (datetime.datetime):
    """

    items: list[SampleResponse]
    server_time: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        server_time = self.server_time.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
                "serverTime": server_time,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sample_response import SampleResponse

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = SampleResponse.from_dict(items_item_data)

            items.append(items_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        sample_batch_response = cls(
            items=items,
            server_time=server_time,
        )

        sample_batch_response.additional_properties = d
        return sample_batch_response

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
