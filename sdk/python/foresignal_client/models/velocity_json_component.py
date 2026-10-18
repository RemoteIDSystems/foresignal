from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.velocity_json_component_header import VelocityJsonComponentHeader


T = TypeVar("T", bound="VelocityJsonComponent")


@_attrs_define
class VelocityJsonComponent:
    """One component (U or V) in leaflet-velocity / wind-js format; data row-major north→south

    Attributes:
        header (VelocityJsonComponentHeader): GRIB-like header
        data (list[float | None]):
    """

    header: VelocityJsonComponentHeader
    data: list[float | None]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        header = self.header.to_dict()

        data = []
        for data_item_data in self.data:
            data_item: float | None
            data_item = data_item_data
            data.append(data_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "header": header,
                "data": data,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.velocity_json_component_header import VelocityJsonComponentHeader

        d = dict(src_dict)
        header = VelocityJsonComponentHeader.from_dict(d.pop("header"))

        data = []
        _data = d.pop("data")
        for data_item_data in _data:

            def _parse_data_item(data: object) -> float | None:
                if data is None:
                    return data
                return cast(float | None, data)

            data_item = _parse_data_item(data_item_data)

            data.append(data_item)

        velocity_json_component = cls(
            header=header,
            data=data,
        )

        velocity_json_component.additional_properties = d
        return velocity_json_component

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
