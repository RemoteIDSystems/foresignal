from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.model_disagreement_fields_item import ModelDisagreementFieldsItem

if TYPE_CHECKING:
    from ..models.model_disagreement_spread import ModelDisagreementSpread


T = TypeVar("T", bound="ModelDisagreement")


@_attrs_define
class ModelDisagreement:
    """Model-disagreement flag over the next 24 h (gust, 24 h precip, t2m max), quantised with hysteresis

    Attributes:
        fields (list[ModelDisagreementFieldsItem]):
        spread (ModelDisagreementSpread):  Example: {'gust': 9.5}.
        models (list[str]):  Example: ['gfs', 'ifs'].
    """

    fields: list[ModelDisagreementFieldsItem]
    spread: ModelDisagreementSpread
    models: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        fields = []
        for fields_item_data in self.fields:
            fields_item = fields_item_data.value
            fields.append(fields_item)

        spread = self.spread.to_dict()

        models = self.models

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fields": fields,
                "spread": spread,
                "models": models,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.model_disagreement_spread import ModelDisagreementSpread

        d = dict(src_dict)
        fields = []
        _fields = d.pop("fields")
        for fields_item_data in _fields:
            fields_item = ModelDisagreementFieldsItem(fields_item_data)

            fields.append(fields_item)

        spread = ModelDisagreementSpread.from_dict(d.pop("spread"))

        models = cast(list[str], d.pop("models"))

        model_disagreement = cls(
            fields=fields,
            spread=spread,
            models=models,
        )

        model_disagreement.additional_properties = d
        return model_disagreement

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
