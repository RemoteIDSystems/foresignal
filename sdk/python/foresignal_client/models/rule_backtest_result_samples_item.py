from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.rule_backtest_result_samples_item_edge import (
    RuleBacktestResultSamplesItemEdge,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleBacktestResultSamplesItem")


@_attrs_define
class RuleBacktestResultSamplesItem:
    """Sample hit

    Attributes:
        seq (int):
        source (str):
        external_id (str):
        observed_at (datetime.datetime):
        edge (RuleBacktestResultSamplesItemEdge | Unset):
    """

    seq: int
    source: str
    external_id: str
    observed_at: datetime.datetime
    edge: RuleBacktestResultSamplesItemEdge | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        seq = self.seq

        source = self.source

        external_id = self.external_id

        observed_at = self.observed_at.isoformat()

        edge: str | Unset = UNSET
        if not isinstance(self.edge, Unset):
            edge = self.edge.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "seq": seq,
                "source": source,
                "externalId": external_id,
                "observedAt": observed_at,
            }
        )
        if edge is not UNSET:
            field_dict["edge"] = edge

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        seq = d.pop("seq")

        source = d.pop("source")

        external_id = d.pop("externalId")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        _edge = d.pop("edge", UNSET)
        edge: RuleBacktestResultSamplesItemEdge | Unset
        if isinstance(_edge, Unset):
            edge = UNSET
        else:
            edge = RuleBacktestResultSamplesItemEdge(_edge)

        rule_backtest_result_samples_item = cls(
            seq=seq,
            source=source,
            external_id=external_id,
            observed_at=observed_at,
            edge=edge,
        )

        rule_backtest_result_samples_item.additional_properties = d
        return rule_backtest_result_samples_item

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
