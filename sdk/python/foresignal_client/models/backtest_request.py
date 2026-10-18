from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.backtest_request_type import BacktestRequestType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.backtest_request_definition import BacktestRequestDefinition
    from ..models.backtest_request_variants_item import BacktestRequestVariantsItem


T = TypeVar("T", bound="BacktestRequest")


@_attrs_define
class BacktestRequest:
    """Backtest submission: a target id or an inline definition, a time or seq range, and up to 5 rule variants

    Attributes:
        type_ (BacktestRequestType):
        target_id (str | Unset): Rule id, recipe id or precursor test id
        definition (BacktestRequestDefinition | Unset): Inline definition (TriggerRuleInput, recipe definition or
            PrecursorTestInput) instead of targetId
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        variants (list[BacktestRequestVariantsItem] | Unset): Rule backtests only; run in the same pass
    """

    type_: BacktestRequestType
    target_id: str | Unset = UNSET
    definition: BacktestRequestDefinition | Unset = UNSET
    from_: datetime.datetime | Unset = UNSET
    to: datetime.datetime | Unset = UNSET
    from_seq: int | Unset = UNSET
    to_seq: int | Unset = UNSET
    variants: list[BacktestRequestVariantsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        target_id = self.target_id

        definition: dict[str, Any] | Unset = UNSET
        if not isinstance(self.definition, Unset):
            definition = self.definition.to_dict()

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        from_seq = self.from_seq

        to_seq = self.to_seq

        variants: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.variants, Unset):
            variants = []
            for variants_item_data in self.variants:
                variants_item = variants_item_data.to_dict()
                variants.append(variants_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if target_id is not UNSET:
            field_dict["targetId"] = target_id
        if definition is not UNSET:
            field_dict["definition"] = definition
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if from_seq is not UNSET:
            field_dict["fromSeq"] = from_seq
        if to_seq is not UNSET:
            field_dict["toSeq"] = to_seq
        if variants is not UNSET:
            field_dict["variants"] = variants

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.backtest_request_definition import BacktestRequestDefinition
        from ..models.backtest_request_variants_item import BacktestRequestVariantsItem

        d = dict(src_dict)
        type_ = BacktestRequestType(d.pop("type"))

        target_id = d.pop("targetId", UNSET)

        _definition = d.pop("definition", UNSET)
        definition: BacktestRequestDefinition | Unset
        if isinstance(_definition, Unset):
            definition = UNSET
        else:
            definition = BacktestRequestDefinition.from_dict(_definition)

        _from_ = d.pop("from", UNSET)
        from_: datetime.datetime | Unset
        if isinstance(_from_, Unset):
            from_ = UNSET
        else:
            from_ = datetime.datetime.fromisoformat(_from_)

        _to = d.pop("to", UNSET)
        to: datetime.datetime | Unset
        if isinstance(_to, Unset):
            to = UNSET
        else:
            to = datetime.datetime.fromisoformat(_to)

        from_seq = d.pop("fromSeq", UNSET)

        to_seq = d.pop("toSeq", UNSET)

        _variants = d.pop("variants", UNSET)
        variants: list[BacktestRequestVariantsItem] | Unset = UNSET
        if _variants is not UNSET:
            variants = []
            for variants_item_data in _variants:
                variants_item = BacktestRequestVariantsItem.from_dict(
                    variants_item_data
                )

                variants.append(variants_item)

        backtest_request = cls(
            type_=type_,
            target_id=target_id,
            definition=definition,
            from_=from_,
            to=to,
            from_seq=from_seq,
            to_seq=to_seq,
            variants=variants,
        )

        backtest_request.additional_properties = d
        return backtest_request

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
