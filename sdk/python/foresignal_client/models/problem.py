from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.problem_class_type_1 import ProblemClassType1
from ..models.problem_class_type_2_type_1 import ProblemClassType2Type1
from ..models.problem_class_type_3_type_1 import ProblemClassType3Type1
from ..models.problem_required_scope_type_1 import ProblemRequiredScopeType1
from ..models.problem_required_scope_type_2_type_1 import ProblemRequiredScopeType2Type1
from ..models.problem_required_scope_type_3_type_1 import ProblemRequiredScopeType3Type1
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.problem_errors_item import ProblemErrorsItem
    from ..models.problem_references_type_0 import ProblemReferencesType0


T = TypeVar("T", bound="Problem")


@_attrs_define
class Problem:
    """
    Attributes:
        title (str):
        status (int):
        type_ (str | Unset):
        detail (str | Unset):
        instance (str | Unset):
        code (str | Unset): Stable machine code Example: CREDENTIALS_REQUIRED.
        errors (list[ProblemErrorsItem] | Unset):
        purge_horizon (int | None | Unset): RESYNC_REQUIRED: purge horizon seq
        watermark_seq (int | None | Unset): RESYNC_REQUIRED: current watermark
        horizon (datetime.datetime | None | Unset): ASOF_BEYOND_RETENTION: earliest supported asOf
        cap_usd (float | None | Unset): BUDGET_EXCEEDED: monthly money cap
        spent_usd (float | None | Unset): BUDGET_EXCEEDED: spent this month
        estimate_usd (float | None | Unset): BUDGET_EXCEEDED: estimated cost of the refused call
        required_scope (None | ProblemRequiredScopeType1 | ProblemRequiredScopeType2Type1 |
            ProblemRequiredScopeType3Type1 | Unset): INSUFFICIENT_SCOPE: the scope the operation needs (023)
        class_ (None | ProblemClassType1 | ProblemClassType2Type1 | ProblemClassType3Type1 | Unset): RATE_LIMITED: the
            rate-limit class that refused the request (035)
        retry_after_seconds (int | None | Unset): RATE_LIMITED: seconds to wait, as in Retry-After (035)
        references (None | ProblemReferencesType0 | Unset): PLACE_IN_USE (036): the rules, views and briefings that
            reference the place
        reason (None | str | Unset): VALIDATION_FAILED from ImageGuard (043): the refusal (file-too-large, unknown-
            format, bigtiff, content-type-mismatch, truncated, side-too-long, too-many-pixels, multi-image, unreadable)
    """

    title: str
    status: int
    type_: str | Unset = UNSET
    detail: str | Unset = UNSET
    instance: str | Unset = UNSET
    code: str | Unset = UNSET
    errors: list[ProblemErrorsItem] | Unset = UNSET
    purge_horizon: int | None | Unset = UNSET
    watermark_seq: int | None | Unset = UNSET
    horizon: datetime.datetime | None | Unset = UNSET
    cap_usd: float | None | Unset = UNSET
    spent_usd: float | None | Unset = UNSET
    estimate_usd: float | None | Unset = UNSET
    required_scope: (
        None
        | ProblemRequiredScopeType1
        | ProblemRequiredScopeType2Type1
        | ProblemRequiredScopeType3Type1
        | Unset
    ) = UNSET
    class_: (
        None
        | ProblemClassType1
        | ProblemClassType2Type1
        | ProblemClassType3Type1
        | Unset
    ) = UNSET
    retry_after_seconds: int | None | Unset = UNSET
    references: None | ProblemReferencesType0 | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.problem_references_type_0 import ProblemReferencesType0

        title = self.title

        status = self.status

        type_ = self.type_

        detail = self.detail

        instance = self.instance

        code = self.code

        errors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.errors, Unset):
            errors = []
            for errors_item_data in self.errors:
                errors_item = errors_item_data.to_dict()
                errors.append(errors_item)

        purge_horizon: int | None | Unset
        if isinstance(self.purge_horizon, Unset):
            purge_horizon = UNSET
        else:
            purge_horizon = self.purge_horizon

        watermark_seq: int | None | Unset
        if isinstance(self.watermark_seq, Unset):
            watermark_seq = UNSET
        else:
            watermark_seq = self.watermark_seq

        horizon: None | str | Unset
        if isinstance(self.horizon, Unset):
            horizon = UNSET
        elif isinstance(self.horizon, datetime.datetime):
            horizon = self.horizon.isoformat()
        else:
            horizon = self.horizon

        cap_usd: float | None | Unset
        if isinstance(self.cap_usd, Unset):
            cap_usd = UNSET
        else:
            cap_usd = self.cap_usd

        spent_usd: float | None | Unset
        if isinstance(self.spent_usd, Unset):
            spent_usd = UNSET
        else:
            spent_usd = self.spent_usd

        estimate_usd: float | None | Unset
        if isinstance(self.estimate_usd, Unset):
            estimate_usd = UNSET
        else:
            estimate_usd = self.estimate_usd

        required_scope: None | str | Unset
        if isinstance(self.required_scope, Unset):
            required_scope = UNSET
        elif (
            isinstance(self.required_scope, ProblemRequiredScopeType1)
            or isinstance(self.required_scope, ProblemRequiredScopeType2Type1)
            or isinstance(self.required_scope, ProblemRequiredScopeType3Type1)
        ):
            required_scope = self.required_scope.value
        else:
            required_scope = self.required_scope

        class_: None | str | Unset
        if isinstance(self.class_, Unset):
            class_ = UNSET
        elif (
            isinstance(self.class_, ProblemClassType1)
            or isinstance(self.class_, ProblemClassType2Type1)
            or isinstance(self.class_, ProblemClassType3Type1)
        ):
            class_ = self.class_.value
        else:
            class_ = self.class_

        retry_after_seconds: int | None | Unset
        if isinstance(self.retry_after_seconds, Unset):
            retry_after_seconds = UNSET
        else:
            retry_after_seconds = self.retry_after_seconds

        references: dict[str, Any] | None | Unset
        if isinstance(self.references, Unset):
            references = UNSET
        elif isinstance(self.references, ProblemReferencesType0):
            references = self.references.to_dict()
        else:
            references = self.references

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
                "status": status,
            }
        )
        if type_ is not UNSET:
            field_dict["type"] = type_
        if detail is not UNSET:
            field_dict["detail"] = detail
        if instance is not UNSET:
            field_dict["instance"] = instance
        if code is not UNSET:
            field_dict["code"] = code
        if errors is not UNSET:
            field_dict["errors"] = errors
        if purge_horizon is not UNSET:
            field_dict["purgeHorizon"] = purge_horizon
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq
        if horizon is not UNSET:
            field_dict["horizon"] = horizon
        if cap_usd is not UNSET:
            field_dict["capUsd"] = cap_usd
        if spent_usd is not UNSET:
            field_dict["spentUsd"] = spent_usd
        if estimate_usd is not UNSET:
            field_dict["estimateUsd"] = estimate_usd
        if required_scope is not UNSET:
            field_dict["requiredScope"] = required_scope
        if class_ is not UNSET:
            field_dict["class"] = class_
        if retry_after_seconds is not UNSET:
            field_dict["retryAfterSeconds"] = retry_after_seconds
        if references is not UNSET:
            field_dict["references"] = references
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.problem_errors_item import ProblemErrorsItem
        from ..models.problem_references_type_0 import ProblemReferencesType0

        d = dict(src_dict)
        title = d.pop("title")

        status = d.pop("status")

        type_ = d.pop("type", UNSET)

        detail = d.pop("detail", UNSET)

        instance = d.pop("instance", UNSET)

        code = d.pop("code", UNSET)

        _errors = d.pop("errors", UNSET)
        errors: list[ProblemErrorsItem] | Unset = UNSET
        if _errors is not UNSET:
            errors = []
            for errors_item_data in _errors:
                errors_item = ProblemErrorsItem.from_dict(errors_item_data)

                errors.append(errors_item)

        def _parse_purge_horizon(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        purge_horizon = _parse_purge_horizon(d.pop("purgeHorizon", UNSET))

        def _parse_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        watermark_seq = _parse_watermark_seq(d.pop("watermarkSeq", UNSET))

        def _parse_horizon(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                horizon_type_0 = datetime.datetime.fromisoformat(data)

                return horizon_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        horizon = _parse_horizon(d.pop("horizon", UNSET))

        def _parse_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        cap_usd = _parse_cap_usd(d.pop("capUsd", UNSET))

        def _parse_spent_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        spent_usd = _parse_spent_usd(d.pop("spentUsd", UNSET))

        def _parse_estimate_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        estimate_usd = _parse_estimate_usd(d.pop("estimateUsd", UNSET))

        def _parse_required_scope(
            data: object,
        ) -> (
            None
            | ProblemRequiredScopeType1
            | ProblemRequiredScopeType2Type1
            | ProblemRequiredScopeType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                required_scope_type_1 = ProblemRequiredScopeType1(data)

                return required_scope_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                required_scope_type_2_type_1 = ProblemRequiredScopeType2Type1(data)

                return required_scope_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                required_scope_type_3_type_1 = ProblemRequiredScopeType3Type1(data)

                return required_scope_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | ProblemRequiredScopeType1
                | ProblemRequiredScopeType2Type1
                | ProblemRequiredScopeType3Type1
                | Unset,
                data,
            )

        required_scope = _parse_required_scope(d.pop("requiredScope", UNSET))

        def _parse_class_(
            data: object,
        ) -> (
            None
            | ProblemClassType1
            | ProblemClassType2Type1
            | ProblemClassType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_1 = ProblemClassType1(data)

                return class_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_2_type_1 = ProblemClassType2Type1(data)

                return class_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                class_type_3_type_1 = ProblemClassType3Type1(data)

                return class_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | ProblemClassType1
                | ProblemClassType2Type1
                | ProblemClassType3Type1
                | Unset,
                data,
            )

        class_ = _parse_class_(d.pop("class", UNSET))

        def _parse_retry_after_seconds(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        retry_after_seconds = _parse_retry_after_seconds(
            d.pop("retryAfterSeconds", UNSET)
        )

        def _parse_references(data: object) -> None | ProblemReferencesType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                references_type_0 = ProblemReferencesType0.from_dict(data)

                return references_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ProblemReferencesType0 | Unset, data)

        references = _parse_references(d.pop("references", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        problem = cls(
            title=title,
            status=status,
            type_=type_,
            detail=detail,
            instance=instance,
            code=code,
            errors=errors,
            purge_horizon=purge_horizon,
            watermark_seq=watermark_seq,
            horizon=horizon,
            cap_usd=cap_usd,
            spent_usd=spent_usd,
            estimate_usd=estimate_usd,
            required_scope=required_scope,
            class_=class_,
            retry_after_seconds=retry_after_seconds,
            references=references,
            reason=reason,
        )

        problem.additional_properties = d
        return problem

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
