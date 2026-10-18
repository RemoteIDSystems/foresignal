from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_digest_next_gfs import PlaceWeatherDigestNextGfs
    from ..models.place_weather_digest_next_hrrr import PlaceWeatherDigestNextHrrr
    from ..models.place_weather_digest_next_nws import PlaceWeatherDigestNextNws


T = TypeVar("T", bound="PlaceWeatherDigestNext")


@_attrs_define
class PlaceWeatherDigestNext:
    """Next 24–72 h

    Attributes:
        nws (PlaceWeatherDigestNextNws | Unset): NWS gridpoint summary
        gfs (PlaceWeatherDigestNextGfs | Unset): GFS maxima
        hrrr (PlaceWeatherDigestNextHrrr | Unset): HRRR maxima
    """

    nws: PlaceWeatherDigestNextNws | Unset = UNSET
    gfs: PlaceWeatherDigestNextGfs | Unset = UNSET
    hrrr: PlaceWeatherDigestNextHrrr | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        nws: dict[str, Any] | Unset = UNSET
        if not isinstance(self.nws, Unset):
            nws = self.nws.to_dict()

        gfs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.gfs, Unset):
            gfs = self.gfs.to_dict()

        hrrr: dict[str, Any] | Unset = UNSET
        if not isinstance(self.hrrr, Unset):
            hrrr = self.hrrr.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if nws is not UNSET:
            field_dict["nws"] = nws
        if gfs is not UNSET:
            field_dict["gfs"] = gfs
        if hrrr is not UNSET:
            field_dict["hrrr"] = hrrr

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_digest_next_gfs import PlaceWeatherDigestNextGfs
        from ..models.place_weather_digest_next_hrrr import PlaceWeatherDigestNextHrrr
        from ..models.place_weather_digest_next_nws import PlaceWeatherDigestNextNws

        d = dict(src_dict)
        _nws = d.pop("nws", UNSET)
        nws: PlaceWeatherDigestNextNws | Unset
        if isinstance(_nws, Unset):
            nws = UNSET
        else:
            nws = PlaceWeatherDigestNextNws.from_dict(_nws)

        _gfs = d.pop("gfs", UNSET)
        gfs: PlaceWeatherDigestNextGfs | Unset
        if isinstance(_gfs, Unset):
            gfs = UNSET
        else:
            gfs = PlaceWeatherDigestNextGfs.from_dict(_gfs)

        _hrrr = d.pop("hrrr", UNSET)
        hrrr: PlaceWeatherDigestNextHrrr | Unset
        if isinstance(_hrrr, Unset):
            hrrr = UNSET
        else:
            hrrr = PlaceWeatherDigestNextHrrr.from_dict(_hrrr)

        place_weather_digest_next = cls(
            nws=nws,
            gfs=gfs,
            hrrr=hrrr,
        )

        place_weather_digest_next.additional_properties = d
        return place_weather_digest_next

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
