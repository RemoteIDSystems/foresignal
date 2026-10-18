from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_set_input_ledger_question_type_0_resolver import (
        HypothesisSetInputLedgerQuestionType0Resolver,
    )


T = TypeVar("T", bound="HypothesisSetInputLedgerQuestionType0")


@_attrs_define
class HypothesisSetInputLedgerQuestionType0:
    """Ledger question to create with the set (when `horizon` and a resolver are given)

    Attributes:
        resolver (HypothesisSetInputLedgerQuestionType0Resolver): 122 resolver
        resolution_criteria (None | str | Unset):
        category (None | str | Unset):
    """

    resolver: HypothesisSetInputLedgerQuestionType0Resolver
    resolution_criteria: None | str | Unset = UNSET
    category: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        resolver = self.resolver.to_dict()

        resolution_criteria: None | str | Unset
        if isinstance(self.resolution_criteria, Unset):
            resolution_criteria = UNSET
        else:
            resolution_criteria = self.resolution_criteria

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "resolver": resolver,
            }
        )
        if resolution_criteria is not UNSET:
            field_dict["resolutionCriteria"] = resolution_criteria
        if category is not UNSET:
            field_dict["category"] = category

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_set_input_ledger_question_type_0_resolver import (
            HypothesisSetInputLedgerQuestionType0Resolver,
        )

        d = dict(src_dict)
        resolver = HypothesisSetInputLedgerQuestionType0Resolver.from_dict(
            d.pop("resolver")
        )

        def _parse_resolution_criteria(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        resolution_criteria = _parse_resolution_criteria(
            d.pop("resolutionCriteria", UNSET)
        )

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        hypothesis_set_input_ledger_question_type_0 = cls(
            resolver=resolver,
            resolution_criteria=resolution_criteria,
            category=category,
        )

        hypothesis_set_input_ledger_question_type_0.additional_properties = d
        return hypothesis_set_input_ledger_question_type_0

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
