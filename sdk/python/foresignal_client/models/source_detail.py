from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.source_descriptor import SourceDescriptor
    from ..models.source_health import SourceHealth
    from ..models.source_settings import SourceSettings


T = TypeVar("T", bound="SourceDetail")


@_attrs_define
class SourceDetail:
    """
    Attributes:
        descriptor (SourceDescriptor):
        settings (SourceSettings):
        health (SourceHealth):
    """

    descriptor: SourceDescriptor
    settings: SourceSettings
    health: SourceHealth
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        descriptor = self.descriptor.to_dict()

        settings = self.settings.to_dict()

        health = self.health.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "descriptor": descriptor,
                "settings": settings,
                "health": health,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.source_descriptor import SourceDescriptor
        from ..models.source_health import SourceHealth
        from ..models.source_settings import SourceSettings

        d = dict(src_dict)
        descriptor = SourceDescriptor.from_dict(d.pop("descriptor"))

        settings = SourceSettings.from_dict(d.pop("settings"))

        health = SourceHealth.from_dict(d.pop("health"))

        source_detail = cls(
            descriptor=descriptor,
            settings=settings,
            health=health,
        )

        source_detail.additional_properties = d
        return source_detail

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
