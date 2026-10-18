from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.regional_index_value_components_item import (
        RegionalIndexValueComponentsItem,
    )


T = TypeVar("T", bound="RegionalIndexValue")


@_attrs_define
class RegionalIndexValue:
    """Daily index value for a region with its decomposition

    Attributes:
        index_id (str):
        region (str):
        day (datetime.date):
        value (float):
        components (list[RegionalIndexValueComponentsItem]):
        as_of_seq (int):
        floors_applied (list[str] | Unset):
        delta24h (float | None | Unset):
        delta7d (float | None | Unset):
        weights_version (int | Unset):
        manifest_id (None | str | Unset):
        record_ref (None | str | Unset):
    """

    index_id: str
    region: str
    day: datetime.date
    value: float
    components: list[RegionalIndexValueComponentsItem]
    as_of_seq: int
    floors_applied: list[str] | Unset = UNSET
    delta24h: float | None | Unset = UNSET
    delta7d: float | None | Unset = UNSET
    weights_version: int | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    record_ref: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index_id = self.index_id

        region = self.region

        day = self.day.isoformat()

        value = self.value

        components = []
        for components_item_data in self.components:
            components_item = components_item_data.to_dict()
            components.append(components_item)

        as_of_seq = self.as_of_seq

        floors_applied: list[str] | Unset = UNSET
        if not isinstance(self.floors_applied, Unset):
            floors_applied = self.floors_applied

        delta24h: float | None | Unset
        if isinstance(self.delta24h, Unset):
            delta24h = UNSET
        else:
            delta24h = self.delta24h

        delta7d: float | None | Unset
        if isinstance(self.delta7d, Unset):
            delta7d = UNSET
        else:
            delta7d = self.delta7d

        weights_version = self.weights_version

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        record_ref: None | str | Unset
        if isinstance(self.record_ref, Unset):
            record_ref = UNSET
        else:
            record_ref = self.record_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "indexId": index_id,
                "region": region,
                "day": day,
                "value": value,
                "components": components,
                "asOfSeq": as_of_seq,
            }
        )
        if floors_applied is not UNSET:
            field_dict["floorsApplied"] = floors_applied
        if delta24h is not UNSET:
            field_dict["delta24h"] = delta24h
        if delta7d is not UNSET:
            field_dict["delta7d"] = delta7d
        if weights_version is not UNSET:
            field_dict["weightsVersion"] = weights_version
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.regional_index_value_components_item import (
            RegionalIndexValueComponentsItem,
        )

        d = dict(src_dict)
        index_id = d.pop("indexId")

        region = d.pop("region")

        day = datetime.date.fromisoformat(d.pop("day"))

        value = d.pop("value")

        components = []
        _components = d.pop("components")
        for components_item_data in _components:
            components_item = RegionalIndexValueComponentsItem.from_dict(
                components_item_data
            )

            components.append(components_item)

        as_of_seq = d.pop("asOfSeq")

        floors_applied = cast(list[str], d.pop("floorsApplied", UNSET))

        def _parse_delta24h(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        delta24h = _parse_delta24h(d.pop("delta24h", UNSET))

        def _parse_delta7d(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        delta7d = _parse_delta7d(d.pop("delta7d", UNSET))

        weights_version = d.pop("weightsVersion", UNSET)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        def _parse_record_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ref = _parse_record_ref(d.pop("recordRef", UNSET))

        regional_index_value = cls(
            index_id=index_id,
            region=region,
            day=day,
            value=value,
            components=components,
            as_of_seq=as_of_seq,
            floors_applied=floors_applied,
            delta24h=delta24h,
            delta7d=delta7d,
            weights_version=weights_version,
            manifest_id=manifest_id,
            record_ref=record_ref,
        )

        regional_index_value.additional_properties = d
        return regional_index_value

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
