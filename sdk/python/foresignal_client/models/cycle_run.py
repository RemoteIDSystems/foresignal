from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cycle_run_cadence import CycleRunCadence
from ..models.cycle_run_status import CycleRunStatus
from ..models.cycle_run_trigger import CycleRunTrigger
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet import CyclePacket
    from ..models.cycle_run_roles_item import CycleRunRolesItem
    from ..models.maintenance_run import MaintenanceRun


T = TypeVar("T", bound="CycleRun")


@_attrs_define
class CycleRun:
    """One run of an analysis cycle or an on-demand situation assessment

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cadence (CycleRunCadence):
        trigger (CycleRunTrigger):
        status (CycleRunStatus):
        period_start (datetime.datetime):
        period_end (datetime.datetime):
        cycle_id (None | str | Unset):
        situation_id (None | str | Unset):
        previous_run_id (None | str | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        packet (CyclePacket | None | Unset):
        roles (list[CycleRunRolesItem] | Unset):
        tokens_used (int | Unset):
        assessment_id (None | str | Unset):
        maintenance (MaintenanceRun | None | Unset):
        manifest_id (None | str | Unset):
        warnings (list[str] | Unset):
        error (None | str | Unset):
    """

    id: str
    cadence: CycleRunCadence
    trigger: CycleRunTrigger
    status: CycleRunStatus
    period_start: datetime.datetime
    period_end: datetime.datetime
    cycle_id: None | str | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    previous_run_id: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    packet: CyclePacket | None | Unset = UNSET
    roles: list[CycleRunRolesItem] | Unset = UNSET
    tokens_used: int | Unset = UNSET
    assessment_id: None | str | Unset = UNSET
    maintenance: MaintenanceRun | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.cycle_packet import CyclePacket
        from ..models.maintenance_run import MaintenanceRun

        id = self.id

        cadence = self.cadence.value

        trigger = self.trigger.value

        status = self.status.value

        period_start = self.period_start.isoformat()

        period_end = self.period_end.isoformat()

        cycle_id: None | str | Unset
        if isinstance(self.cycle_id, Unset):
            cycle_id = UNSET
        else:
            cycle_id = self.cycle_id

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        previous_run_id: None | str | Unset
        if isinstance(self.previous_run_id, Unset):
            previous_run_id = UNSET
        else:
            previous_run_id = self.previous_run_id

        started_at: None | str | Unset
        if isinstance(self.started_at, Unset):
            started_at = UNSET
        elif isinstance(self.started_at, datetime.datetime):
            started_at = self.started_at.isoformat()
        else:
            started_at = self.started_at

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        packet: dict[str, Any] | None | Unset
        if isinstance(self.packet, Unset):
            packet = UNSET
        elif isinstance(self.packet, CyclePacket):
            packet = self.packet.to_dict()
        else:
            packet = self.packet

        roles: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.roles, Unset):
            roles = []
            for roles_item_data in self.roles:
                roles_item = roles_item_data.to_dict()
                roles.append(roles_item)

        tokens_used = self.tokens_used

        assessment_id: None | str | Unset
        if isinstance(self.assessment_id, Unset):
            assessment_id = UNSET
        else:
            assessment_id = self.assessment_id

        maintenance: dict[str, Any] | None | Unset
        if isinstance(self.maintenance, Unset):
            maintenance = UNSET
        elif isinstance(self.maintenance, MaintenanceRun):
            maintenance = self.maintenance.to_dict()
        else:
            maintenance = self.maintenance

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "cadence": cadence,
                "trigger": trigger,
                "status": status,
                "periodStart": period_start,
                "periodEnd": period_end,
            }
        )
        if cycle_id is not UNSET:
            field_dict["cycleId"] = cycle_id
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if previous_run_id is not UNSET:
            field_dict["previousRunId"] = previous_run_id
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if packet is not UNSET:
            field_dict["packet"] = packet
        if roles is not UNSET:
            field_dict["roles"] = roles
        if tokens_used is not UNSET:
            field_dict["tokensUsed"] = tokens_used
        if assessment_id is not UNSET:
            field_dict["assessmentId"] = assessment_id
        if maintenance is not UNSET:
            field_dict["maintenance"] = maintenance
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet import CyclePacket
        from ..models.cycle_run_roles_item import CycleRunRolesItem
        from ..models.maintenance_run import MaintenanceRun

        d = dict(src_dict)
        id = d.pop("id")

        cadence = CycleRunCadence(d.pop("cadence"))

        trigger = CycleRunTrigger(d.pop("trigger"))

        status = CycleRunStatus(d.pop("status"))

        period_start = datetime.datetime.fromisoformat(d.pop("periodStart"))

        period_end = datetime.datetime.fromisoformat(d.pop("periodEnd"))

        def _parse_cycle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cycle_id = _parse_cycle_id(d.pop("cycleId", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_previous_run_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        previous_run_id = _parse_previous_run_id(d.pop("previousRunId", UNSET))

        def _parse_started_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                started_at_type_0 = datetime.datetime.fromisoformat(data)

                return started_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        started_at = _parse_started_at(d.pop("startedAt", UNSET))

        def _parse_finished_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                finished_at_type_0 = datetime.datetime.fromisoformat(data)

                return finished_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        finished_at = _parse_finished_at(d.pop("finishedAt", UNSET))

        def _parse_packet(data: object) -> CyclePacket | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                packet_type_0 = CyclePacket.from_dict(data)

                return packet_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CyclePacket | None | Unset, data)

        packet = _parse_packet(d.pop("packet", UNSET))

        _roles = d.pop("roles", UNSET)
        roles: list[CycleRunRolesItem] | Unset = UNSET
        if _roles is not UNSET:
            roles = []
            for roles_item_data in _roles:
                roles_item = CycleRunRolesItem.from_dict(roles_item_data)

                roles.append(roles_item)

        tokens_used = d.pop("tokensUsed", UNSET)

        def _parse_assessment_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        assessment_id = _parse_assessment_id(d.pop("assessmentId", UNSET))

        def _parse_maintenance(data: object) -> MaintenanceRun | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                maintenance_type_0 = MaintenanceRun.from_dict(data)

                return maintenance_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MaintenanceRun | None | Unset, data)

        maintenance = _parse_maintenance(d.pop("maintenance", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        warnings = cast(list[str], d.pop("warnings", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        cycle_run = cls(
            id=id,
            cadence=cadence,
            trigger=trigger,
            status=status,
            period_start=period_start,
            period_end=period_end,
            cycle_id=cycle_id,
            situation_id=situation_id,
            previous_run_id=previous_run_id,
            started_at=started_at,
            finished_at=finished_at,
            packet=packet,
            roles=roles,
            tokens_used=tokens_used,
            assessment_id=assessment_id,
            maintenance=maintenance,
            manifest_id=manifest_id,
            warnings=warnings,
            error=error,
        )

        cycle_run.additional_properties = d
        return cycle_run

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
