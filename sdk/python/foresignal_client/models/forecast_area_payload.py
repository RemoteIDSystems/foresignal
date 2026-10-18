from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_area_payload_model import ForecastAreaPayloadModel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_area_payload_threshold import ForecastAreaPayloadThreshold


T = TypeVar("T", bound="ForecastAreaPayload")


@_attrs_define
class ForecastAreaPayload:
    """Kind `forecast-area` (`wx-forecast-areas`): union polygon of forecast threshold exceedance over the horizon for one
    model run; `validFrom`/`validUntil` are explicit

        Attributes:
            model (ForecastAreaPayloadModel):
            field (str):  Example: gust.
            threshold (ForecastAreaPayloadThreshold): Threshold
            first_exceed_at (datetime.datetime):
            run (datetime.datetime):
            peak_value (float | Unset):
            peak_at (datetime.datetime | Unset):
            area_km_2 (float | Unset):
            attribution (str | Unset): ECMWF CC BY 4.0 attribution with the changes-made line; present on IFS- and AIFS-
                derived areas. `run` is volatile: a newer run with the same area writes no version
            automated (bool | Unset):
            not_a_warning (bool | Unset):
    """

    model: ForecastAreaPayloadModel
    field: str
    threshold: ForecastAreaPayloadThreshold
    first_exceed_at: datetime.datetime
    run: datetime.datetime
    peak_value: float | Unset = UNSET
    peak_at: datetime.datetime | Unset = UNSET
    area_km_2: float | Unset = UNSET
    attribution: str | Unset = UNSET
    automated: bool | Unset = UNSET
    not_a_warning: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        model = self.model.value

        field = self.field

        threshold = self.threshold.to_dict()

        first_exceed_at = self.first_exceed_at.isoformat()

        run = self.run.isoformat()

        peak_value = self.peak_value

        peak_at: str | Unset = UNSET
        if not isinstance(self.peak_at, Unset):
            peak_at = self.peak_at.isoformat()

        area_km_2 = self.area_km_2

        attribution = self.attribution

        automated = self.automated

        not_a_warning = self.not_a_warning

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "model": model,
                "field": field,
                "threshold": threshold,
                "firstExceedAt": first_exceed_at,
                "run": run,
            }
        )
        if peak_value is not UNSET:
            field_dict["peakValue"] = peak_value
        if peak_at is not UNSET:
            field_dict["peakAt"] = peak_at
        if area_km_2 is not UNSET:
            field_dict["areaKm2"] = area_km_2
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if automated is not UNSET:
            field_dict["automated"] = automated
        if not_a_warning is not UNSET:
            field_dict["notAWarning"] = not_a_warning

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_area_payload_threshold import (
            ForecastAreaPayloadThreshold,
        )

        d = dict(src_dict)
        model = ForecastAreaPayloadModel(d.pop("model"))

        field = d.pop("field")

        threshold = ForecastAreaPayloadThreshold.from_dict(d.pop("threshold"))

        first_exceed_at = datetime.datetime.fromisoformat(d.pop("firstExceedAt"))

        run = datetime.datetime.fromisoformat(d.pop("run"))

        peak_value = d.pop("peakValue", UNSET)

        _peak_at = d.pop("peakAt", UNSET)
        peak_at: datetime.datetime | Unset
        if isinstance(_peak_at, Unset):
            peak_at = UNSET
        else:
            peak_at = datetime.datetime.fromisoformat(_peak_at)

        area_km_2 = d.pop("areaKm2", UNSET)

        attribution = d.pop("attribution", UNSET)

        automated = d.pop("automated", UNSET)

        not_a_warning = d.pop("notAWarning", UNSET)

        forecast_area_payload = cls(
            model=model,
            field=field,
            threshold=threshold,
            first_exceed_at=first_exceed_at,
            run=run,
            peak_value=peak_value,
            peak_at=peak_at,
            area_km_2=area_km_2,
            attribution=attribution,
            automated=automated,
            not_a_warning=not_a_warning,
        )

        forecast_area_payload.additional_properties = d
        return forecast_area_payload

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
