from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_social_volume_selector_metrics_item import (
    ChronicleSocialVolumeSelectorMetricsItem,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_social_volume_selector_scopes_item import (
        ChronicleSocialVolumeSelectorScopesItem,
    )


T = TypeVar("T", bound="ChronicleSocialVolumeSelector")


@_attrs_define
class ChronicleSocialVolumeSelector:
    """Chronicle layer `social-volume` (137): social volume series by topic, place or entity

    Attributes:
        type_ (Literal['social-volume']):
        scopes (list[ChronicleSocialVolumeSelectorScopesItem]):
        platforms (list[str] | Unset):
        metrics (list[ChronicleSocialVolumeSelectorMetricsItem] | Unset):
    """

    type_: Literal["social-volume"]
    scopes: list[ChronicleSocialVolumeSelectorScopesItem]
    platforms: list[str] | Unset = UNSET
    metrics: list[ChronicleSocialVolumeSelectorMetricsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        scopes = []
        for scopes_item_data in self.scopes:
            scopes_item = scopes_item_data.to_dict()
            scopes.append(scopes_item)

        platforms: list[str] | Unset = UNSET
        if not isinstance(self.platforms, Unset):
            platforms = self.platforms

        metrics: list[str] | Unset = UNSET
        if not isinstance(self.metrics, Unset):
            metrics = []
            for metrics_item_data in self.metrics:
                metrics_item = metrics_item_data.value
                metrics.append(metrics_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "scopes": scopes,
            }
        )
        if platforms is not UNSET:
            field_dict["platforms"] = platforms
        if metrics is not UNSET:
            field_dict["metrics"] = metrics

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_social_volume_selector_scopes_item import (
            ChronicleSocialVolumeSelectorScopesItem,
        )

        d = dict(src_dict)
        type_ = cast(Literal["social-volume"], d.pop("type"))
        if type_ != "social-volume":
            raise ValueError(f"type must match const 'social-volume', got '{type_}'")

        scopes = []
        _scopes = d.pop("scopes")
        for scopes_item_data in _scopes:
            scopes_item = ChronicleSocialVolumeSelectorScopesItem.from_dict(
                scopes_item_data
            )

            scopes.append(scopes_item)

        platforms = cast(list[str], d.pop("platforms", UNSET))

        _metrics = d.pop("metrics", UNSET)
        metrics: list[ChronicleSocialVolumeSelectorMetricsItem] | Unset = UNSET
        if _metrics is not UNSET:
            metrics = []
            for metrics_item_data in _metrics:
                metrics_item = ChronicleSocialVolumeSelectorMetricsItem(
                    metrics_item_data
                )

                metrics.append(metrics_item)

        chronicle_social_volume_selector = cls(
            type_=type_,
            scopes=scopes,
            platforms=platforms,
            metrics=metrics,
        )

        chronicle_social_volume_selector.additional_properties = d
        return chronicle_social_volume_selector

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
