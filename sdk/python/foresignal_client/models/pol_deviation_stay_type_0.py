from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="PolDeviationStayType0")


@_attrs_define
class PolDeviationStayType0:
    """The stay that triggered the deviation

    Attributes:
        arrive (datetime.datetime | Unset):
        depart (datetime.datetime | None | Unset):
        centroid (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
    """

    arrive: datetime.datetime | Unset = UNSET
    depart: datetime.datetime | None | Unset = UNSET
    centroid: Geometry | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        arrive: str | Unset = UNSET
        if not isinstance(self.arrive, Unset):
            arrive = self.arrive.isoformat()

        depart: None | str | Unset
        if isinstance(self.depart, Unset):
            depart = UNSET
        elif isinstance(self.depart, datetime.datetime):
            depart = self.depart.isoformat()
        else:
            depart = self.depart

        centroid: dict[str, Any] | Unset = UNSET
        if not isinstance(self.centroid, Unset):
            centroid = self.centroid.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if arrive is not UNSET:
            field_dict["arrive"] = arrive
        if depart is not UNSET:
            field_dict["depart"] = depart
        if centroid is not UNSET:
            field_dict["centroid"] = centroid

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        _arrive = d.pop("arrive", UNSET)
        arrive: datetime.datetime | Unset
        if isinstance(_arrive, Unset):
            arrive = UNSET
        else:
            arrive = datetime.datetime.fromisoformat(_arrive)

        def _parse_depart(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                depart_type_0 = datetime.datetime.fromisoformat(data)

                return depart_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        depart = _parse_depart(d.pop("depart", UNSET))

        _centroid = d.pop("centroid", UNSET)
        centroid: Geometry | Unset
        if isinstance(_centroid, Unset):
            centroid = UNSET
        else:
            centroid = Geometry.from_dict(_centroid)

        pol_deviation_stay_type_0 = cls(
            arrive=arrive,
            depart=depart,
            centroid=centroid,
        )

        pol_deviation_stay_type_0.additional_properties = d
        return pol_deviation_stay_type_0

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
