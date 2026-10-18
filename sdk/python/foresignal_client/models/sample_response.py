from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.model_disagreement import ModelDisagreement
    from ..models.sample_position import SamplePosition
    from ..models.sample_value import SampleValue


T = TypeVar("T", bound="SampleResponse")


@_attrs_define
class SampleResponse:
    """Values of the requested products at one point

    Attributes:
        lon (float):
        lat (float):
        server_time (datetime.datetime):
        values (list[SampleValue]):
        place_id (None | str | Unset):
        timezone (str | Unset):  Example: America/Chicago.
        time (str | Unset): Requested time expression Example: latest.
        disagreement (ModelDisagreement | None | Unset): 105: when several model products are sampled
        position (None | SamplePosition | Unset): 106: present with `record=`
    """

    lon: float
    lat: float
    server_time: datetime.datetime
    values: list[SampleValue]
    place_id: None | str | Unset = UNSET
    timezone: str | Unset = UNSET
    time: str | Unset = UNSET
    disagreement: ModelDisagreement | None | Unset = UNSET
    position: None | SamplePosition | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.model_disagreement import ModelDisagreement
        from ..models.sample_position import SamplePosition

        lon = self.lon

        lat = self.lat

        server_time = self.server_time.isoformat()

        values = []
        for values_item_data in self.values:
            values_item = values_item_data.to_dict()
            values.append(values_item)

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        timezone = self.timezone

        time = self.time

        disagreement: dict[str, Any] | None | Unset
        if isinstance(self.disagreement, Unset):
            disagreement = UNSET
        elif isinstance(self.disagreement, ModelDisagreement):
            disagreement = self.disagreement.to_dict()
        else:
            disagreement = self.disagreement

        position: dict[str, Any] | None | Unset
        if isinstance(self.position, Unset):
            position = UNSET
        elif isinstance(self.position, SamplePosition):
            position = self.position.to_dict()
        else:
            position = self.position

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lon": lon,
                "lat": lat,
                "serverTime": server_time,
                "values": values,
            }
        )
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if time is not UNSET:
            field_dict["time"] = time
        if disagreement is not UNSET:
            field_dict["disagreement"] = disagreement
        if position is not UNSET:
            field_dict["position"] = position

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.model_disagreement import ModelDisagreement
        from ..models.sample_position import SamplePosition
        from ..models.sample_value import SampleValue

        d = dict(src_dict)
        lon = d.pop("lon")

        lat = d.pop("lat")

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        values = []
        _values = d.pop("values")
        for values_item_data in _values:
            values_item = SampleValue.from_dict(values_item_data)

            values.append(values_item)

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        timezone = d.pop("timezone", UNSET)

        time = d.pop("time", UNSET)

        def _parse_disagreement(data: object) -> ModelDisagreement | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                disagreement_type_0 = ModelDisagreement.from_dict(data)

                return disagreement_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ModelDisagreement | None | Unset, data)

        disagreement = _parse_disagreement(d.pop("disagreement", UNSET))

        def _parse_position(data: object) -> None | SamplePosition | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                position_type_0 = SamplePosition.from_dict(data)

                return position_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SamplePosition | Unset, data)

        position = _parse_position(d.pop("position", UNSET))

        sample_response = cls(
            lon=lon,
            lat=lat,
            server_time=server_time,
            values=values,
            place_id=place_id,
            timezone=timezone,
            time=time,
            disagreement=disagreement,
            position=position,
        )

        sample_response.additional_properties = d
        return sample_response

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
