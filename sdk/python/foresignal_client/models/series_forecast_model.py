from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_forecast_model_method import SeriesForecastModelMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_forecast_model_backtest import SeriesForecastModelBacktest
    from ..models.series_forecast_model_params import SeriesForecastModelParams


T = TypeVar("T", bound="SeriesForecastModel")


@_attrs_define
class SeriesForecastModel:
    """Selected forecasting method for a series and its rolling-origin backtest

    Attributes:
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        method (SeriesForecastModelMethod):
        backtest (SeriesForecastModelBacktest): Rolling-origin backtest (last 8 weekly origins)
        selected_at (datetime.datetime):
        version (str):  Example: series-forecast 1.0.0.
        params (SeriesForecastModelParams | Unset):
        next_selection_at (datetime.datetime | None | Unset):
        manifest_id (None | str | Unset):
    """

    ref: str
    method: SeriesForecastModelMethod
    backtest: SeriesForecastModelBacktest
    selected_at: datetime.datetime
    version: str
    params: SeriesForecastModelParams | Unset = UNSET
    next_selection_at: datetime.datetime | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        method = self.method.value

        backtest = self.backtest.to_dict()

        selected_at = self.selected_at.isoformat()

        version = self.version

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        next_selection_at: None | str | Unset
        if isinstance(self.next_selection_at, Unset):
            next_selection_at = UNSET
        elif isinstance(self.next_selection_at, datetime.datetime):
            next_selection_at = self.next_selection_at.isoformat()
        else:
            next_selection_at = self.next_selection_at

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "method": method,
                "backtest": backtest,
                "selectedAt": selected_at,
                "version": version,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params
        if next_selection_at is not UNSET:
            field_dict["nextSelectionAt"] = next_selection_at
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_forecast_model_backtest import SeriesForecastModelBacktest
        from ..models.series_forecast_model_params import SeriesForecastModelParams

        d = dict(src_dict)
        ref = d.pop("ref")

        method = SeriesForecastModelMethod(d.pop("method"))

        backtest = SeriesForecastModelBacktest.from_dict(d.pop("backtest"))

        selected_at = datetime.datetime.fromisoformat(d.pop("selectedAt"))

        version = d.pop("version")

        _params = d.pop("params", UNSET)
        params: SeriesForecastModelParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = SeriesForecastModelParams.from_dict(_params)

        def _parse_next_selection_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_selection_at_type_0 = datetime.datetime.fromisoformat(data)

                return next_selection_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_selection_at = _parse_next_selection_at(d.pop("nextSelectionAt", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        series_forecast_model = cls(
            ref=ref,
            method=method,
            backtest=backtest,
            selected_at=selected_at,
            version=version,
            params=params,
            next_selection_at=next_selection_at,
            manifest_id=manifest_id,
        )

        series_forecast_model.additional_properties = d
        return series_forecast_model

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
