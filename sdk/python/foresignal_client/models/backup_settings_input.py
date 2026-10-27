from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.backup_settings_input_secrets import BackupSettingsInputSecrets
    from ..models.s3_target import S3Target


T = TypeVar("T", bound="BackupSettingsInput")


@_attrs_define
class BackupSettingsInput:
    """Backup settings with write-only S3 credentials. Replaces the settings: an omitted member takes its default, except
    `secrets`, where an omitted member keeps the stored value and an empty string or null clears it. Unknown members are
    a 400; the read-only `secretsSet` and `lastRunAt` of a GET may be sent back and are ignored.

        Attributes:
            enabled (bool):
            target (str):
            schedule (None | str | Unset):
            root (None | str | Unset):
            s3 (None | S3Target | Unset):
            keep_backups (int | None | Unset):
            include_config (bool | Unset):  Default: True.
            secrets (BackupSettingsInputSecrets | Unset): Write-only S3 credentials
    """

    enabled: bool
    target: str
    schedule: None | str | Unset = UNSET
    root: None | str | Unset = UNSET
    s3: None | S3Target | Unset = UNSET
    keep_backups: int | None | Unset = UNSET
    include_config: bool | Unset = True
    secrets: BackupSettingsInputSecrets | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.s3_target import S3Target

        enabled = self.enabled

        target = self.target

        schedule: None | str | Unset
        if isinstance(self.schedule, Unset):
            schedule = UNSET
        else:
            schedule = self.schedule

        root: None | str | Unset
        if isinstance(self.root, Unset):
            root = UNSET
        else:
            root = self.root

        s3: dict[str, Any] | None | Unset
        if isinstance(self.s3, Unset):
            s3 = UNSET
        elif isinstance(self.s3, S3Target):
            s3 = self.s3.to_dict()
        else:
            s3 = self.s3

        keep_backups: int | None | Unset
        if isinstance(self.keep_backups, Unset):
            keep_backups = UNSET
        else:
            keep_backups = self.keep_backups

        include_config = self.include_config

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
                "target": target,
            }
        )
        if schedule is not UNSET:
            field_dict["schedule"] = schedule
        if root is not UNSET:
            field_dict["root"] = root
        if s3 is not UNSET:
            field_dict["s3"] = s3
        if keep_backups is not UNSET:
            field_dict["keepBackups"] = keep_backups
        if include_config is not UNSET:
            field_dict["includeConfig"] = include_config
        if secrets is not UNSET:
            field_dict["secrets"] = secrets

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.backup_settings_input_secrets import BackupSettingsInputSecrets
        from ..models.s3_target import S3Target

        d = dict(src_dict)
        enabled = d.pop("enabled")

        target = d.pop("target")

        def _parse_schedule(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        schedule = _parse_schedule(d.pop("schedule", UNSET))

        def _parse_root(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        root = _parse_root(d.pop("root", UNSET))

        def _parse_s3(data: object) -> None | S3Target | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                s3_type_0 = S3Target.from_dict(data)

                return s3_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | S3Target | Unset, data)

        s3 = _parse_s3(d.pop("s3", UNSET))

        def _parse_keep_backups(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        keep_backups = _parse_keep_backups(d.pop("keepBackups", UNSET))

        include_config = d.pop("includeConfig", UNSET)

        _secrets = d.pop("secrets", UNSET)
        secrets: BackupSettingsInputSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = BackupSettingsInputSecrets.from_dict(_secrets)

        backup_settings_input = cls(
            enabled=enabled,
            target=target,
            schedule=schedule,
            root=root,
            s3=s3,
            keep_backups=keep_backups,
            include_config=include_config,
            secrets=secrets,
        )

        backup_settings_input.additional_properties = d
        return backup_settings_input

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
