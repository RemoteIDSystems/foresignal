from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sample_request_units import SampleRequestUnits
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sample_request_points_item_type_0 import SampleRequestPointsItemType0
    from ..models.sample_request_points_item_type_1 import SampleRequestPointsItemType1


T = TypeVar("T", bound="SampleRequest")


@_attrs_define
class SampleRequest:
    """Batch point sample (≤ 100 points)

    Attributes:
        points (list[SampleRequestPointsItemType0 | SampleRequestPointsItemType1]): Points to sample
        products (list[str]):
        time (str | Unset): `latest`, `now`, ISO-8601, or a relative offset (`-PT1H`, `+PT6H`) Default: 'latest'.
        units (SampleRequestUnits | Unset):  Default: SampleRequestUnits.METRIC.
    """

    points: list[SampleRequestPointsItemType0 | SampleRequestPointsItemType1]
    products: list[str]
    time: str | Unset = "latest"
    units: SampleRequestUnits | Unset = SampleRequestUnits.METRIC
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.sample_request_points_item_type_0 import (
            SampleRequestPointsItemType0,
        )

        points = []
        for points_item_data in self.points:
            points_item: dict[str, Any]
            if isinstance(points_item_data, SampleRequestPointsItemType0):
                points_item = points_item_data.to_dict()
            else:
                points_item = points_item_data.to_dict()

            points.append(points_item)

        products = self.products

        time = self.time

        units: str | Unset = UNSET
        if not isinstance(self.units, Unset):
            units = self.units.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "points": points,
                "products": products,
            }
        )
        if time is not UNSET:
            field_dict["time"] = time
        if units is not UNSET:
            field_dict["units"] = units

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sample_request_points_item_type_0 import (
            SampleRequestPointsItemType0,
        )
        from ..models.sample_request_points_item_type_1 import (
            SampleRequestPointsItemType1,
        )

        d = dict(src_dict)
        points = []
        _points = d.pop("points")
        for points_item_data in _points:

            def _parse_points_item(
                data: object,
            ) -> SampleRequestPointsItemType0 | SampleRequestPointsItemType1:
                try:
                    if not isinstance(data, dict):
                        raise TypeError()
                    points_item_type_0 = SampleRequestPointsItemType0.from_dict(data)

                    return points_item_type_0
                except (TypeError, ValueError, AttributeError, KeyError):
                    pass
                if not isinstance(data, dict):
                    raise TypeError()
                points_item_type_1 = SampleRequestPointsItemType1.from_dict(data)

                return points_item_type_1

            points_item = _parse_points_item(points_item_data)

            points.append(points_item)

        products = cast(list[str], d.pop("products"))

        time = d.pop("time", UNSET)

        _units = d.pop("units", UNSET)
        units: SampleRequestUnits | Unset
        if isinstance(_units, Unset):
            units = UNSET
        else:
            units = SampleRequestUnits(_units)

        sample_request = cls(
            points=points,
            products=products,
            time=time,
            units=units,
        )

        sample_request.additional_properties = d
        return sample_request

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
