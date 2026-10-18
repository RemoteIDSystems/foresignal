from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_set_input_ledger_question_type_0_resolver_type import (
    HypothesisSetInputLedgerQuestionType0ResolverType,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_set_input_ledger_question_type_0_resolver_params import (
        HypothesisSetInputLedgerQuestionType0ResolverParams,
    )


T = TypeVar("T", bound="HypothesisSetInputLedgerQuestionType0Resolver")


@_attrs_define
class HypothesisSetInputLedgerQuestionType0Resolver:
    """122 resolver

    Attributes:
        type_ (HypothesisSetInputLedgerQuestionType0ResolverType):
        params (HypothesisSetInputLedgerQuestionType0ResolverParams | Unset):
    """

    type_: HypothesisSetInputLedgerQuestionType0ResolverType
    params: HypothesisSetInputLedgerQuestionType0ResolverParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_set_input_ledger_question_type_0_resolver_params import (
            HypothesisSetInputLedgerQuestionType0ResolverParams,
        )

        d = dict(src_dict)
        type_ = HypothesisSetInputLedgerQuestionType0ResolverType(d.pop("type"))

        _params = d.pop("params", UNSET)
        params: HypothesisSetInputLedgerQuestionType0ResolverParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = HypothesisSetInputLedgerQuestionType0ResolverParams.from_dict(
                _params
            )

        hypothesis_set_input_ledger_question_type_0_resolver = cls(
            type_=type_,
            params=params,
        )

        hypothesis_set_input_ledger_question_type_0_resolver.additional_properties = d
        return hypothesis_set_input_ledger_question_type_0_resolver

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
