from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_provider_type import PushProviderType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.apns_provider_config import ApnsProviderConfig
    from ..models.fcm_provider_config import FcmProviderConfig


T = TypeVar("T", bound="PushProvider")


@_attrs_define
class PushProvider:
    """A native push provider; secrets are never returned (032)

    Attributes:
        id (str):
        type_ (PushProviderType):
        app_ids (list[str]):
        config (ApnsProviderConfig | FcmProviderConfig):
        enabled (bool):
        secret_fields (list[str] | Unset): Names of secrets that are set
        last_test_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    type_: PushProviderType
    app_ids: list[str]
    config: ApnsProviderConfig | FcmProviderConfig
    enabled: bool
    secret_fields: list[str] | Unset = UNSET
    last_test_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.apns_provider_config import ApnsProviderConfig

        id = self.id

        type_ = self.type_.value

        app_ids = self.app_ids

        config: dict[str, Any]
        if isinstance(self.config, ApnsProviderConfig):
            config = self.config.to_dict()
        else:
            config = self.config.to_dict()

        enabled = self.enabled

        secret_fields: list[str] | Unset = UNSET
        if not isinstance(self.secret_fields, Unset):
            secret_fields = self.secret_fields

        last_test_at: None | str | Unset
        if isinstance(self.last_test_at, Unset):
            last_test_at = UNSET
        elif isinstance(self.last_test_at, datetime.datetime):
            last_test_at = self.last_test_at.isoformat()
        else:
            last_test_at = self.last_test_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "appIds": app_ids,
                "config": config,
                "enabled": enabled,
            }
        )
        if secret_fields is not UNSET:
            field_dict["secretFields"] = secret_fields
        if last_test_at is not UNSET:
            field_dict["lastTestAt"] = last_test_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.apns_provider_config import ApnsProviderConfig
        from ..models.fcm_provider_config import FcmProviderConfig

        d = dict(src_dict)
        id = d.pop("id")

        type_ = PushProviderType(d.pop("type"))

        app_ids = cast(list[str], d.pop("appIds"))

        def _parse_config(data: object) -> ApnsProviderConfig | FcmProviderConfig:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                config_type_0 = ApnsProviderConfig.from_dict(data)

                return config_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            config_type_1 = FcmProviderConfig.from_dict(data)

            return config_type_1

        config = _parse_config(d.pop("config"))

        enabled = d.pop("enabled")

        secret_fields = cast(list[str], d.pop("secretFields", UNSET))

        def _parse_last_test_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_test_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_test_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_test_at = _parse_last_test_at(d.pop("lastTestAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        push_provider = cls(
            id=id,
            type_=type_,
            app_ids=app_ids,
            config=config,
            enabled=enabled,
            secret_fields=secret_fields,
            last_test_at=last_test_at,
            last_error=last_error,
            updated_at=updated_at,
        )

        push_provider.additional_properties = d
        return push_provider

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
