from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.sink_type_id import SinkTypeId
from ..models.sink_type_kind import SinkTypeKind
from ..models.sink_type_modes_item import SinkTypeModesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sink_type_config_schema import SinkTypeConfigSchema


T = TypeVar("T", bound="SinkType")


@_attrs_define
class SinkType:
    """A sink type and its configuration schema (110)

    Attributes:
        id (SinkTypeId): federation-push is added by 113 (edge relay)
        name (str):
        kind (SinkTypeKind): CHANGE_FEED: ordered at-least-once delivery from a durable cursor; STATE_MIRROR: external
            copy of current state
        modes (list[SinkTypeModesItem]): PUSH: Foresignal connects out; SERVE: Foresignal listens on a LAN address
        config_schema (SinkTypeConfigSchema): JSON Schema 2020-12 of the type's `config`
        secret_fields (list[str]): Write-only secret settings
        description (str | Unset):
        default_max_per_second (int | None | Unset): 50 for PUSH, null (unlimited) for SERVE
    """

    id: SinkTypeId
    name: str
    kind: SinkTypeKind
    modes: list[SinkTypeModesItem]
    config_schema: SinkTypeConfigSchema
    secret_fields: list[str]
    description: str | Unset = UNSET
    default_max_per_second: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        name = self.name

        kind = self.kind.value

        modes = []
        for modes_item_data in self.modes:
            modes_item = modes_item_data.value
            modes.append(modes_item)

        config_schema = self.config_schema.to_dict()

        secret_fields = self.secret_fields

        description = self.description

        default_max_per_second: int | None | Unset
        if isinstance(self.default_max_per_second, Unset):
            default_max_per_second = UNSET
        else:
            default_max_per_second = self.default_max_per_second

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "kind": kind,
                "modes": modes,
                "configSchema": config_schema,
                "secretFields": secret_fields,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if default_max_per_second is not UNSET:
            field_dict["defaultMaxPerSecond"] = default_max_per_second

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sink_type_config_schema import SinkTypeConfigSchema

        d = dict(src_dict)
        id = SinkTypeId(d.pop("id"))

        name = d.pop("name")

        kind = SinkTypeKind(d.pop("kind"))

        modes = []
        _modes = d.pop("modes")
        for modes_item_data in _modes:
            modes_item = SinkTypeModesItem(modes_item_data)

            modes.append(modes_item)

        config_schema = SinkTypeConfigSchema.from_dict(d.pop("configSchema"))

        secret_fields = cast(list[str], d.pop("secretFields"))

        description = d.pop("description", UNSET)

        def _parse_default_max_per_second(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        default_max_per_second = _parse_default_max_per_second(
            d.pop("defaultMaxPerSecond", UNSET)
        )

        sink_type = cls(
            id=id,
            name=name,
            kind=kind,
            modes=modes,
            config_schema=config_schema,
            secret_fields=secret_fields,
            description=description,
            default_max_per_second=default_max_per_second,
        )

        sink_type.additional_properties = d
        return sink_type

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
