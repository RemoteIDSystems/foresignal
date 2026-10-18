from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cpa_batch_request_filter import CpaBatchRequestFilter


T = TypeVar("T", bound="CpaBatchRequest")


@_attrs_define
class CpaBatchRequest:
    """One subject against up to 500 records from a filter (114)

    Attributes:
        subject (str): `{sourceId}/{externalId}`
        filter_ (CpaBatchRequestFilter): Candidate records
        horizon (str | Unset): CPA horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h). Default: '2h'.
            Example: 2h.
        max_dcpa_m (float | Unset):
        top (int | Unset):  Default: 20.
    """

    subject: str
    filter_: CpaBatchRequestFilter
    horizon: str | Unset = "2h"
    max_dcpa_m: float | Unset = UNSET
    top: int | Unset = 20
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        subject = self.subject

        filter_ = self.filter_.to_dict()

        horizon = self.horizon

        max_dcpa_m = self.max_dcpa_m

        top = self.top

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "subject": subject,
                "filter": filter_,
            }
        )
        if horizon is not UNSET:
            field_dict["horizon"] = horizon
        if max_dcpa_m is not UNSET:
            field_dict["maxDcpaM"] = max_dcpa_m
        if top is not UNSET:
            field_dict["top"] = top

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cpa_batch_request_filter import CpaBatchRequestFilter

        d = dict(src_dict)
        subject = d.pop("subject")

        filter_ = CpaBatchRequestFilter.from_dict(d.pop("filter"))

        horizon = d.pop("horizon", UNSET)

        max_dcpa_m = d.pop("maxDcpaM", UNSET)

        top = d.pop("top", UNSET)

        cpa_batch_request = cls(
            subject=subject,
            filter_=filter_,
            horizon=horizon,
            max_dcpa_m=max_dcpa_m,
            top=top,
        )

        cpa_batch_request.additional_properties = d
        return cpa_batch_request

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
