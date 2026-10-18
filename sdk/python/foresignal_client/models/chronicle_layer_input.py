from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.capture_mode import CaptureMode
    from ..models.chronicle_area import ChronicleArea
    from ..models.chronicle_external_stream_selector import (
        ChronicleExternalStreamSelector,
    )
    from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
    from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
    from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
    from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
    from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
    from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
    from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
    from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
    from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
    from ..models.chronicle_social_narratives_selector import (
        ChronicleSocialNarrativesSelector,
    )
    from ..models.chronicle_social_volume_selector import ChronicleSocialVolumeSelector


T = TypeVar("T", bound="ChronicleLayerInput")


@_attrs_define
class ChronicleLayerInput:
    """Add a layer to a chronicle

    Attributes:
        name (str):
        selector (ChronicleExternalStreamSelector | ChronicleLayerSelectorType0 | ChronicleLayerSelectorType1 |
            ChronicleLayerSelectorType2 | ChronicleLayerSelectorType3 | ChronicleLayerSelectorType4 |
            ChronicleLayerSelectorType5 | ChronicleLayerSelectorType6 | ChronicleLayerSelectorType7 |
            ChronicleLayerSelectorType8 | ChronicleSocialNarrativesSelector | ChronicleSocialVolumeSelector): What a
            chronicle layer selects (one variant by `type`)
        capture (CaptureMode): How a chronicle layer captures matching data
        area_override (ChronicleArea | None | Unset):
        enabled (bool | Unset):  Default: True.
    """

    name: str
    selector: (
        ChronicleExternalStreamSelector
        | ChronicleLayerSelectorType0
        | ChronicleLayerSelectorType1
        | ChronicleLayerSelectorType2
        | ChronicleLayerSelectorType3
        | ChronicleLayerSelectorType4
        | ChronicleLayerSelectorType5
        | ChronicleLayerSelectorType6
        | ChronicleLayerSelectorType7
        | ChronicleLayerSelectorType8
        | ChronicleSocialNarrativesSelector
        | ChronicleSocialVolumeSelector
    )
    capture: CaptureMode
    area_override: ChronicleArea | None | Unset = UNSET
    enabled: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
        from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
        from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
        from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
        from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
        from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
        from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
        from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
        from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
        from ..models.chronicle_social_narratives_selector import (
            ChronicleSocialNarrativesSelector,
        )
        from ..models.chronicle_social_volume_selector import (
            ChronicleSocialVolumeSelector,
        )

        name = self.name

        selector: dict[str, Any]
        if (
            isinstance(self.selector, ChronicleLayerSelectorType0)
            or isinstance(self.selector, ChronicleLayerSelectorType1)
            or isinstance(self.selector, ChronicleLayerSelectorType2)
            or isinstance(self.selector, ChronicleLayerSelectorType3)
            or isinstance(self.selector, ChronicleLayerSelectorType4)
            or isinstance(self.selector, ChronicleLayerSelectorType5)
            or isinstance(self.selector, ChronicleLayerSelectorType6)
            or isinstance(self.selector, ChronicleLayerSelectorType7)
            or isinstance(self.selector, ChronicleLayerSelectorType8)
            or isinstance(self.selector, ChronicleSocialNarrativesSelector)
            or isinstance(self.selector, ChronicleSocialVolumeSelector)
        ):
            selector = self.selector.to_dict()
        else:
            selector = self.selector.to_dict()

        capture = self.capture.to_dict()

        area_override: dict[str, Any] | None | Unset
        if isinstance(self.area_override, Unset):
            area_override = UNSET
        elif isinstance(self.area_override, ChronicleArea):
            area_override = self.area_override.to_dict()
        else:
            area_override = self.area_override

        enabled = self.enabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "selector": selector,
                "capture": capture,
            }
        )
        if area_override is not UNSET:
            field_dict["areaOverride"] = area_override
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.capture_mode import CaptureMode
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_external_stream_selector import (
            ChronicleExternalStreamSelector,
        )
        from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
        from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
        from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
        from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
        from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
        from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
        from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
        from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
        from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
        from ..models.chronicle_social_narratives_selector import (
            ChronicleSocialNarrativesSelector,
        )
        from ..models.chronicle_social_volume_selector import (
            ChronicleSocialVolumeSelector,
        )

        d = dict(src_dict)
        name = d.pop("name")

        def _parse_selector(
            data: object,
        ) -> (
            ChronicleExternalStreamSelector
            | ChronicleLayerSelectorType0
            | ChronicleLayerSelectorType1
            | ChronicleLayerSelectorType2
            | ChronicleLayerSelectorType3
            | ChronicleLayerSelectorType4
            | ChronicleLayerSelectorType5
            | ChronicleLayerSelectorType6
            | ChronicleLayerSelectorType7
            | ChronicleLayerSelectorType8
            | ChronicleSocialNarrativesSelector
            | ChronicleSocialVolumeSelector
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_0 = (
                    ChronicleLayerSelectorType0.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_1 = (
                    ChronicleLayerSelectorType1.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_2 = (
                    ChronicleLayerSelectorType2.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_3 = (
                    ChronicleLayerSelectorType3.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_4 = (
                    ChronicleLayerSelectorType4.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_4
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_5 = (
                    ChronicleLayerSelectorType5.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_5
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_6 = (
                    ChronicleLayerSelectorType6.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_6
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_7 = (
                    ChronicleLayerSelectorType7.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_7
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_8 = (
                    ChronicleLayerSelectorType8.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_8
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_9 = (
                    ChronicleSocialNarrativesSelector.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_9
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_10 = (
                    ChronicleSocialVolumeSelector.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_10
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_chronicle_layer_selector_type_11 = (
                ChronicleExternalStreamSelector.from_dict(data)
            )

            return componentsschemas_chronicle_layer_selector_type_11

        selector = _parse_selector(d.pop("selector"))

        capture = CaptureMode.from_dict(d.pop("capture"))

        def _parse_area_override(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_override_type_0 = ChronicleArea.from_dict(data)

                return area_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area_override = _parse_area_override(d.pop("areaOverride", UNSET))

        enabled = d.pop("enabled", UNSET)

        chronicle_layer_input = cls(
            name=name,
            selector=selector,
            capture=capture,
            area_override=area_override,
            enabled=enabled,
        )

        chronicle_layer_input.additional_properties = d
        return chronicle_layer_input

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
