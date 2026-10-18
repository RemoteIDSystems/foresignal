from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleBacktestResultTopEntitiesItem")


@_attrs_define
class RuleBacktestResultTopEntitiesItem:
    """Entity with the most hits

    Attributes:
        count (int):
        source_id (str | Unset):
        external_id (str | Unset):
        entity_id (None | str | Unset):
    """

    count: int
    source_id: str | Unset = UNSET
    external_id: str | Unset = UNSET
    entity_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        count = self.count

        source_id = self.source_id

        external_id = self.external_id

        entity_id: None | str | Unset
        if isinstance(self.entity_id, Unset):
            entity_id = UNSET
        else:
            entity_id = self.entity_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "count": count,
            }
        )
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if entity_id is not UNSET:
            field_dict["entityId"] = entity_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        count = d.pop("count")

        source_id = d.pop("sourceId", UNSET)

        external_id = d.pop("externalId", UNSET)

        def _parse_entity_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        entity_id = _parse_entity_id(d.pop("entityId", UNSET))

        rule_backtest_result_top_entities_item = cls(
            count=count,
            source_id=source_id,
            external_id=external_id,
            entity_id=entity_id,
        )

        rule_backtest_result_top_entities_item.additional_properties = d
        return rule_backtest_result_top_entities_item

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
