from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.s3_target import S3Target


T = TypeVar("T", bound="BackupSettings")


@_attrs_define
class BackupSettings:
    """Backup settings (settings key `backup`, prompt R2-017); secrets are write-only (B4/180). Backups land through the
    cold-archive tiers of R2-016: `local` and `external` on the external-disk tier (copied, fsynced, SHA-256 read back),
    `s3` on the S3 tier (ETag and size checked), each backup under `backups/{id}/`.

        Attributes:
            enabled (bool): Master switch: off, `runBackup` is 409 BACKUP_NOT_CONFIGURED and the schedule does not fire
                Default: False.
            target (str): `local`: under `root`, or the data directory when `root` is empty; `external`: a mounted disk at
                the absolute `root`; `s3`: the `s3` bucket Default: 'local'.
            schedule (None | str | Unset): Cron expression for scheduled backups: six fields (second minute hour day month
                weekday), evaluated in UTC; null for backups only on request
            root (None | str | Unset): Local/external backup directory; required and absolute for `external`; a relative
                `local` root is under the data directory
            s3 (None | S3Target | Unset):
            keep_backups (int | None | Unset): How many complete backups of each type to retain; older ones are pruned with
                their files, and failed ones older than the oldest kept are dropped. Null keeps every backup. 1 to 10000
                Default: 7.
            include_config (bool | Unset): A scheduled backup, and a `runBackup` that names no type, also writes the config
                bundle (type `full`); off, they are `database` only Default: True.
            secrets_set (list[str] | Unset): Names of secrets that have a stored value
            last_run_at (datetime.datetime | None | Unset):
    """

    enabled: bool = False
    target: str = "local"
    schedule: None | str | Unset = UNSET
    root: None | str | Unset = UNSET
    s3: None | S3Target | Unset = UNSET
    keep_backups: int | None | Unset = 7
    include_config: bool | Unset = True
    secrets_set: list[str] | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
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

        secrets_set: list[str] | Unset = UNSET
        if not isinstance(self.secrets_set, Unset):
            secrets_set = self.secrets_set

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

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
        if secrets_set is not UNSET:
            field_dict["secretsSet"] = secrets_set
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
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

        secrets_set = cast(list[str], d.pop("secretsSet", UNSET))

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        backup_settings = cls(
            enabled=enabled,
            target=target,
            schedule=schedule,
            root=root,
            s3=s3,
            keep_backups=keep_backups,
            include_config=include_config,
            secrets_set=secrets_set,
            last_run_at=last_run_at,
        )

        backup_settings.additional_properties = d
        return backup_settings

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
