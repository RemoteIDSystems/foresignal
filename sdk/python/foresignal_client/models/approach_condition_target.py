from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ApproachConditionTarget")


@_attrs_define
class ApproachConditionTarget:
    """Exactly one of placeId, point or record

    Attributes:
        place_id (None | str | Unset):
        point (Geometry | None | Unset):
        record (None | str | Unset): `{sourceId}/{externalId}`
    """

    place_id: None | str | Unset = UNSET
    point: Geometry | None | Unset = UNSET
    record: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        point: dict[str, Any] | None | Unset
        if isinstance(self.point, Unset):
            point = UNSET
        elif isinstance(self.point, Geometry):
            point = self.point.to_dict()
        else:
            point = self.point

        record: None | str | Unset
        if isinstance(self.record, Unset):
            record = UNSET
        else:
            record = self.record

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if point is not UNSET:
            field_dict["point"] = point
        if record is not UNSET:
            field_dict["record"] = record

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_point(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                point_type_0 = Geometry.from_dict(data)

                return point_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        point = _parse_point(d.pop("point", UNSET))

        def _parse_record(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record = _parse_record(d.pop("record", UNSET))

        approach_condition_target = cls(
            place_id=place_id,
            point=point,
            record=record,
        )

        approach_condition_target.additional_properties = d
        return approach_condition_target

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
