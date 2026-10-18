from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_area import ChronicleArea
    from ..models.chronicle_layer_input import ChronicleLayerInput


T = TypeVar("T", bound="ChronicleInput")


@_attrs_define
class ChronicleInput:
    """Create a chronicle; it starts in status `proposed` (no capture) until activated

    Attributes:
        name (str):
        description (None | str | Unset):
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
        area (ChronicleArea | None | Unset):
        retention (str | Unset): `forever` (default) or an ISO-8601 duration kept after stop Example: forever.
        size_cap_bytes (int | Unset): Default `chronicle.defaultCapGb` (20 GB)
        situation_ids (list[str] | Unset):
        hypothesis_set_ids (list[str] | Unset):
        investigation_ids (list[str] | Unset):
        topic_ids (list[str] | Unset):
        layers (list[ChronicleLayerInput] | Unset):
        from_workspace_id (None | str | Unset):
        activate (bool | Unset): Activate immediately after creation Default: False.
    """

    name: str
    description: None | str | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    area: ChronicleArea | None | Unset = UNSET
    retention: str | Unset = UNSET
    size_cap_bytes: int | Unset = UNSET
    situation_ids: list[str] | Unset = UNSET
    hypothesis_set_ids: list[str] | Unset = UNSET
    investigation_ids: list[str] | Unset = UNSET
    topic_ids: list[str] | Unset = UNSET
    layers: list[ChronicleLayerInput] | Unset = UNSET
    from_workspace_id: None | str | Unset = UNSET
    activate: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_area import ChronicleArea

        name = self.name

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, ChronicleArea):
            area = self.area.to_dict()
        else:
            area = self.area

        retention = self.retention

        size_cap_bytes = self.size_cap_bytes

        situation_ids: list[str] | Unset = UNSET
        if not isinstance(self.situation_ids, Unset):
            situation_ids = self.situation_ids

        hypothesis_set_ids: list[str] | Unset = UNSET
        if not isinstance(self.hypothesis_set_ids, Unset):
            hypothesis_set_ids = self.hypothesis_set_ids

        investigation_ids: list[str] | Unset = UNSET
        if not isinstance(self.investigation_ids, Unset):
            investigation_ids = self.investigation_ids

        topic_ids: list[str] | Unset = UNSET
        if not isinstance(self.topic_ids, Unset):
            topic_ids = self.topic_ids

        layers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.layers, Unset):
            layers = []
            for layers_item_data in self.layers:
                layers_item = layers_item_data.to_dict()
                layers.append(layers_item)

        from_workspace_id: None | str | Unset
        if isinstance(self.from_workspace_id, Unset):
            from_workspace_id = UNSET
        else:
            from_workspace_id = self.from_workspace_id

        activate = self.activate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if area is not UNSET:
            field_dict["area"] = area
        if retention is not UNSET:
            field_dict["retention"] = retention
        if size_cap_bytes is not UNSET:
            field_dict["sizeCapBytes"] = size_cap_bytes
        if situation_ids is not UNSET:
            field_dict["situationIds"] = situation_ids
        if hypothesis_set_ids is not UNSET:
            field_dict["hypothesisSetIds"] = hypothesis_set_ids
        if investigation_ids is not UNSET:
            field_dict["investigationIds"] = investigation_ids
        if topic_ids is not UNSET:
            field_dict["topicIds"] = topic_ids
        if layers is not UNSET:
            field_dict["layers"] = layers
        if from_workspace_id is not UNSET:
            field_dict["fromWorkspaceId"] = from_workspace_id
        if activate is not UNSET:
            field_dict["activate"] = activate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_layer_input import ChronicleLayerInput

        d = dict(src_dict)
        name = d.pop("name")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        def _parse_area(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = ChronicleArea.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        retention = d.pop("retention", UNSET)

        size_cap_bytes = d.pop("sizeCapBytes", UNSET)

        situation_ids = cast(list[str], d.pop("situationIds", UNSET))

        hypothesis_set_ids = cast(list[str], d.pop("hypothesisSetIds", UNSET))

        investigation_ids = cast(list[str], d.pop("investigationIds", UNSET))

        topic_ids = cast(list[str], d.pop("topicIds", UNSET))

        _layers = d.pop("layers", UNSET)
        layers: list[ChronicleLayerInput] | Unset = UNSET
        if _layers is not UNSET:
            layers = []
            for layers_item_data in _layers:
                layers_item = ChronicleLayerInput.from_dict(layers_item_data)

                layers.append(layers_item)

        def _parse_from_workspace_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        from_workspace_id = _parse_from_workspace_id(d.pop("fromWorkspaceId", UNSET))

        activate = d.pop("activate", UNSET)

        chronicle_input = cls(
            name=name,
            description=description,
            visibility=visibility,
            tlp=tlp,
            area=area,
            retention=retention,
            size_cap_bytes=size_cap_bytes,
            situation_ids=situation_ids,
            hypothesis_set_ids=hypothesis_set_ids,
            investigation_ids=investigation_ids,
            topic_ids=topic_ids,
            layers=layers,
            from_workspace_id=from_workspace_id,
            activate=activate,
        )

        chronicle_input.additional_properties = d
        return chronicle_input

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
