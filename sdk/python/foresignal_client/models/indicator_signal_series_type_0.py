from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_signal_series_type_0_direction_type_1 import (
    IndicatorSignalSeriesType0DirectionType1,
)
from ..models.indicator_signal_series_type_0_direction_type_2_type_1 import (
    IndicatorSignalSeriesType0DirectionType2Type1,
)
from ..models.indicator_signal_series_type_0_direction_type_3_type_1 import (
    IndicatorSignalSeriesType0DirectionType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_signal_series_type_0_condition_type_0 import (
        IndicatorSignalSeriesType0ConditionType0,
    )


T = TypeVar("T", bound="IndicatorSignalSeriesType0")


@_attrs_define
class IndicatorSignalSeriesType0:
    """Series condition or change point

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
        condition (IndicatorSignalSeriesType0ConditionType0 | None | Unset):
        change_point (bool | Unset):  Default: False.
        direction (IndicatorSignalSeriesType0DirectionType1 | IndicatorSignalSeriesType0DirectionType2Type1 |
            IndicatorSignalSeriesType0DirectionType3Type1 | None | Unset):
    """

    ref: str
    condition: IndicatorSignalSeriesType0ConditionType0 | None | Unset = UNSET
    change_point: bool | Unset = False
    direction: (
        IndicatorSignalSeriesType0DirectionType1
        | IndicatorSignalSeriesType0DirectionType2Type1
        | IndicatorSignalSeriesType0DirectionType3Type1
        | None
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.indicator_signal_series_type_0_condition_type_0 import (
            IndicatorSignalSeriesType0ConditionType0,
        )

        ref = self.ref

        condition: dict[str, Any] | None | Unset
        if isinstance(self.condition, Unset):
            condition = UNSET
        elif isinstance(self.condition, IndicatorSignalSeriesType0ConditionType0):
            condition = self.condition.to_dict()
        else:
            condition = self.condition

        change_point = self.change_point

        direction: None | str | Unset
        if isinstance(self.direction, Unset):
            direction = UNSET
        elif (
            isinstance(self.direction, IndicatorSignalSeriesType0DirectionType1)
            or isinstance(self.direction, IndicatorSignalSeriesType0DirectionType2Type1)
            or isinstance(self.direction, IndicatorSignalSeriesType0DirectionType3Type1)
        ):
            direction = self.direction.value
        else:
            direction = self.direction

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
            }
        )
        if condition is not UNSET:
            field_dict["condition"] = condition
        if change_point is not UNSET:
            field_dict["changePoint"] = change_point
        if direction is not UNSET:
            field_dict["direction"] = direction

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_signal_series_type_0_condition_type_0 import (
            IndicatorSignalSeriesType0ConditionType0,
        )

        d = dict(src_dict)
        ref = d.pop("ref")

        def _parse_condition(
            data: object,
        ) -> IndicatorSignalSeriesType0ConditionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                condition_type_0 = IndicatorSignalSeriesType0ConditionType0.from_dict(
                    data
                )

                return condition_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalSeriesType0ConditionType0 | None | Unset, data)

        condition = _parse_condition(d.pop("condition", UNSET))

        change_point = d.pop("changePoint", UNSET)

        def _parse_direction(
            data: object,
        ) -> (
            IndicatorSignalSeriesType0DirectionType1
            | IndicatorSignalSeriesType0DirectionType2Type1
            | IndicatorSignalSeriesType0DirectionType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                direction_type_1 = IndicatorSignalSeriesType0DirectionType1(data)

                return direction_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                direction_type_2_type_1 = IndicatorSignalSeriesType0DirectionType2Type1(
                    data
                )

                return direction_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                direction_type_3_type_1 = IndicatorSignalSeriesType0DirectionType3Type1(
                    data
                )

                return direction_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                IndicatorSignalSeriesType0DirectionType1
                | IndicatorSignalSeriesType0DirectionType2Type1
                | IndicatorSignalSeriesType0DirectionType3Type1
                | None
                | Unset,
                data,
            )

        direction = _parse_direction(d.pop("direction", UNSET))

        indicator_signal_series_type_0 = cls(
            ref=ref,
            condition=condition,
            change_point=change_point,
            direction=direction,
        )

        indicator_signal_series_type_0.additional_properties = d
        return indicator_signal_series_type_0

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
