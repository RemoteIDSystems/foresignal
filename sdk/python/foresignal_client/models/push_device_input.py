from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_device_input_content_level import PushDeviceInputContentLevel
from ..models.push_device_input_environment_type_1 import (
    PushDeviceInputEnvironmentType1,
)
from ..models.push_device_input_environment_type_2_type_1 import (
    PushDeviceInputEnvironmentType2Type1,
)
from ..models.push_device_input_environment_type_3_type_1 import (
    PushDeviceInputEnvironmentType3Type1,
)
from ..models.push_device_input_platform import PushDeviceInputPlatform
from ..models.push_device_input_transport import PushDeviceInputTransport
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_filters import PushFilters


T = TypeVar("T", bound="PushDeviceInput")


@_attrs_define
class PushDeviceInput:
    """Register a native device for push (032)

    Attributes:
        transport (PushDeviceInputTransport):
        platform (PushDeviceInputPlatform):
        app_id (str):
        token (str): APNs: 64–200 hex chars; FCM: 1–4096 chars
        environment (None | PushDeviceInputEnvironmentType1 | PushDeviceInputEnvironmentType2Type1 |
            PushDeviceInputEnvironmentType3Type1 | Unset): APNs only; must be covered by the provider's keyEnvironment
        name (None | str | Unset):
        filters (PushFilters | Unset): Which notifications a push target receives (031; shared by subscriptions and
            native devices)
        content_level (PushDeviceInputContentLevel | Unset): minimal: no alert text through Apple/Google; short:
            shortTitle (≤ 40 chars) Default: PushDeviceInputContentLevel.MINIMAL.
        lang (None | str | Unset):
    """

    transport: PushDeviceInputTransport
    platform: PushDeviceInputPlatform
    app_id: str
    token: str
    environment: (
        None
        | PushDeviceInputEnvironmentType1
        | PushDeviceInputEnvironmentType2Type1
        | PushDeviceInputEnvironmentType3Type1
        | Unset
    ) = UNSET
    name: None | str | Unset = UNSET
    filters: PushFilters | Unset = UNSET
    content_level: PushDeviceInputContentLevel | Unset = (
        PushDeviceInputContentLevel.MINIMAL
    )
    lang: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        transport = self.transport.value

        platform = self.platform.value

        app_id = self.app_id

        token = self.token

        environment: None | str | Unset
        if isinstance(self.environment, Unset):
            environment = UNSET
        elif (
            isinstance(self.environment, PushDeviceInputEnvironmentType1)
            or isinstance(self.environment, PushDeviceInputEnvironmentType2Type1)
            or isinstance(self.environment, PushDeviceInputEnvironmentType3Type1)
        ):
            environment = self.environment.value
        else:
            environment = self.environment

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        filters: dict[str, Any] | Unset = UNSET
        if not isinstance(self.filters, Unset):
            filters = self.filters.to_dict()

        content_level: str | Unset = UNSET
        if not isinstance(self.content_level, Unset):
            content_level = self.content_level.value

        lang: None | str | Unset
        if isinstance(self.lang, Unset):
            lang = UNSET
        else:
            lang = self.lang

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "transport": transport,
                "platform": platform,
                "appId": app_id,
                "token": token,
            }
        )
        if environment is not UNSET:
            field_dict["environment"] = environment
        if name is not UNSET:
            field_dict["name"] = name
        if filters is not UNSET:
            field_dict["filters"] = filters
        if content_level is not UNSET:
            field_dict["contentLevel"] = content_level
        if lang is not UNSET:
            field_dict["lang"] = lang

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_filters import PushFilters

        d = dict(src_dict)
        transport = PushDeviceInputTransport(d.pop("transport"))

        platform = PushDeviceInputPlatform(d.pop("platform"))

        app_id = d.pop("appId")

        token = d.pop("token")

        def _parse_environment(
            data: object,
        ) -> (
            None
            | PushDeviceInputEnvironmentType1
            | PushDeviceInputEnvironmentType2Type1
            | PushDeviceInputEnvironmentType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_1 = PushDeviceInputEnvironmentType1(data)

                return environment_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_2_type_1 = PushDeviceInputEnvironmentType2Type1(data)

                return environment_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_3_type_1 = PushDeviceInputEnvironmentType3Type1(data)

                return environment_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | PushDeviceInputEnvironmentType1
                | PushDeviceInputEnvironmentType2Type1
                | PushDeviceInputEnvironmentType3Type1
                | Unset,
                data,
            )

        environment = _parse_environment(d.pop("environment", UNSET))

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _filters = d.pop("filters", UNSET)
        filters: PushFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = PushFilters.from_dict(_filters)

        _content_level = d.pop("contentLevel", UNSET)
        content_level: PushDeviceInputContentLevel | Unset
        if isinstance(_content_level, Unset):
            content_level = UNSET
        else:
            content_level = PushDeviceInputContentLevel(_content_level)

        def _parse_lang(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lang = _parse_lang(d.pop("lang", UNSET))

        push_device_input = cls(
            transport=transport,
            platform=platform,
            app_id=app_id,
            token=token,
            environment=environment,
            name=name,
            filters=filters,
            content_level=content_level,
            lang=lang,
        )

        push_device_input.additional_properties = d
        return push_device_input

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
