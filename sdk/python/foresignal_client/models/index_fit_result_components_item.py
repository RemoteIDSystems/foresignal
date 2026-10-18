from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IndexFitResultComponentsItem")


@_attrs_define
class IndexFitResultComponentsItem:
    """Per-component fit

    Attributes:
        key (str):
        coefficient (float):
        proposed_weight (float):
        standard_error (float | None | Unset):
        p_value (float | None | Unset):
        significant (bool | Unset):
        current_weight (float | Unset):
    """

    key: str
    coefficient: float
    proposed_weight: float
    standard_error: float | None | Unset = UNSET
    p_value: float | None | Unset = UNSET
    significant: bool | Unset = UNSET
    current_weight: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        key = self.key

        coefficient = self.coefficient

        proposed_weight = self.proposed_weight

        standard_error: float | None | Unset
        if isinstance(self.standard_error, Unset):
            standard_error = UNSET
        else:
            standard_error = self.standard_error

        p_value: float | None | Unset
        if isinstance(self.p_value, Unset):
            p_value = UNSET
        else:
            p_value = self.p_value

        significant = self.significant

        current_weight = self.current_weight

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "key": key,
                "coefficient": coefficient,
                "proposedWeight": proposed_weight,
            }
        )
        if standard_error is not UNSET:
            field_dict["standardError"] = standard_error
        if p_value is not UNSET:
            field_dict["pValue"] = p_value
        if significant is not UNSET:
            field_dict["significant"] = significant
        if current_weight is not UNSET:
            field_dict["currentWeight"] = current_weight

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        key = d.pop("key")

        coefficient = d.pop("coefficient")

        proposed_weight = d.pop("proposedWeight")

        def _parse_standard_error(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        standard_error = _parse_standard_error(d.pop("standardError", UNSET))

        def _parse_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_value = _parse_p_value(d.pop("pValue", UNSET))

        significant = d.pop("significant", UNSET)

        current_weight = d.pop("currentWeight", UNSET)

        index_fit_result_components_item = cls(
            key=key,
            coefficient=coefficient,
            proposed_weight=proposed_weight,
            standard_error=standard_error,
            p_value=p_value,
            significant=significant,
            current_weight=current_weight,
        )

        index_fit_result_components_item.additional_properties = d
        return index_fit_result_components_item

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
