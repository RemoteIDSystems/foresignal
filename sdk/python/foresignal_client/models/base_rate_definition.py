from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.base_rate_definition_window import BaseRateDefinitionWindow
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.base_rate_definition_condition_type_0 import (
        BaseRateDefinitionConditionType0,
    )


T = TypeVar("T", bound="BaseRateDefinition")


@_attrs_define
class BaseRateDefinition:
    """Reference class of a base rate: family, regions or region class, window and optional condition

    Attributes:
        family (str): Signal family plus subtype, e.g. `conflict:battles`, `seismic:m5`, `disaster:gdacs-orange+`
        window (BaseRateDefinitionWindow):
        region_codes (list[str] | Unset): Regions (exclusive with `regionClass`)
        region_class (None | str | Unset): 040 region class, e.g. a continent or income group (exclusive with
            `regionCodes`)
        condition (BaseRateDefinitionConditionType0 | None | Unset):
    """

    family: str
    window: BaseRateDefinitionWindow
    region_codes: list[str] | Unset = UNSET
    region_class: None | str | Unset = UNSET
    condition: BaseRateDefinitionConditionType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.base_rate_definition_condition_type_0 import (
            BaseRateDefinitionConditionType0,
        )

        family = self.family

        window = self.window.value

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        region_class: None | str | Unset
        if isinstance(self.region_class, Unset):
            region_class = UNSET
        else:
            region_class = self.region_class

        condition: dict[str, Any] | None | Unset
        if isinstance(self.condition, Unset):
            condition = UNSET
        elif isinstance(self.condition, BaseRateDefinitionConditionType0):
            condition = self.condition.to_dict()
        else:
            condition = self.condition

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "family": family,
                "window": window,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if region_class is not UNSET:
            field_dict["regionClass"] = region_class
        if condition is not UNSET:
            field_dict["condition"] = condition

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.base_rate_definition_condition_type_0 import (
            BaseRateDefinitionConditionType0,
        )

        d = dict(src_dict)
        family = d.pop("family")

        window = BaseRateDefinitionWindow(d.pop("window"))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_region_class(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_class = _parse_region_class(d.pop("regionClass", UNSET))

        def _parse_condition(
            data: object,
        ) -> BaseRateDefinitionConditionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                condition_type_0 = BaseRateDefinitionConditionType0.from_dict(data)

                return condition_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BaseRateDefinitionConditionType0 | None | Unset, data)

        condition = _parse_condition(d.pop("condition", UNSET))

        base_rate_definition = cls(
            family=family,
            window=window,
            region_codes=region_codes,
            region_class=region_class,
            condition=condition,
        )

        base_rate_definition.additional_properties = d
        return base_rate_definition

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
