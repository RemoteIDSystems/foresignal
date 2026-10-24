from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.external_forecaster_registration_config import (
        ExternalForecasterRegistrationConfig,
    )


T = TypeVar("T", bound="ExternalForecasterRegistration")


@_attrs_define
class ExternalForecasterRegistration:
    """Pre-registers, names and configures a first-class external forecaster (155), independent of the auto-derived
    edp:{sourceId}/{streamId} id.

        Attributes:
            id (str): Forecaster id, conventionally edp:{sourceId}/{streamId}.
            name (str | Unset): Optional display name (defaults to the id).
            config (ExternalForecasterRegistrationConfig | Unset): Optional configuration; `sourceId` and `streamId` pin the
                provider for backtests, and `externalMinResolved` (integer) overrides the ensemble's global minimum resolved
                count.
    """

    id: str
    name: str | Unset = UNSET
    config: ExternalForecasterRegistrationConfig | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if config is not UNSET:
            field_dict["config"] = config

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.external_forecaster_registration_config import (
            ExternalForecasterRegistrationConfig,
        )

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name", UNSET)

        _config = d.pop("config", UNSET)
        config: ExternalForecasterRegistrationConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = ExternalForecasterRegistrationConfig.from_dict(_config)

        external_forecaster_registration = cls(
            id=id,
            name=name,
            config=config,
        )

        external_forecaster_registration.additional_properties = d
        return external_forecaster_registration

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
