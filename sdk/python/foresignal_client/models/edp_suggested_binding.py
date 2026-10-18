from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_suggested_binding_target import EdpSuggestedBindingTarget
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_suggested_binding_config import EdpSuggestedBindingConfig


T = TypeVar("T", bound="EdpSuggestedBinding")


@_attrs_define
class EdpSuggestedBinding:
    """A binding the provider recommends for an atomic stream

    Attributes:
        target (EdpSuggestedBindingTarget):
        config (EdpSuggestedBindingConfig | Unset):
    """

    target: EdpSuggestedBindingTarget
    config: EdpSuggestedBindingConfig | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        target = self.target.value

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "target": target,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_suggested_binding_config import EdpSuggestedBindingConfig

        d = dict(src_dict)
        target = EdpSuggestedBindingTarget(d.pop("target"))

        _config = d.pop("config", UNSET)
        config: EdpSuggestedBindingConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = EdpSuggestedBindingConfig.from_dict(_config)

        edp_suggested_binding = cls(
            target=target,
            config=config,
        )

        edp_suggested_binding.additional_properties = d
        return edp_suggested_binding

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
