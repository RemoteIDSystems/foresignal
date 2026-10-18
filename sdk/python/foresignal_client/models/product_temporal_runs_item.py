from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_temporal_runs_item_status import ProductTemporalRunsItemStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductTemporalRunsItem")


@_attrs_define
class ProductTemporalRunsItem:
    """Forecast run summary

    Attributes:
        run (str): Run reference time (ISO-8601), usable as `run=` Example: 2026-10-10T12:00:00Z.
        status (ProductTemporalRunsItemStatus):
        steps (list[int] | Unset): Forecast steps (hours) present
        valid_from (datetime.datetime | Unset):
        valid_to (datetime.datetime | Unset):
    """

    run: str
    status: ProductTemporalRunsItemStatus
    steps: list[int] | Unset = UNSET
    valid_from: datetime.datetime | Unset = UNSET
    valid_to: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        run = self.run

        status = self.status.value

        steps: list[int] | Unset = UNSET
        if not isinstance(self.steps, Unset):
            steps = self.steps

        valid_from: str | Unset = UNSET
        if not isinstance(self.valid_from, Unset):
            valid_from = self.valid_from.isoformat()

        valid_to: str | Unset = UNSET
        if not isinstance(self.valid_to, Unset):
            valid_to = self.valid_to.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "run": run,
                "status": status,
            }
        )
        if steps is not UNSET:
            field_dict["steps"] = steps
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if valid_to is not UNSET:
            field_dict["validTo"] = valid_to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        run = d.pop("run")

        status = ProductTemporalRunsItemStatus(d.pop("status"))

        steps = cast(list[int], d.pop("steps", UNSET))

        _valid_from = d.pop("validFrom", UNSET)
        valid_from: datetime.datetime | Unset
        if isinstance(_valid_from, Unset):
            valid_from = UNSET
        else:
            valid_from = datetime.datetime.fromisoformat(_valid_from)

        _valid_to = d.pop("validTo", UNSET)
        valid_to: datetime.datetime | Unset
        if isinstance(_valid_to, Unset):
            valid_to = UNSET
        else:
            valid_to = datetime.datetime.fromisoformat(_valid_to)

        product_temporal_runs_item = cls(
            run=run,
            status=status,
            steps=steps,
            valid_from=valid_from,
            valid_to=valid_to,
        )

        product_temporal_runs_item.additional_properties = d
        return product_temporal_runs_item

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
