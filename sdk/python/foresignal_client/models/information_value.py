from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.information_value_indicators_item import (
        InformationValueIndicatorsItem,
    )


T = TypeVar("T", bound="InformationValue")


@_attrs_define
class InformationValue:
    """Pending indicators ranked by expected information gain (`InformationValue` 1.0.0)

    Attributes:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        computed_at (datetime.datetime):
        entropy (float): H(P) of the set's current distribution (nats)
        indicators (list[InformationValueIndicatorsItem]):
        version (str | Unset):  Example: InformationValue 1.0.0.
    """

    set_id: str
    computed_at: datetime.datetime
    entropy: float
    indicators: list[InformationValueIndicatorsItem]
    version: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        set_id = self.set_id

        computed_at = self.computed_at.isoformat()

        entropy = self.entropy

        indicators = []
        for indicators_item_data in self.indicators:
            indicators_item = indicators_item_data.to_dict()
            indicators.append(indicators_item)

        version = self.version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "setId": set_id,
                "computedAt": computed_at,
                "entropy": entropy,
                "indicators": indicators,
            }
        )
        if version is not UNSET:
            field_dict["version"] = version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.information_value_indicators_item import (
            InformationValueIndicatorsItem,
        )

        d = dict(src_dict)
        set_id = d.pop("setId")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        entropy = d.pop("entropy")

        indicators = []
        _indicators = d.pop("indicators")
        for indicators_item_data in _indicators:
            indicators_item = InformationValueIndicatorsItem.from_dict(
                indicators_item_data
            )

            indicators.append(indicators_item)

        version = d.pop("version", UNSET)

        information_value = cls(
            set_id=set_id,
            computed_at=computed_at,
            entropy=entropy,
            indicators=indicators,
            version=version,
        )

        information_value.additional_properties = d
        return information_value

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
