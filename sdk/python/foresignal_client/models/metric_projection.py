from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.metric_projection_labels_item import MetricProjectionLabelsItem


T = TypeVar("T", bound="MetricProjection")


@_attrs_define
class MetricProjection:
    """A payload field exposed as a Prometheus gauge `foresignal_data_<name>` (038)

    Attributes:
        name (str):
        source (str):
        path (str): 017 trigger path syntax; must not be a volatile field
        kind (None | str | Unset):
        labels (list[MetricProjectionLabelsItem] | Unset): Default labels source and external_id; entities with the same
            label values share one series (the latest write wins)
        unit (None | str | Unset):
        help_ (None | str | Unset):
        max_series (int | Unset):  Default: 500.
        allow_private (bool | Unset):  Default: False.
    """

    name: str
    source: str
    path: str
    kind: None | str | Unset = UNSET
    labels: list[MetricProjectionLabelsItem] | Unset = UNSET
    unit: None | str | Unset = UNSET
    help_: None | str | Unset = UNSET
    max_series: int | Unset = 500
    allow_private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        source = self.source

        path = self.path

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        labels: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.labels, Unset):
            labels = []
            for labels_item_data in self.labels:
                labels_item = labels_item_data.to_dict()
                labels.append(labels_item)

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        help_: None | str | Unset
        if isinstance(self.help_, Unset):
            help_ = UNSET
        else:
            help_ = self.help_

        max_series = self.max_series

        allow_private = self.allow_private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "source": source,
                "path": path,
            }
        )
        if kind is not UNSET:
            field_dict["kind"] = kind
        if labels is not UNSET:
            field_dict["labels"] = labels
        if unit is not UNSET:
            field_dict["unit"] = unit
        if help_ is not UNSET:
            field_dict["help"] = help_
        if max_series is not UNSET:
            field_dict["maxSeries"] = max_series
        if allow_private is not UNSET:
            field_dict["allowPrivate"] = allow_private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.metric_projection_labels_item import MetricProjectionLabelsItem

        d = dict(src_dict)
        name = d.pop("name")

        source = d.pop("source")

        path = d.pop("path")

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        _labels = d.pop("labels", UNSET)
        labels: list[MetricProjectionLabelsItem] | Unset = UNSET
        if _labels is not UNSET:
            labels = []
            for labels_item_data in _labels:
                labels_item = MetricProjectionLabelsItem.from_dict(labels_item_data)

                labels.append(labels_item)

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        def _parse_help_(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        help_ = _parse_help_(d.pop("help", UNSET))

        max_series = d.pop("maxSeries", UNSET)

        allow_private = d.pop("allowPrivate", UNSET)

        metric_projection = cls(
            name=name,
            source=source,
            path=path,
            kind=kind,
            labels=labels,
            unit=unit,
            help_=help_,
            max_series=max_series,
            allow_private=allow_private,
        )

        metric_projection.additional_properties = d
        return metric_projection

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
