from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_selector import EventSelector


T = TypeVar("T", bound="EventStudyRequest")


@_attrs_define
class EventStudyRequest:
    """Batch event study: events from a selector against one target series (103 job `event-study`)

    Attributes:
        selector (EventSelector): Selects event occurrences: one of 103's selector forms (`rule`, `alertRuleId`,
            `intelEventType`, `anomalyRuleId` + `classification`) or a 118-121 addition (`alertRuleTag`,
            `situationActivated`, `changePoint`, `unexplainedMove`, `referenceFamily`, `timestamps`); exactly one form per
            selector, optionally narrowed by `regionCodes` and `minSeverity`.
        target (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        from_ (datetime.datetime): Start of the event range
        to (datetime.datetime): End of the event range
        name (None | str | Unset): Optional label
        as_of (datetime.datetime | None | Unset): Knowledge cutoff for the replay (default: submit time)
    """

    selector: EventSelector
    target: str
    from_: datetime.datetime
    to: datetime.datetime
    name: None | str | Unset = UNSET
    as_of: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        selector = self.selector.to_dict()

        target = self.target

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "selector": selector,
                "target": target,
                "from": from_,
                "to": to,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if as_of is not UNSET:
            field_dict["asOf"] = as_of

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_selector import EventSelector

        d = dict(src_dict)
        selector = EventSelector.from_dict(d.pop("selector"))

        target = d.pop("target")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        event_study_request = cls(
            selector=selector,
            target=target,
            from_=from_,
            to=to,
            name=name,
            as_of=as_of,
        )

        event_study_request.additional_properties = d
        return event_study_request

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
