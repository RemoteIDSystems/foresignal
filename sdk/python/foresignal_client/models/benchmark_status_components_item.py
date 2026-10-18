from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_status_components_item_status import (
    BenchmarkStatusComponentsItemStatus,
)
from ..models.benchmark_status_components_item_suite_type_1 import (
    BenchmarkStatusComponentsItemSuiteType1,
)
from ..models.benchmark_status_components_item_suite_type_2_type_1 import (
    BenchmarkStatusComponentsItemSuiteType2Type1,
)
from ..models.benchmark_status_components_item_suite_type_3_type_1 import (
    BenchmarkStatusComponentsItemSuiteType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_status_components_item_gating import (
        BenchmarkStatusComponentsItemGating,
    )


T = TypeVar("T", bound="BenchmarkStatusComponentsItem")


@_attrs_define
class BenchmarkStatusComponentsItem:
    """Latest status of one component

    Attributes:
        component (str): Benchmarked component: `change-point`, `anomaly`, `event-study`, `co-occurrence`, `evidence`,
            `canonical-linking`, `precursor`, `situation-linker`, `forecaster:base-rate`, `forecaster:analog`,
            `forecaster:precursor` or `forecaster:ensemble` Example: change-point.
        status (BenchmarkStatusComponentsItemStatus): `unknown` when never benchmarked
        component_version (str | Unset):
        run_id (None | str | Unset):
        suite (BenchmarkStatusComponentsItemSuiteType1 | BenchmarkStatusComponentsItemSuiteType2Type1 |
            BenchmarkStatusComponentsItemSuiteType3Type1 | None | Unset):
        finished_at (datetime.datetime | None | Unset):
        version_changed (bool | Unset): The component's version differs from the one last benchmarked
        gating (BenchmarkStatusComponentsItemGating | Unset): Effect on autonomous features
    """

    component: str
    status: BenchmarkStatusComponentsItemStatus
    component_version: str | Unset = UNSET
    run_id: None | str | Unset = UNSET
    suite: (
        BenchmarkStatusComponentsItemSuiteType1
        | BenchmarkStatusComponentsItemSuiteType2Type1
        | BenchmarkStatusComponentsItemSuiteType3Type1
        | None
        | Unset
    ) = UNSET
    finished_at: datetime.datetime | None | Unset = UNSET
    version_changed: bool | Unset = UNSET
    gating: BenchmarkStatusComponentsItemGating | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        component = self.component

        status = self.status.value

        component_version = self.component_version

        run_id: None | str | Unset
        if isinstance(self.run_id, Unset):
            run_id = UNSET
        else:
            run_id = self.run_id

        suite: None | str | Unset
        if isinstance(self.suite, Unset):
            suite = UNSET
        elif (
            isinstance(self.suite, BenchmarkStatusComponentsItemSuiteType1)
            or isinstance(self.suite, BenchmarkStatusComponentsItemSuiteType2Type1)
            or isinstance(self.suite, BenchmarkStatusComponentsItemSuiteType3Type1)
        ):
            suite = self.suite.value
        else:
            suite = self.suite

        finished_at: None | str | Unset
        if isinstance(self.finished_at, Unset):
            finished_at = UNSET
        elif isinstance(self.finished_at, datetime.datetime):
            finished_at = self.finished_at.isoformat()
        else:
            finished_at = self.finished_at

        version_changed = self.version_changed

        gating: dict[str, Any] | Unset = UNSET
        if not isinstance(self.gating, Unset):
            gating = self.gating.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "component": component,
                "status": status,
            }
        )
        if component_version is not UNSET:
            field_dict["componentVersion"] = component_version
        if run_id is not UNSET:
            field_dict["runId"] = run_id
        if suite is not UNSET:
            field_dict["suite"] = suite
        if finished_at is not UNSET:
            field_dict["finishedAt"] = finished_at
        if version_changed is not UNSET:
            field_dict["versionChanged"] = version_changed
        if gating is not UNSET:
            field_dict["gating"] = gating

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_status_components_item_gating import (
            BenchmarkStatusComponentsItemGating,
        )

        d = dict(src_dict)
        component = d.pop("component")

        status = BenchmarkStatusComponentsItemStatus(d.pop("status"))

        component_version = d.pop("componentVersion", UNSET)

        def _parse_run_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run_id = _parse_run_id(d.pop("runId", UNSET))

        def _parse_suite(
            data: object,
        ) -> (
            BenchmarkStatusComponentsItemSuiteType1
            | BenchmarkStatusComponentsItemSuiteType2Type1
            | BenchmarkStatusComponentsItemSuiteType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suite_type_1 = BenchmarkStatusComponentsItemSuiteType1(data)

                return suite_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suite_type_2_type_1 = BenchmarkStatusComponentsItemSuiteType2Type1(data)

                return suite_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                suite_type_3_type_1 = BenchmarkStatusComponentsItemSuiteType3Type1(data)

                return suite_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                BenchmarkStatusComponentsItemSuiteType1
                | BenchmarkStatusComponentsItemSuiteType2Type1
                | BenchmarkStatusComponentsItemSuiteType3Type1
                | None
                | Unset,
                data,
            )

        suite = _parse_suite(d.pop("suite", UNSET))

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

        version_changed = d.pop("versionChanged", UNSET)

        _gating = d.pop("gating", UNSET)
        gating: BenchmarkStatusComponentsItemGating | Unset
        if isinstance(_gating, Unset):
            gating = UNSET
        else:
            gating = BenchmarkStatusComponentsItemGating.from_dict(_gating)

        benchmark_status_components_item = cls(
            component=component,
            status=status,
            component_version=component_version,
            run_id=run_id,
            suite=suite,
            finished_at=finished_at,
            version_changed=version_changed,
            gating=gating,
        )

        benchmark_status_components_item.additional_properties = d
        return benchmark_status_components_item

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
