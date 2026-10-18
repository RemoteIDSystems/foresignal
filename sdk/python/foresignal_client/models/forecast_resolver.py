from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_resolver_aggregate import ForecastResolverAggregate
from ..models.forecast_resolver_op import ForecastResolverOp
from ..models.forecast_resolver_type import ForecastResolverType
from ..models.severity import Severity
from ..models.situation_status import SituationStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_selector import EventSelector
    from ..models.resolver_area import ResolverArea
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="ForecastResolver")


@_attrs_define
class ForecastResolver:
    """Machine resolver (pure, versioned, validated) or `{type: manual}`; shown verbatim to every forecaster

    Attributes:
        type_ (ForecastResolverType):
        selector (EventSelector | Unset): Selects event occurrences: one of 103's selector forms (`rule`, `alertRuleId`,
            `intelEventType`, `anomalyRuleId` + `classification`) or a 118-121 addition (`alertRuleTag`,
            `situationActivated`, `changePoint`, `unexplainedMove`, `referenceFamily`, `timestamps`); exactly one form per
            selector, optionally narrowed by `regionCodes` and `minSeverity`.
        area (ResolverArea | Unset): Area for a resolver: one of bbox, polygon, place or regions
        window (TimeRange | Unset):
        op (ForecastResolverOp | Unset): count-threshold, series-value (omit op and threshold for a numeric question)
        threshold (float | Unset): count-threshold, series-value (omit op and threshold for a numeric question: a
            series-value aggregate, or since 126 a count-threshold count; a referenceFamily selector then counts the events
            in its reference history, one source per region)
        ref (str | Unset): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        period (str | Unset): series-value: a single period, e.g. `2026-11` or `2026-W45`
        aggregate (ForecastResolverAggregate | Unset): series-value
        rule_id (str | Unset): alert-fired: 017 rule id
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (SituationStatus | Unset): Situation lifecycle status; `closed` is final
        min_severity (Severity | Unset):
        set_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        version (str | Unset): Resolver version (server-set)
    """

    type_: ForecastResolverType
    selector: EventSelector | Unset = UNSET
    area: ResolverArea | Unset = UNSET
    window: TimeRange | Unset = UNSET
    op: ForecastResolverOp | Unset = UNSET
    threshold: float | Unset = UNSET
    ref: str | Unset = UNSET
    period: str | Unset = UNSET
    aggregate: ForecastResolverAggregate | Unset = UNSET
    rule_id: str | Unset = UNSET
    situation_id: str | Unset = UNSET
    status: SituationStatus | Unset = UNSET
    min_severity: Severity | Unset = UNSET
    set_id: str | Unset = UNSET
    version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        selector: dict[str, Any] | Unset = UNSET
        if not isinstance(self.selector, Unset):
            selector = self.selector.to_dict()

        area: dict[str, Any] | Unset = UNSET
        if not isinstance(self.area, Unset):
            area = self.area.to_dict()

        window: dict[str, Any] | Unset = UNSET
        if not isinstance(self.window, Unset):
            window = self.window.to_dict()

        op: str | Unset = UNSET
        if not isinstance(self.op, Unset):
            op = self.op.value

        threshold = self.threshold

        ref = self.ref

        period = self.period

        aggregate: str | Unset = UNSET
        if not isinstance(self.aggregate, Unset):
            aggregate = self.aggregate.value

        rule_id = self.rule_id

        situation_id = self.situation_id

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        set_id = self.set_id

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if selector is not UNSET:
            field_dict["selector"] = selector
        if area is not UNSET:
            field_dict["area"] = area
        if window is not UNSET:
            field_dict["window"] = window
        if op is not UNSET:
            field_dict["op"] = op
        if threshold is not UNSET:
            field_dict["threshold"] = threshold
        if ref is not UNSET:
            field_dict["ref"] = ref
        if period is not UNSET:
            field_dict["period"] = period
        if aggregate is not UNSET:
            field_dict["aggregate"] = aggregate
        if rule_id is not UNSET:
            field_dict["ruleId"] = rule_id
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if status is not UNSET:
            field_dict["status"] = status
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if set_id is not UNSET:
            field_dict["setId"] = set_id
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_selector import EventSelector
        from ..models.resolver_area import ResolverArea
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        type_ = ForecastResolverType(d.pop("type"))

        _selector = d.pop("selector", UNSET)
        selector: EventSelector | Unset
        if isinstance(_selector, Unset):
            selector = UNSET
        else:
            selector = EventSelector.from_dict(_selector)

        _area = d.pop("area", UNSET)
        area: ResolverArea | Unset
        if isinstance(_area, Unset):
            area = UNSET
        else:
            area = ResolverArea.from_dict(_area)

        _window = d.pop("window", UNSET)
        window: TimeRange | Unset
        if isinstance(_window, Unset):
            window = UNSET
        else:
            window = TimeRange.from_dict(_window)

        _op = d.pop("op", UNSET)
        op: ForecastResolverOp | Unset
        if isinstance(_op, Unset):
            op = UNSET
        else:
            op = ForecastResolverOp(_op)

        threshold = d.pop("threshold", UNSET)

        ref = d.pop("ref", UNSET)

        period = d.pop("period", UNSET)

        _aggregate = d.pop("aggregate", UNSET)
        aggregate: ForecastResolverAggregate | Unset
        if isinstance(_aggregate, Unset):
            aggregate = UNSET
        else:
            aggregate = ForecastResolverAggregate(_aggregate)

        rule_id = d.pop("ruleId", UNSET)

        situation_id = d.pop("situationId", UNSET)

        _status = d.pop("status", UNSET)
        status: SituationStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = SituationStatus(_status)

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: Severity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = Severity(_min_severity)

        set_id = d.pop("setId", UNSET)

        version = d.pop("version", UNSET)

        forecast_resolver = cls(
            type_=type_,
            selector=selector,
            area=area,
            window=window,
            op=op,
            threshold=threshold,
            ref=ref,
            period=period,
            aggregate=aggregate,
            rule_id=rule_id,
            situation_id=situation_id,
            status=status,
            min_severity=min_severity,
            set_id=set_id,
            version=version,
        )

        forecast_resolver.additional_properties = d
        return forecast_resolver

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
