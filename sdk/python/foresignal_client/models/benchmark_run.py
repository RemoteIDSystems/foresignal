from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_run_job_status import BenchmarkRunJobStatus
from ..models.benchmark_run_status_type_1 import BenchmarkRunStatusType1
from ..models.benchmark_run_status_type_2_type_1 import BenchmarkRunStatusType2Type1
from ..models.benchmark_run_status_type_3_type_1 import BenchmarkRunStatusType3Type1
from ..models.benchmark_run_suite import BenchmarkRunSuite
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_component_result import BenchmarkComponentResult


T = TypeVar("T", bound="BenchmarkRun")


@_attrs_define
class BenchmarkRun:
    """A benchmark run over the synthetic suite, the historical corpus or both

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        suite (BenchmarkRunSuite):
        suite_version (str):  Example: bench-scenarios 1.0.0.
        seed (int):
        components (list[str]):
        job_status (BenchmarkRunJobStatus): 103 job lifecycle
        status (BenchmarkRunStatusType1 | BenchmarkRunStatusType2Type1 | BenchmarkRunStatusType3Type1 | None): Worst
            component status; null until the run is done
        scorer_version (str | Unset):  Example: BenchmarkScorer 1.0.0.
        progress (float | Unset):
        results (list[BenchmarkComponentResult] | Unset):
        manifest_id (None | str | Unset):
        created_by (None | str | Unset): Principal or `scheduler`
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        error (None | str | Unset):
    """

    id: str
    suite: BenchmarkRunSuite
    suite_version: str
    seed: int
    components: list[str]
    job_status: BenchmarkRunJobStatus
    status: (
        BenchmarkRunStatusType1
        | BenchmarkRunStatusType2Type1
        | BenchmarkRunStatusType3Type1
        | None
    )
    scorer_version: str | Unset = UNSET
    progress: float | Unset = UNSET
    results: list[BenchmarkComponentResult] | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    created_by: None | str | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        suite = self.suite.value

        suite_version = self.suite_version

        seed = self.seed

        components = self.components

        job_status = self.job_status.value

        status: None | str
        if (
            isinstance(self.status, BenchmarkRunStatusType1)
            or isinstance(self.status, BenchmarkRunStatusType2Type1)
            or isinstance(self.status, BenchmarkRunStatusType3Type1)
        ):
            status = self.status.value
        else:
            status = self.status

        scorer_version = self.scorer_version

        progress = self.progress

        results: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.results, Unset):
            results = []
            for results_item_data in self.results:
                results_item = results_item_data.to_dict()
                results.append(results_item)

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        created_by: None | str | Unset
        if isinstance(self.created_by, Unset):
            created_by = UNSET
        else:
            created_by = self.created_by

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
                "suite": suite,
                "suiteVersion": suite_version,
                "seed": seed,
                "components": components,
                "jobStatus": job_status,
                "status": status,
            }
        )
        if scorer_version is not UNSET:
            field_dict["scorerVersion"] = scorer_version
        if progress is not UNSET:
            field_dict["progress"] = progress
        if results is not UNSET:
            field_dict["results"] = results
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_component_result import BenchmarkComponentResult

        d = dict(src_dict)
        id = d.pop("id")

        suite = BenchmarkRunSuite(d.pop("suite"))

        suite_version = d.pop("suiteVersion")

        seed = d.pop("seed")

        components = cast(list[str], d.pop("components"))

        job_status = BenchmarkRunJobStatus(d.pop("jobStatus"))

        def _parse_status(
            data: object,
        ) -> (
            BenchmarkRunStatusType1
            | BenchmarkRunStatusType2Type1
            | BenchmarkRunStatusType3Type1
            | None
        ):
            if data is None:
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_1 = BenchmarkRunStatusType1(data)

                return status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_2_type_1 = BenchmarkRunStatusType2Type1(data)

                return status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_3_type_1 = BenchmarkRunStatusType3Type1(data)

                return status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                BenchmarkRunStatusType1
                | BenchmarkRunStatusType2Type1
                | BenchmarkRunStatusType3Type1
                | None,
                data,
            )

        status = _parse_status(d.pop("status"))

        scorer_version = d.pop("scorerVersion", UNSET)

        progress = d.pop("progress", UNSET)

        _results = d.pop("results", UNSET)
        results: list[BenchmarkComponentResult] | Unset = UNSET
        if _results is not UNSET:
            results = []
            for results_item_data in _results:
                results_item = BenchmarkComponentResult.from_dict(results_item_data)

                results.append(results_item)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        def _parse_created_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        created_by = _parse_created_by(d.pop("createdBy", UNSET))

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

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        benchmark_run = cls(
            id=id,
            suite=suite,
            suite_version=suite_version,
            seed=seed,
            components=components,
            job_status=job_status,
            status=status,
            scorer_version=scorer_version,
            progress=progress,
            results=results,
            manifest_id=manifest_id,
            created_by=created_by,
            started_at=started_at,
            finished_at=finished_at,
            error=error,
        )

        benchmark_run.additional_properties = d
        return benchmark_run

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
