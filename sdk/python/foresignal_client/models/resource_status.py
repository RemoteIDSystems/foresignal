from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.resource_gauge import ResourceGauge
    from ..models.resource_status_database import ResourceStatusDatabase


T = TypeVar("T", bound="ResourceStatus")


@_attrs_define
class ResourceStatus:
    """Live host disk, memory and database gauges with an overall status (177)

    Attributes:
        status (str): Worst of the gauges
        disk (ResourceGauge): A single resource reading
        checked_at (datetime.datetime): When the reading was taken; readings are reused for up to 30 s
        memory (ResourceGauge | Unset): A single resource reading
        database (ResourceStatusDatabase | Unset):
        warnings (list[str] | Unset): Human-readable breach messages
    """

    status: str
    disk: ResourceGauge
    checked_at: datetime.datetime
    memory: ResourceGauge | Unset = UNSET
    database: ResourceStatusDatabase | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status

        disk = self.disk.to_dict()

        checked_at = self.checked_at.isoformat()

        memory: dict[str, Any] | Unset = UNSET
        if not isinstance(self.memory, Unset):
            memory = self.memory.to_dict()

        database: dict[str, Any] | Unset = UNSET
        if not isinstance(self.database, Unset):
            database = self.database.to_dict()

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
                "disk": disk,
                "checkedAt": checked_at,
            }
        )
        if memory is not UNSET:
            field_dict["memory"] = memory
        if database is not UNSET:
            field_dict["database"] = database
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.resource_gauge import ResourceGauge
        from ..models.resource_status_database import ResourceStatusDatabase

        d = dict(src_dict)
        status = d.pop("status")

        disk = ResourceGauge.from_dict(d.pop("disk"))

        checked_at = datetime.datetime.fromisoformat(d.pop("checkedAt"))

        _memory = d.pop("memory", UNSET)
        memory: ResourceGauge | Unset
        if isinstance(_memory, Unset):
            memory = UNSET
        else:
            memory = ResourceGauge.from_dict(_memory)

        _database = d.pop("database", UNSET)
        database: ResourceStatusDatabase | Unset
        if isinstance(_database, Unset):
            database = UNSET
        else:
            database = ResourceStatusDatabase.from_dict(_database)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        resource_status = cls(
            status=status,
            disk=disk,
            checked_at=checked_at,
            memory=memory,
            database=database,
            warnings=warnings,
        )

        resource_status.additional_properties = d
        return resource_status

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
