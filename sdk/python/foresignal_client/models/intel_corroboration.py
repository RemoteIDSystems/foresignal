from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_corroboration_auto_corroborate import (
        IntelCorroborationAutoCorroborate,
    )
    from ..models.intel_corroboration_event_quality import (
        IntelCorroborationEventQuality,
    )
    from ..models.intel_corroboration_situation_activation import (
        IntelCorroborationSituationActivation,
    )


T = TypeVar("T", bound="IntelCorroboration")


@_attrs_define
class IntelCorroboration:
    """The tunable corroboration policy (154): the "promote to a trusted signal" gates, each defaulting to its historical
    constant. A PUT replaces the document; members left out return to their defaults.

        Attributes:
            event_quality (IntelCorroborationEventQuality | Unset): The thresholds for an intelligence event's evidence-
                quality label
            situation_activation (IntelCorroborationSituationActivation | Unset): The rule that moves a situation from
                emerging to active (118)
            auto_corroborate (IntelCorroborationAutoCorroborate | Unset): Automatic corroboration of a lone
                agent/analyst/operator event on creation: it searches for independent observations near it in space and time and
                attaches them, so it surfaces already corroborated and can auto-activate
    """

    event_quality: IntelCorroborationEventQuality | Unset = UNSET
    situation_activation: IntelCorroborationSituationActivation | Unset = UNSET
    auto_corroborate: IntelCorroborationAutoCorroborate | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event_quality: dict[str, Any] | Unset = UNSET
        if not isinstance(self.event_quality, Unset):
            event_quality = self.event_quality.to_dict()

        situation_activation: dict[str, Any] | Unset = UNSET
        if not isinstance(self.situation_activation, Unset):
            situation_activation = self.situation_activation.to_dict()

        auto_corroborate: dict[str, Any] | Unset = UNSET
        if not isinstance(self.auto_corroborate, Unset):
            auto_corroborate = self.auto_corroborate.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if event_quality is not UNSET:
            field_dict["eventQuality"] = event_quality
        if situation_activation is not UNSET:
            field_dict["situationActivation"] = situation_activation
        if auto_corroborate is not UNSET:
            field_dict["autoCorroborate"] = auto_corroborate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_corroboration_auto_corroborate import (
            IntelCorroborationAutoCorroborate,
        )
        from ..models.intel_corroboration_event_quality import (
            IntelCorroborationEventQuality,
        )
        from ..models.intel_corroboration_situation_activation import (
            IntelCorroborationSituationActivation,
        )

        d = dict(src_dict)
        _event_quality = d.pop("eventQuality", UNSET)
        event_quality: IntelCorroborationEventQuality | Unset
        if isinstance(_event_quality, Unset):
            event_quality = UNSET
        else:
            event_quality = IntelCorroborationEventQuality.from_dict(_event_quality)

        _situation_activation = d.pop("situationActivation", UNSET)
        situation_activation: IntelCorroborationSituationActivation | Unset
        if isinstance(_situation_activation, Unset):
            situation_activation = UNSET
        else:
            situation_activation = IntelCorroborationSituationActivation.from_dict(
                _situation_activation
            )

        _auto_corroborate = d.pop("autoCorroborate", UNSET)
        auto_corroborate: IntelCorroborationAutoCorroborate | Unset
        if isinstance(_auto_corroborate, Unset):
            auto_corroborate = UNSET
        else:
            auto_corroborate = IntelCorroborationAutoCorroborate.from_dict(
                _auto_corroborate
            )

        intel_corroboration = cls(
            event_quality=event_quality,
            situation_activation=situation_activation,
            auto_corroborate=auto_corroborate,
        )

        intel_corroboration.additional_properties = d
        return intel_corroboration

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
