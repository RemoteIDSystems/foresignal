from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_binding_input_target import ExternalBindingInputTarget
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.external_binding_input_config import ExternalBindingInputConfig
    from ..models.external_binding_input_filter_type_0_item import (
        ExternalBindingInputFilterType0Item,
    )


T = TypeVar("T", bound="ExternalBindingInput")


@_attrs_define
class ExternalBindingInput:
    """Bind an atomic stream to a target (138)

    Attributes:
        stream_id (str):
        target (ExternalBindingInputTarget):
        config (ExternalBindingInputConfig | Unset): Target configuration (docs/External-Data-Protocol.md §5)
        filter_ (list[ExternalBindingInputFilterType0Item] | None | Unset): 017 conditions on item properties
        enabled (bool | Unset):  Default: True.
        backfill (bool | Unset):  Default: True.
    """

    stream_id: str
    target: ExternalBindingInputTarget
    config: ExternalBindingInputConfig | Unset = UNSET
    filter_: list[ExternalBindingInputFilterType0Item] | None | Unset = UNSET
    enabled: bool | Unset = True
    backfill: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        stream_id = self.stream_id

        target = self.target.value

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        filter_: list[dict[str, Any]] | None | Unset
        if isinstance(self.filter_, Unset):
            filter_ = UNSET
        elif isinstance(self.filter_, list):
            filter_ = []
            for filter_type_0_item_data in self.filter_:
                filter_type_0_item = filter_type_0_item_data.to_dict()
                filter_.append(filter_type_0_item)

        else:
            filter_ = self.filter_

        enabled = self.enabled

        backfill = self.backfill

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "streamId": stream_id,
                "target": target,
            }
        )
        if config is not UNSET:
            field_dict["config"] = config
        if filter_ is not UNSET:
            field_dict["filter"] = filter_
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if backfill is not UNSET:
            field_dict["backfill"] = backfill

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.external_binding_input_config import ExternalBindingInputConfig
        from ..models.external_binding_input_filter_type_0_item import (
            ExternalBindingInputFilterType0Item,
        )

        d = dict(src_dict)
        stream_id = d.pop("streamId")

        target = ExternalBindingInputTarget(d.pop("target"))

        _config = d.pop("config", UNSET)
        config: ExternalBindingInputConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = ExternalBindingInputConfig.from_dict(_config)

        def _parse_filter_(
            data: object,
        ) -> list[ExternalBindingInputFilterType0Item] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                filter_type_0 = []
                _filter_type_0 = data
                for filter_type_0_item_data in _filter_type_0:
                    filter_type_0_item = ExternalBindingInputFilterType0Item.from_dict(
                        filter_type_0_item_data
                    )

                    filter_type_0.append(filter_type_0_item)

                return filter_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[ExternalBindingInputFilterType0Item] | None | Unset, data)

        filter_ = _parse_filter_(d.pop("filter", UNSET))

        enabled = d.pop("enabled", UNSET)

        backfill = d.pop("backfill", UNSET)

        external_binding_input = cls(
            stream_id=stream_id,
            target=target,
            config=config,
            filter_=filter_,
            enabled=enabled,
            backfill=backfill,
        )

        external_binding_input.additional_properties = d
        return external_binding_input

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
