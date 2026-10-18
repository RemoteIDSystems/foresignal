from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProblemReferencesType0")


@_attrs_define
class ProblemReferencesType0:
    """PLACE_IN_USE (036): the rules, views and briefings that reference the place

    Attributes:
        rules (list[UUID] | Unset):
        views (list[UUID] | Unset):
        briefings (list[UUID] | Unset):
    """

    rules: list[UUID] | Unset = UNSET
    views: list[UUID] | Unset = UNSET
    briefings: list[UUID] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rules: list[str] | Unset = UNSET
        if not isinstance(self.rules, Unset):
            rules = []
            for rules_item_data in self.rules:
                rules_item = str(rules_item_data)
                rules.append(rules_item)

        views: list[str] | Unset = UNSET
        if not isinstance(self.views, Unset):
            views = []
            for views_item_data in self.views:
                views_item = str(views_item_data)
                views.append(views_item)

        briefings: list[str] | Unset = UNSET
        if not isinstance(self.briefings, Unset):
            briefings = []
            for briefings_item_data in self.briefings:
                briefings_item = str(briefings_item_data)
                briefings.append(briefings_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if rules is not UNSET:
            field_dict["rules"] = rules
        if views is not UNSET:
            field_dict["views"] = views
        if briefings is not UNSET:
            field_dict["briefings"] = briefings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _rules = d.pop("rules", UNSET)
        rules: list[UUID] | Unset = UNSET
        if _rules is not UNSET:
            rules = []
            for rules_item_data in _rules:
                rules_item = UUID(rules_item_data)

                rules.append(rules_item)

        _views = d.pop("views", UNSET)
        views: list[UUID] | Unset = UNSET
        if _views is not UNSET:
            views = []
            for views_item_data in _views:
                views_item = UUID(views_item_data)

                views.append(views_item)

        _briefings = d.pop("briefings", UNSET)
        briefings: list[UUID] | Unset = UNSET
        if _briefings is not UNSET:
            briefings = []
            for briefings_item_data in _briefings:
                briefings_item = UUID(briefings_item_data)

                briefings.append(briefings_item)

        problem_references_type_0 = cls(
            rules=rules,
            views=views,
            briefings=briefings,
        )

        problem_references_type_0.additional_properties = d
        return problem_references_type_0

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
