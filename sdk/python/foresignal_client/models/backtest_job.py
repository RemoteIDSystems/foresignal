from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.backtest_job_status import BacktestJobStatus
from ..models.backtest_job_type import BacktestJobType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.backtest_caveat import BacktestCaveat
    from ..models.backtest_job_definition import BacktestJobDefinition
    from ..models.base_rate_refresh_result import BaseRateRefreshResult
    from ..models.event_study_result import EventStudyResult
    from ..models.precursor_batch import PrecursorBatch
    from ..models.precursor_test_result import PrecursorTestResult
    from ..models.recipe_backtest_result import RecipeBacktestResult
    from ..models.reference_import import ReferenceImport
    from ..models.replay_result import ReplayResult
    from ..models.rule_backtest_result import RuleBacktestResult
    from ..models.rule_quality import RuleQuality


T = TypeVar("T", bound="BacktestJob")


@_attrs_define
class BacktestJob:
    """An asynchronous backtest job with its definition snapshot, range, progress, caveats and result

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (BacktestJobType):
        definition_hash (str):
        from_seq (int):
        to_seq (int):
        status (BacktestJobStatus):
        progress (float):
        created_at (datetime.datetime):
        caveats (list[BacktestCaveat]):
        target_id (None | str | Unset):
        definition (BacktestJobDefinition | Unset): Snapshot taken at submit time
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        started_at (datetime.datetime | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        created_by (str | Unset):
        detector_version (str | Unset):
        result (BaseRateRefreshResult | EventStudyResult | None | PrecursorBatch | PrecursorTestResult |
            RecipeBacktestResult | ReferenceImport | ReplayResult | RuleBacktestResult | RuleQuality | Unset): By job type;
            ≤ 1 MB
        truncated (bool | Unset):
        error (None | str | Unset):
        manifest_id (None | str | Unset): The analysis manifest of this output (prompt 116); null for outputs written
            before it
    """

    id: str
    type_: BacktestJobType
    definition_hash: str
    from_seq: int
    to_seq: int
    status: BacktestJobStatus
    progress: float
    created_at: datetime.datetime
    caveats: list[BacktestCaveat]
    target_id: None | str | Unset = UNSET
    definition: BacktestJobDefinition | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    started_at: datetime.datetime | None | Unset = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    created_by: str | Unset = UNSET
    detector_version: str | Unset = UNSET
    result: (
        BaseRateRefreshResult
        | EventStudyResult
        | None
        | PrecursorBatch
        | PrecursorTestResult
        | RecipeBacktestResult
        | ReferenceImport
        | ReplayResult
        | RuleBacktestResult
        | RuleQuality
        | Unset
    ) = UNSET
    truncated: bool | Unset = UNSET
    error: None | str | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.base_rate_refresh_result import BaseRateRefreshResult
        from ..models.event_study_result import EventStudyResult
        from ..models.precursor_batch import PrecursorBatch
        from ..models.precursor_test_result import PrecursorTestResult
        from ..models.recipe_backtest_result import RecipeBacktestResult
        from ..models.reference_import import ReferenceImport
        from ..models.replay_result import ReplayResult
        from ..models.rule_backtest_result import RuleBacktestResult
        from ..models.rule_quality import RuleQuality

        id = self.id

        type_ = self.type_.value

        definition_hash = self.definition_hash

        from_seq = self.from_seq

        to_seq = self.to_seq

        status = self.status.value

        progress = self.progress

        created_at = self.created_at.isoformat()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        target_id: None | str | Unset
        if isinstance(self.target_id, Unset):
            target_id = UNSET
        else:
            target_id = self.target_id

        definition: dict[str, Any] | Unset = UNSET
        if not isinstance(self.definition, Unset):
            definition = self.definition.to_dict()

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

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

        created_by = self.created_by

        detector_version = self.detector_version

        result: dict[str, Any] | None | Unset
        if isinstance(self.result, Unset):
            result = UNSET
        elif (
            isinstance(self.result, RuleBacktestResult)
            or isinstance(self.result, RecipeBacktestResult)
            or isinstance(self.result, PrecursorTestResult)
            or isinstance(self.result, RuleQuality)
            or isinstance(self.result, ReplayResult)
            or isinstance(self.result, EventStudyResult)
            or isinstance(self.result, PrecursorBatch)
            or isinstance(self.result, ReferenceImport)
            or isinstance(self.result, BaseRateRefreshResult)
        ):
            result = self.result.to_dict()
        else:
            result = self.result

        truncated = self.truncated

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "definitionHash": definition_hash,
                "fromSeq": from_seq,
                "toSeq": to_seq,
                "status": status,
                "progress": progress,
                "createdAt": created_at,
                "caveats": caveats,
            }
        )
        if target_id is not UNSET:
            field_dict["targetId"] = target_id
        if definition is not UNSET:
            field_dict["definition"] = definition
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if started_at is not UNSET:
            field_dict["startedAt"] = started_at
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if detector_version is not UNSET:
            field_dict["detectorVersion"] = detector_version
        if result is not UNSET:
            field_dict["result"] = result
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if error is not UNSET:
            field_dict["error"] = error
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.backtest_caveat import BacktestCaveat
        from ..models.backtest_job_definition import BacktestJobDefinition
        from ..models.base_rate_refresh_result import BaseRateRefreshResult
        from ..models.event_study_result import EventStudyResult
        from ..models.precursor_batch import PrecursorBatch
        from ..models.precursor_test_result import PrecursorTestResult
        from ..models.recipe_backtest_result import RecipeBacktestResult
        from ..models.reference_import import ReferenceImport
        from ..models.replay_result import ReplayResult
        from ..models.rule_backtest_result import RuleBacktestResult
        from ..models.rule_quality import RuleQuality

        d = dict(src_dict)
        id = d.pop("id")

        type_ = BacktestJobType(d.pop("type"))

        definition_hash = d.pop("definitionHash")

        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        status = BacktestJobStatus(d.pop("status"))

        progress = d.pop("progress")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = BacktestCaveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        def _parse_target_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_id = _parse_target_id(d.pop("targetId", UNSET))

        _definition = d.pop("definition", UNSET)
        definition: BacktestJobDefinition | Unset
        if isinstance(_definition, Unset):
            definition = UNSET
        else:
            definition = BacktestJobDefinition.from_dict(_definition)

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

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

        created_by = d.pop("createdBy", UNSET)

        detector_version = d.pop("detectorVersion", UNSET)

        def _parse_result(
            data: object,
        ) -> (
            BaseRateRefreshResult
            | EventStudyResult
            | None
            | PrecursorBatch
            | PrecursorTestResult
            | RecipeBacktestResult
            | ReferenceImport
            | ReplayResult
            | RuleBacktestResult
            | RuleQuality
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_0 = RuleBacktestResult.from_dict(data)

                return result_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_1 = RecipeBacktestResult.from_dict(data)

                return result_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_2 = PrecursorTestResult.from_dict(data)

                return result_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_3 = RuleQuality.from_dict(data)

                return result_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_4 = ReplayResult.from_dict(data)

                return result_type_4
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_5 = EventStudyResult.from_dict(data)

                return result_type_5
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_6 = PrecursorBatch.from_dict(data)

                return result_type_6
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_7 = ReferenceImport.from_dict(data)

                return result_type_7
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_8 = BaseRateRefreshResult.from_dict(data)

                return result_type_8
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                BaseRateRefreshResult
                | EventStudyResult
                | None
                | PrecursorBatch
                | PrecursorTestResult
                | RecipeBacktestResult
                | ReferenceImport
                | ReplayResult
                | RuleBacktestResult
                | RuleQuality
                | Unset,
                data,
            )

        result = _parse_result(d.pop("result", UNSET))

        truncated = d.pop("truncated", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        backtest_job = cls(
            id=id,
            type_=type_,
            definition_hash=definition_hash,
            from_seq=from_seq,
            to_seq=to_seq,
            status=status,
            progress=progress,
            created_at=created_at,
            caveats=caveats,
            target_id=target_id,
            definition=definition,
            from_=from_,
            to=to,
            started_at=started_at,
            finished_at=finished_at,
            created_by=created_by,
            detector_version=detector_version,
            result=result,
            truncated=truncated,
            error=error,
            manifest_id=manifest_id,
        )

        backtest_job.additional_properties = d
        return backtest_job

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
