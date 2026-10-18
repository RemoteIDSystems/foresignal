from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.doctor_report_status import DoctorReportStatus
from ..models.doctor_report_trigger import DoctorReportTrigger
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.doctor_finding import DoctorFinding
    from ..models.doctor_report_fixture_type_0 import DoctorReportFixtureType0
    from ..models.doctor_report_llm_type_0 import DoctorReportLlmType0
    from ..models.doctor_report_probe_type_0 import DoctorReportProbeType0
    from ..models.structure_diff import StructureDiff


T = TypeVar("T", bound="DoctorReport")


@_attrs_define
class DoctorReport:
    """A persisted adapter-doctor report (retained 90 days); no raw upstream body is stored

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        created_at (datetime.datetime):
        trigger (DoctorReportTrigger):
        status (DoctorReportStatus):
        findings (list[DoctorFinding]):
        diff (None | StructureDiff | Unset):
        probe (DoctorReportProbeType0 | None | Unset):
        fixture (DoctorReportFixtureType0 | None | Unset):
        llm (DoctorReportLlmType0 | None | Unset):
        adapter_version (None | str | Unset):
    """

    id: str
    source_id: str
    created_at: datetime.datetime
    trigger: DoctorReportTrigger
    status: DoctorReportStatus
    findings: list[DoctorFinding]
    diff: None | StructureDiff | Unset = UNSET
    probe: DoctorReportProbeType0 | None | Unset = UNSET
    fixture: DoctorReportFixtureType0 | None | Unset = UNSET
    llm: DoctorReportLlmType0 | None | Unset = UNSET
    adapter_version: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.doctor_report_fixture_type_0 import DoctorReportFixtureType0
        from ..models.doctor_report_llm_type_0 import DoctorReportLlmType0
        from ..models.doctor_report_probe_type_0 import DoctorReportProbeType0
        from ..models.structure_diff import StructureDiff

        id = self.id

        source_id = self.source_id

        created_at = self.created_at.isoformat()

        trigger = self.trigger.value

        status = self.status.value

        findings = []
        for findings_item_data in self.findings:
            findings_item = findings_item_data.to_dict()
            findings.append(findings_item)

        diff: dict[str, Any] | None | Unset
        if isinstance(self.diff, Unset):
            diff = UNSET
        elif isinstance(self.diff, StructureDiff):
            diff = self.diff.to_dict()
        else:
            diff = self.diff

        probe: dict[str, Any] | None | Unset
        if isinstance(self.probe, Unset):
            probe = UNSET
        elif isinstance(self.probe, DoctorReportProbeType0):
            probe = self.probe.to_dict()
        else:
            probe = self.probe

        fixture: dict[str, Any] | None | Unset
        if isinstance(self.fixture, Unset):
            fixture = UNSET
        elif isinstance(self.fixture, DoctorReportFixtureType0):
            fixture = self.fixture.to_dict()
        else:
            fixture = self.fixture

        llm: dict[str, Any] | None | Unset
        if isinstance(self.llm, Unset):
            llm = UNSET
        elif isinstance(self.llm, DoctorReportLlmType0):
            llm = self.llm.to_dict()
        else:
            llm = self.llm

        adapter_version: None | str | Unset
        if isinstance(self.adapter_version, Unset):
            adapter_version = UNSET
        else:
            adapter_version = self.adapter_version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "sourceId": source_id,
                "createdAt": created_at,
                "trigger": trigger,
                "status": status,
                "findings": findings,
            }
        )
        if diff is not UNSET:
            field_dict["diff"] = diff
        if probe is not UNSET:
            field_dict["probe"] = probe
        if fixture is not UNSET:
            field_dict["fixture"] = fixture
        if llm is not UNSET:
            field_dict["llm"] = llm
        if adapter_version is not UNSET:
            field_dict["adapterVersion"] = adapter_version

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.doctor_finding import DoctorFinding
        from ..models.doctor_report_fixture_type_0 import DoctorReportFixtureType0
        from ..models.doctor_report_llm_type_0 import DoctorReportLlmType0
        from ..models.doctor_report_probe_type_0 import DoctorReportProbeType0
        from ..models.structure_diff import StructureDiff

        d = dict(src_dict)
        id = d.pop("id")

        source_id = d.pop("sourceId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        trigger = DoctorReportTrigger(d.pop("trigger"))

        status = DoctorReportStatus(d.pop("status"))

        findings = []
        _findings = d.pop("findings")
        for findings_item_data in _findings:
            findings_item = DoctorFinding.from_dict(findings_item_data)

            findings.append(findings_item)

        def _parse_diff(data: object) -> None | StructureDiff | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                diff_type_0 = StructureDiff.from_dict(data)

                return diff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StructureDiff | Unset, data)

        diff = _parse_diff(d.pop("diff", UNSET))

        def _parse_probe(data: object) -> DoctorReportProbeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                probe_type_0 = DoctorReportProbeType0.from_dict(data)

                return probe_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DoctorReportProbeType0 | None | Unset, data)

        probe = _parse_probe(d.pop("probe", UNSET))

        def _parse_fixture(data: object) -> DoctorReportFixtureType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                fixture_type_0 = DoctorReportFixtureType0.from_dict(data)

                return fixture_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DoctorReportFixtureType0 | None | Unset, data)

        fixture = _parse_fixture(d.pop("fixture", UNSET))

        def _parse_llm(data: object) -> DoctorReportLlmType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                llm_type_0 = DoctorReportLlmType0.from_dict(data)

                return llm_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DoctorReportLlmType0 | None | Unset, data)

        llm = _parse_llm(d.pop("llm", UNSET))

        def _parse_adapter_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        adapter_version = _parse_adapter_version(d.pop("adapterVersion", UNSET))

        doctor_report = cls(
            id=id,
            source_id=source_id,
            created_at=created_at,
            trigger=trigger,
            status=status,
            findings=findings,
            diff=diff,
            probe=probe,
            fixture=fixture,
            llm=llm,
            adapter_version=adapter_version,
        )

        doctor_report.additional_properties = d
        return doctor_report

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
