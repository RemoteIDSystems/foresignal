from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_selector import EventSelector


T = TypeVar("T", bound="PrecursorSide")


@_attrs_define
class PrecursorSide:
    """Antecedent or consequent of a precursor: an event selector or a series

    Attributes:
        label (str): Display label
        selector (EventSelector | None | Unset):
        series_ref (None | str | Unset):
        family (None | str | Unset): Event family or series family
        threshold (float | None | Unset): Series antecedents: standardised threshold that counts as an occurrence
            (default 2.0)
    """

    label: str
    selector: EventSelector | None | Unset = UNSET
    series_ref: None | str | Unset = UNSET
    family: None | str | Unset = UNSET
    threshold: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.event_selector import EventSelector

        label = self.label

        selector: dict[str, Any] | None | Unset
        if isinstance(self.selector, Unset):
            selector = UNSET
        elif isinstance(self.selector, EventSelector):
            selector = self.selector.to_dict()
        else:
            selector = self.selector

        series_ref: None | str | Unset
        if isinstance(self.series_ref, Unset):
            series_ref = UNSET
        else:
            series_ref = self.series_ref

        family: None | str | Unset
        if isinstance(self.family, Unset):
            family = UNSET
        else:
            family = self.family

        threshold: float | None | Unset
        if isinstance(self.threshold, Unset):
            threshold = UNSET
        else:
            threshold = self.threshold

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "label": label,
            }
        )
        if selector is not UNSET:
            field_dict["selector"] = selector
        if series_ref is not UNSET:
            field_dict["seriesRef"] = series_ref
        if family is not UNSET:
            field_dict["family"] = family
        if threshold is not UNSET:
            field_dict["threshold"] = threshold

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_selector import EventSelector

        d = dict(src_dict)
        label = d.pop("label")

        def _parse_selector(data: object) -> EventSelector | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                selector_type_0 = EventSelector.from_dict(data)

                return selector_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(EventSelector | None | Unset, data)

        selector = _parse_selector(d.pop("selector", UNSET))

        def _parse_series_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        series_ref = _parse_series_ref(d.pop("seriesRef", UNSET))

        def _parse_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        family = _parse_family(d.pop("family", UNSET))

        def _parse_threshold(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        threshold = _parse_threshold(d.pop("threshold", UNSET))

        precursor_side = cls(
            label=label,
            selector=selector,
            series_ref=series_ref,
            family=family,
            threshold=threshold,
        )

        precursor_side.additional_properties = d
        return precursor_side

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
