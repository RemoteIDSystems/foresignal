from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_device_content_level import PushDeviceContentLevel
from ..models.push_device_environment_type_1 import PushDeviceEnvironmentType1
from ..models.push_device_environment_type_2_type_1 import (
    PushDeviceEnvironmentType2Type1,
)
from ..models.push_device_environment_type_3_type_1 import (
    PushDeviceEnvironmentType3Type1,
)
from ..models.push_device_platform import PushDevicePlatform
from ..models.push_device_status import PushDeviceStatus
from ..models.push_device_transport import PushDeviceTransport
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_filters import PushFilters


T = TypeVar("T", bound="PushDevice")


@_attrs_define
class PushDevice:
    """A registered native push device; the token is never returned (032)

    Attributes:
        id (UUID):
        transport (PushDeviceTransport):
        platform (PushDevicePlatform):
        app_id (str):
        token_suffix (str): Last 6 characters of the device token
        status (PushDeviceStatus): 032 never disables a device by itself: dead tokens are deleted. `disabled` is
            reserved.
        created_at (datetime.datetime):
        environment (None | PushDeviceEnvironmentType1 | PushDeviceEnvironmentType2Type1 |
            PushDeviceEnvironmentType3Type1 | Unset):
        token_id (UUID | Unset): API token that registered the device
        name (None | str | Unset):
        device_name (None | str | Unset):
        app_version (None | str | Unset):
        filters (PushFilters | Unset): Which notifications a push target receives (031; shared by subscriptions and
            native devices)
        content_level (PushDeviceContentLevel | Unset):
        lang (None | str | Unset):
        last_success_at (datetime.datetime | None | Unset):
        last_failure_at (datetime.datetime | None | Unset):
        failure_count (int | Unset):
        last_status (int | None | Unset):
    """

    id: UUID
    transport: PushDeviceTransport
    platform: PushDevicePlatform
    app_id: str
    token_suffix: str
    status: PushDeviceStatus
    created_at: datetime.datetime
    environment: (
        None
        | PushDeviceEnvironmentType1
        | PushDeviceEnvironmentType2Type1
        | PushDeviceEnvironmentType3Type1
        | Unset
    ) = UNSET
    token_id: UUID | Unset = UNSET
    name: None | str | Unset = UNSET
    device_name: None | str | Unset = UNSET
    app_version: None | str | Unset = UNSET
    filters: PushFilters | Unset = UNSET
    content_level: PushDeviceContentLevel | Unset = UNSET
    lang: None | str | Unset = UNSET
    last_success_at: datetime.datetime | None | Unset = UNSET
    last_failure_at: datetime.datetime | None | Unset = UNSET
    failure_count: int | Unset = UNSET
    last_status: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        transport = self.transport.value

        platform = self.platform.value

        app_id = self.app_id

        token_suffix = self.token_suffix

        status = self.status.value

        created_at = self.created_at.isoformat()

        environment: None | str | Unset
        if isinstance(self.environment, Unset):
            environment = UNSET
        elif (
            isinstance(self.environment, PushDeviceEnvironmentType1)
            or isinstance(self.environment, PushDeviceEnvironmentType2Type1)
            or isinstance(self.environment, PushDeviceEnvironmentType3Type1)
        ):
            environment = self.environment.value
        else:
            environment = self.environment

        token_id: str | Unset = UNSET
        if not isinstance(self.token_id, Unset):
            token_id = str(self.token_id)

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        device_name: None | str | Unset
        if isinstance(self.device_name, Unset):
            device_name = UNSET
        else:
            device_name = self.device_name

        app_version: None | str | Unset
        if isinstance(self.app_version, Unset):
            app_version = UNSET
        else:
            app_version = self.app_version

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

        last_success_at: None | str | Unset
        if isinstance(self.last_success_at, Unset):
            last_success_at = UNSET
        elif isinstance(self.last_success_at, datetime.datetime):
            last_success_at = self.last_success_at.isoformat()
        else:
            last_success_at = self.last_success_at

        last_failure_at: None | str | Unset
        if isinstance(self.last_failure_at, Unset):
            last_failure_at = UNSET
        elif isinstance(self.last_failure_at, datetime.datetime):
            last_failure_at = self.last_failure_at.isoformat()
        else:
            last_failure_at = self.last_failure_at

        failure_count = self.failure_count

        last_status: int | None | Unset
        if isinstance(self.last_status, Unset):
            last_status = UNSET
        else:
            last_status = self.last_status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "transport": transport,
                "platform": platform,
                "appId": app_id,
                "tokenSuffix": token_suffix,
                "status": status,
                "createdAt": created_at,
            }
        )
        if environment is not UNSET:
            field_dict["environment"] = environment
        if token_id is not UNSET:
            field_dict["tokenId"] = token_id
        if name is not UNSET:
            field_dict["name"] = name
        if device_name is not UNSET:
            field_dict["deviceName"] = device_name
        if app_version is not UNSET:
            field_dict["appVersion"] = app_version
        if filters is not UNSET:
            field_dict["filters"] = filters
        if content_level is not UNSET:
            field_dict["contentLevel"] = content_level
        if lang is not UNSET:
            field_dict["lang"] = lang
        if last_success_at is not UNSET:
            field_dict["lastSuccessAt"] = last_success_at
        if last_failure_at is not UNSET:
            field_dict["lastFailureAt"] = last_failure_at
        if failure_count is not UNSET:
            field_dict["failureCount"] = failure_count
        if last_status is not UNSET:
            field_dict["lastStatus"] = last_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_filters import PushFilters

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        transport = PushDeviceTransport(d.pop("transport"))

        platform = PushDevicePlatform(d.pop("platform"))

        app_id = d.pop("appId")

        token_suffix = d.pop("tokenSuffix")

        status = PushDeviceStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_environment(
            data: object,
        ) -> (
            None
            | PushDeviceEnvironmentType1
            | PushDeviceEnvironmentType2Type1
            | PushDeviceEnvironmentType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_1 = PushDeviceEnvironmentType1(data)

                return environment_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_2_type_1 = PushDeviceEnvironmentType2Type1(data)

                return environment_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                environment_type_3_type_1 = PushDeviceEnvironmentType3Type1(data)

                return environment_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | PushDeviceEnvironmentType1
                | PushDeviceEnvironmentType2Type1
                | PushDeviceEnvironmentType3Type1
                | Unset,
                data,
            )

        environment = _parse_environment(d.pop("environment", UNSET))

        _token_id = d.pop("tokenId", UNSET)
        token_id: UUID | Unset
        if isinstance(_token_id, Unset):
            token_id = UNSET
        else:
            token_id = UUID(_token_id)

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        def _parse_device_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        device_name = _parse_device_name(d.pop("deviceName", UNSET))

        def _parse_app_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        app_version = _parse_app_version(d.pop("appVersion", UNSET))

        _filters = d.pop("filters", UNSET)
        filters: PushFilters | Unset
        if isinstance(_filters, Unset):
            filters = UNSET
        else:
            filters = PushFilters.from_dict(_filters)

        _content_level = d.pop("contentLevel", UNSET)
        content_level: PushDeviceContentLevel | Unset
        if isinstance(_content_level, Unset):
            content_level = UNSET
        else:
            content_level = PushDeviceContentLevel(_content_level)

        def _parse_lang(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        lang = _parse_lang(d.pop("lang", UNSET))

        def _parse_last_success_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_success_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_success_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_success_at = _parse_last_success_at(d.pop("lastSuccessAt", UNSET))

        def _parse_last_failure_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_failure_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_failure_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_failure_at = _parse_last_failure_at(d.pop("lastFailureAt", UNSET))

        failure_count = d.pop("failureCount", UNSET)

        def _parse_last_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_status = _parse_last_status(d.pop("lastStatus", UNSET))

        push_device = cls(
            id=id,
            transport=transport,
            platform=platform,
            app_id=app_id,
            token_suffix=token_suffix,
            status=status,
            created_at=created_at,
            environment=environment,
            token_id=token_id,
            name=name,
            device_name=device_name,
            app_version=app_version,
            filters=filters,
            content_level=content_level,
            lang=lang,
            last_success_at=last_success_at,
            last_failure_at=last_failure_at,
            failure_count=failure_count,
            last_status=last_status,
        )

        push_device.additional_properties = d
        return push_device

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
