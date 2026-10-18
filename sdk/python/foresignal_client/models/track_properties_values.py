from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="TrackPropertiesValues")


@_attrs_define
class TrackPropertiesValues:
    """Requested numeric fields, parallel arrays per segment"""

    additional_properties: dict[str, list[list[float | None]]] = _attrs_field(
        init=False, factory=dict
    )

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = []
            for additional_property_item_data in prop:
                additional_property_item = []
                for additional_property_item_item_data in additional_property_item_data:
                    additional_property_item_item: float | None
                    additional_property_item_item = additional_property_item_item_data
                    additional_property_item.append(additional_property_item_item)

                field_dict[prop_name].append(additional_property_item)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        track_properties_values = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = []
            _additional_property = prop_dict
            for additional_property_item_data in _additional_property:
                additional_property_item = []
                _additional_property_item = additional_property_item_data
                for additional_property_item_item_data in _additional_property_item:

                    def _parse_additional_property_item_item(
                        data: object,
                    ) -> float | None:
                        if data is None:
                            return data
                        return cast(float | None, data)

                    additional_property_item_item = (
                        _parse_additional_property_item_item(
                            additional_property_item_item_data
                        )
                    )

                    additional_property_item.append(additional_property_item_item)

                additional_property.append(additional_property_item)

            additional_properties[prop_name] = additional_property

        track_properties_values.additional_properties = additional_properties
        return track_properties_values

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> list[list[float | None]]:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: list[list[float | None]]) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
