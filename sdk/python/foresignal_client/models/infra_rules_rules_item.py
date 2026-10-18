from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_rules_rules_item_id import InfraRulesRulesItemId
from ..models.infra_rules_rules_item_label import InfraRulesRulesItemLabel
from ..models.infra_rules_rules_item_method import InfraRulesRulesItemMethod
from ..models.infra_rules_rules_item_relation import InfraRulesRulesItemRelation
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.infra_rules_rules_item_params import InfraRulesRulesItemParams


T = TypeVar("T", bound="InfraRulesRulesItem")


@_attrs_define
class InfraRulesRulesItem:
    """One rule

    Attributes:
        id (InfraRulesRulesItemId):
        enabled (bool):
        method (InfraRulesRulesItemMethod | Unset):
        relation (InfraRulesRulesItemRelation | Unset):
        label (InfraRulesRulesItemLabel | Unset):
        params (InfraRulesRulesItemParams | Unset): Overridable parameters (radii, distances)
    """

    id: InfraRulesRulesItemId
    enabled: bool
    method: InfraRulesRulesItemMethod | Unset = UNSET
    relation: InfraRulesRulesItemRelation | Unset = UNSET
    label: InfraRulesRulesItemLabel | Unset = UNSET
    params: InfraRulesRulesItemParams | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        enabled = self.enabled

        method: str | Unset = UNSET
        if not isinstance(self.method, Unset):
            method = self.method.value

        relation: str | Unset = UNSET
        if not isinstance(self.relation, Unset):
            relation = self.relation.value

        label: str | Unset = UNSET
        if not isinstance(self.label, Unset):
            label = self.label.value

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "enabled": enabled,
            }
        )
        if method is not UNSET:
            field_dict["method"] = method
        if relation is not UNSET:
            field_dict["relation"] = relation
        if label is not UNSET:
            field_dict["label"] = label
        if params is not UNSET:
            field_dict["params"] = params

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.infra_rules_rules_item_params import InfraRulesRulesItemParams

        d = dict(src_dict)
        id = InfraRulesRulesItemId(d.pop("id"))

        enabled = d.pop("enabled")

        _method = d.pop("method", UNSET)
        method: InfraRulesRulesItemMethod | Unset
        if isinstance(_method, Unset):
            method = UNSET
        else:
            method = InfraRulesRulesItemMethod(_method)

        _relation = d.pop("relation", UNSET)
        relation: InfraRulesRulesItemRelation | Unset
        if isinstance(_relation, Unset):
            relation = UNSET
        else:
            relation = InfraRulesRulesItemRelation(_relation)

        _label = d.pop("label", UNSET)
        label: InfraRulesRulesItemLabel | Unset
        if isinstance(_label, Unset):
            label = UNSET
        else:
            label = InfraRulesRulesItemLabel(_label)

        _params = d.pop("params", UNSET)
        params: InfraRulesRulesItemParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = InfraRulesRulesItemParams.from_dict(_params)

        infra_rules_rules_item = cls(
            id=id,
            enabled=enabled,
            method=method,
            relation=relation,
            label=label,
            params=params,
        )

        infra_rules_rules_item.additional_properties = d
        return infra_rules_rules_item

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
