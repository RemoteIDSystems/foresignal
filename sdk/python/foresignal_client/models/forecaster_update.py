from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecaster_update_config import ForecasterUpdateConfig


T = TypeVar("T", bound="ForecasterUpdate")


@_attrs_define
class ForecasterUpdate:
    """Replace a forecaster's settings

    Attributes:
        active (bool):
        config (ForecasterUpdateConfig | Unset): Replaces the forecaster's configuration. For an external forecaster,
            `externalMinResolved` (integer) overrides the ensemble's global minimum resolved count before the forecaster is
            weighted (155).
        name (str | Unset): Optional display name; set for a first-class external forecaster (155).
    """

    active: bool
    config: ForecasterUpdateConfig | Unset = UNSET
    name: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        active = self.active

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        name = self.name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "active": active,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config
        if name is not UNSET:
            field_dict["name"] = name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecaster_update_config import ForecasterUpdateConfig

        d = dict(src_dict)
        active = d.pop("active")

        _config = d.pop("config", UNSET)
        config: ForecasterUpdateConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = ForecasterUpdateConfig.from_dict(_config)

        name = d.pop("name", UNSET)

        forecaster_update = cls(
            active=active,
            config=config,
            name=name,
        )

        forecaster_update.additional_properties = d
        return forecaster_update

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
