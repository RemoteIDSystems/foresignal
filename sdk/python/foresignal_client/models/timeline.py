from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.timeline_backend import TimelineBackend
from ..models.timeline_group_by import TimelineGroupBy
from ..models.timeline_measure import TimelineMeasure
from ..models.timeline_time_field import TimelineTimeField
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.timeline_alerts_item import TimelineAlertsItem
    from ..models.timeline_bucket import TimelineBucket


T = TypeVar("T", bound="Timeline")


@_attrs_define
class Timeline:
    """Event-density histogram for scrubbers (037)

    Attributes:
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str):
        time_field (TimelineTimeField):
        buckets (list[TimelineBucket]):
        measure (TimelineMeasure | Unset):
        group_by (TimelineGroupBy | Unset):
        alerts (list[TimelineAlertsItem] | Unset): Alert counts per bucket by severity, parallel to `buckets` (only with
            includeAlerts)
        max_bucket (int | Unset):
        approximate (bool | Unset):
        retained_from (datetime.datetime | None | Unset):
        backend (TimelineBackend | Unset): 038 serves ingested ranges older than retention from the rollup
    """

    from_: datetime.datetime
    to: datetime.datetime
    bucket: str
    time_field: TimelineTimeField
    buckets: list[TimelineBucket]
    measure: TimelineMeasure | Unset = UNSET
    group_by: TimelineGroupBy | Unset = UNSET
    alerts: list[TimelineAlertsItem] | Unset = UNSET
    max_bucket: int | Unset = UNSET
    approximate: bool | Unset = UNSET
    retained_from: datetime.datetime | None | Unset = UNSET
    backend: TimelineBackend | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        bucket = self.bucket

        time_field = self.time_field.value

        buckets = []
        for buckets_item_data in self.buckets:
            buckets_item = buckets_item_data.to_dict()
            buckets.append(buckets_item)

        measure: str | Unset = UNSET
        if not isinstance(self.measure, Unset):
            measure = self.measure.value

        group_by: str | Unset = UNSET
        if not isinstance(self.group_by, Unset):
            group_by = self.group_by.value

        alerts: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.alerts, Unset):
            alerts = []
            for alerts_item_data in self.alerts:
                alerts_item = alerts_item_data.to_dict()
                alerts.append(alerts_item)

        max_bucket = self.max_bucket

        approximate = self.approximate

        retained_from: None | str | Unset
        if isinstance(self.retained_from, Unset):
            retained_from = UNSET
        elif isinstance(self.retained_from, datetime.datetime):
            retained_from = self.retained_from.isoformat()
        else:
            retained_from = self.retained_from

        backend: str | Unset = UNSET
        if not isinstance(self.backend, Unset):
            backend = self.backend.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "bucket": bucket,
                "timeField": time_field,
                "buckets": buckets,
            }
        )
        if measure is not UNSET:
            field_dict["measure"] = measure
        if group_by is not UNSET:
            field_dict["groupBy"] = group_by
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if max_bucket is not UNSET:
            field_dict["maxBucket"] = max_bucket
        if approximate is not UNSET:
            field_dict["approximate"] = approximate
        if retained_from is not UNSET:
            field_dict["retainedFrom"] = retained_from
        if backend is not UNSET:
            field_dict["backend"] = backend

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.timeline_alerts_item import TimelineAlertsItem
        from ..models.timeline_bucket import TimelineBucket

        d = dict(src_dict)
        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        bucket = d.pop("bucket")

        time_field = TimelineTimeField(d.pop("timeField"))

        buckets = []
        _buckets = d.pop("buckets")
        for buckets_item_data in _buckets:
            buckets_item = TimelineBucket.from_dict(buckets_item_data)

            buckets.append(buckets_item)

        _measure = d.pop("measure", UNSET)
        measure: TimelineMeasure | Unset
        if isinstance(_measure, Unset):
            measure = UNSET
        else:
            measure = TimelineMeasure(_measure)

        _group_by = d.pop("groupBy", UNSET)
        group_by: TimelineGroupBy | Unset
        if isinstance(_group_by, Unset):
            group_by = UNSET
        else:
            group_by = TimelineGroupBy(_group_by)

        _alerts = d.pop("alerts", UNSET)
        alerts: list[TimelineAlertsItem] | Unset = UNSET
        if _alerts is not UNSET:
            alerts = []
            for alerts_item_data in _alerts:
                alerts_item = TimelineAlertsItem.from_dict(alerts_item_data)

                alerts.append(alerts_item)

        max_bucket = d.pop("maxBucket", UNSET)

        approximate = d.pop("approximate", UNSET)

        def _parse_retained_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                retained_from_type_0 = datetime.datetime.fromisoformat(data)

                return retained_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        retained_from = _parse_retained_from(d.pop("retainedFrom", UNSET))

        _backend = d.pop("backend", UNSET)
        backend: TimelineBackend | Unset
        if isinstance(_backend, Unset):
            backend = UNSET
        else:
            backend = TimelineBackend(_backend)

        timeline = cls(
            from_=from_,
            to=to,
            bucket=bucket,
            time_field=time_field,
            buckets=buckets,
            measure=measure,
            group_by=group_by,
            alerts=alerts,
            max_bucket=max_bucket,
            approximate=approximate,
            retained_from=retained_from,
            backend=backend,
        )

        timeline.additional_properties = d
        return timeline

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
