from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precision_components_item_component import (
    PrecisionComponentsItemComponent,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precision_components_item_interval_type_0 import (
        PrecisionComponentsItemIntervalType0,
    )


T = TypeVar("T", bound="PrecisionComponentsItem")


@_attrs_define
class PrecisionComponentsItem:
    """Precision for one producer

    Attributes:
        component (PrecisionComponentsItemComponent):
        key (str): Detector, recipe or linker id and version Example: cusum 1.0.0.
        confirmed (int):
        rejected (int):
        labelled (int):
        duplicate (int | Unset):
        needs_review (int | Unset): Counted as unlabelled
        precision (float | None | Unset):
        interval (None | PrecisionComponentsItemIntervalType0 | Unset):
    """

    component: PrecisionComponentsItemComponent
    key: str
    confirmed: int
    rejected: int
    labelled: int
    duplicate: int | Unset = UNSET
    needs_review: int | Unset = UNSET
    precision: float | None | Unset = UNSET
    interval: None | PrecisionComponentsItemIntervalType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.precision_components_item_interval_type_0 import (
            PrecisionComponentsItemIntervalType0,
        )

        component = self.component.value

        key = self.key

        confirmed = self.confirmed

        rejected = self.rejected

        labelled = self.labelled

        duplicate = self.duplicate

        needs_review = self.needs_review

        precision: float | None | Unset
        if isinstance(self.precision, Unset):
            precision = UNSET
        else:
            precision = self.precision

        interval: dict[str, Any] | None | Unset
        if isinstance(self.interval, Unset):
            interval = UNSET
        elif isinstance(self.interval, PrecisionComponentsItemIntervalType0):
            interval = self.interval.to_dict()
        else:
            interval = self.interval

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "component": component,
                "key": key,
                "confirmed": confirmed,
                "rejected": rejected,
                "labelled": labelled,
            }
        )
        if duplicate is not UNSET:
            field_dict["duplicate"] = duplicate
        if needs_review is not UNSET:
            field_dict["needsReview"] = needs_review
        if precision is not UNSET:
            field_dict["precision"] = precision
        if interval is not UNSET:
            field_dict["interval"] = interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precision_components_item_interval_type_0 import (
            PrecisionComponentsItemIntervalType0,
        )

        d = dict(src_dict)
        component = PrecisionComponentsItemComponent(d.pop("component"))

        key = d.pop("key")

        confirmed = d.pop("confirmed")

        rejected = d.pop("rejected")

        labelled = d.pop("labelled")

        duplicate = d.pop("duplicate", UNSET)

        needs_review = d.pop("needsReview", UNSET)

        def _parse_precision(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        precision = _parse_precision(d.pop("precision", UNSET))

        def _parse_interval(
            data: object,
        ) -> None | PrecisionComponentsItemIntervalType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_type_0 = PrecisionComponentsItemIntervalType0.from_dict(data)

                return interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrecisionComponentsItemIntervalType0 | Unset, data)

        interval = _parse_interval(d.pop("interval", UNSET))

        precision_components_item = cls(
            component=component,
            key=key,
            confirmed=confirmed,
            rejected=rejected,
            labelled=labelled,
            duplicate=duplicate,
            needs_review=needs_review,
            precision=precision,
            interval=interval,
        )

        precision_components_item.additional_properties = d
        return precision_components_item

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
