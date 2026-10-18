from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_digest_period_hazards_entered_item import (
        PlaceWeatherDigestPeriodHazardsEnteredItem,
    )


T = TypeVar("T", bound="PlaceWeatherDigestPeriod")


@_attrs_define
class PlaceWeatherDigestPeriod:
    """The briefing lookback

    Attributes:
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        partial (bool | Unset): Window starts below the retention floor ("partial period")
        max_dbz (float | None | Unset): Largest radar.maxDbzInRadius of the ok place-weather versions in the window
            (null when none was ok); omitted when weatherProducts excludes mrms-reflectivity
        max_mesh_mm (float | None | Unset): Largest hail.meshMaxMmInRadius of the ok place-weather versions (null when
            none); omitted when weatherProducts excludes mrms-mesh-60m
        lightning_total (int | None | Unset): GOES GLM flashes within the place radius over the window, from the 14-day
            flash archive (the largest count over satellites); null when GLM is unavailable
        hazards_entered (list[PlaceWeatherDigestPeriodHazardsEnteredItem] | Unset):
        notes (list[str] | Unset): Why a period value is unknown or partial, for example "lightning total unavailable:
            GOES GLM is disabled or not installed"
    """

    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    partial: bool | Unset = UNSET
    max_dbz: float | None | Unset = UNSET
    max_mesh_mm: float | None | Unset = UNSET
    lightning_total: int | None | Unset = UNSET
    hazards_entered: list[PlaceWeatherDigestPeriodHazardsEnteredItem] | Unset = UNSET
    notes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        partial = self.partial

        max_dbz: float | None | Unset
        if isinstance(self.max_dbz, Unset):
            max_dbz = UNSET
        else:
            max_dbz = self.max_dbz

        max_mesh_mm: float | None | Unset
        if isinstance(self.max_mesh_mm, Unset):
            max_mesh_mm = UNSET
        else:
            max_mesh_mm = self.max_mesh_mm

        lightning_total: int | None | Unset
        if isinstance(self.lightning_total, Unset):
            lightning_total = UNSET
        else:
            lightning_total = self.lightning_total

        hazards_entered: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.hazards_entered, Unset):
            hazards_entered = []
            for hazards_entered_item_data in self.hazards_entered:
                hazards_entered_item = hazards_entered_item_data.to_dict()
                hazards_entered.append(hazards_entered_item)

        notes: list[str] | Unset = UNSET
        if not isinstance(self.notes, Unset):
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if partial is not UNSET:
            field_dict["partial"] = partial
        if max_dbz is not UNSET:
            field_dict["maxDbz"] = max_dbz
        if max_mesh_mm is not UNSET:
            field_dict["maxMeshMm"] = max_mesh_mm
        if lightning_total is not UNSET:
            field_dict["lightningTotal"] = lightning_total
        if hazards_entered is not UNSET:
            field_dict["hazardsEntered"] = hazards_entered
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_digest_period_hazards_entered_item import (
            PlaceWeatherDigestPeriodHazardsEnteredItem,
        )

        d = dict(src_dict)
        _from_ = d.pop("from", UNSET)
        from_: datetime.datetime | Unset
        if isinstance(_from_, Unset):
            from_ = UNSET
        else:
            from_ = datetime.datetime.fromisoformat(_from_)

        _to = d.pop("to", UNSET)
        to: datetime.datetime | Unset
        if isinstance(_to, Unset):
            to = UNSET
        else:
            to = datetime.datetime.fromisoformat(_to)

        partial = d.pop("partial", UNSET)

        def _parse_max_dbz(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_dbz = _parse_max_dbz(d.pop("maxDbz", UNSET))

        def _parse_max_mesh_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_mesh_mm = _parse_max_mesh_mm(d.pop("maxMeshMm", UNSET))

        def _parse_lightning_total(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        lightning_total = _parse_lightning_total(d.pop("lightningTotal", UNSET))

        _hazards_entered = d.pop("hazardsEntered", UNSET)
        hazards_entered: list[PlaceWeatherDigestPeriodHazardsEnteredItem] | Unset = (
            UNSET
        )
        if _hazards_entered is not UNSET:
            hazards_entered = []
            for hazards_entered_item_data in _hazards_entered:
                hazards_entered_item = (
                    PlaceWeatherDigestPeriodHazardsEnteredItem.from_dict(
                        hazards_entered_item_data
                    )
                )

                hazards_entered.append(hazards_entered_item)

        notes = cast(list[str], d.pop("notes", UNSET))

        place_weather_digest_period = cls(
            from_=from_,
            to=to,
            partial=partial,
            max_dbz=max_dbz,
            max_mesh_mm=max_mesh_mm,
            lightning_total=lightning_total,
            hazards_entered=hazards_entered,
            notes=notes,
        )

        place_weather_digest_period.additional_properties = d
        return place_weather_digest_period

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
