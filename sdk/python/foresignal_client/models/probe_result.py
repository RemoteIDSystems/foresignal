from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.normalized_record_preview import NormalizedRecordPreview
    from ..models.probe_result_requests_item import ProbeResultRequestsItem


T = TypeVar("T", bound="ProbeResult")


@_attrs_define
class ProbeResult:
    """
    Attributes:
        source_id (str):
        requests (list[ProbeResultRequestsItem]):
        records (list[NormalizedRecordPreview]):
        parse_error (None | str | Unset):
    """

    source_id: str
    requests: list[ProbeResultRequestsItem]
    records: list[NormalizedRecordPreview]
    parse_error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        requests = []
        for requests_item_data in self.requests:
            requests_item = requests_item_data.to_dict()
            requests.append(requests_item)

        records = []
        for records_item_data in self.records:
            records_item = records_item_data.to_dict()
            records.append(records_item)

        parse_error: None | str | Unset
        if isinstance(self.parse_error, Unset):
            parse_error = UNSET
        else:
            parse_error = self.parse_error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "requests": requests,
                "records": records,
            }
        )
        if parse_error is not UNSET:
            field_dict["parseError"] = parse_error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.normalized_record_preview import NormalizedRecordPreview
        from ..models.probe_result_requests_item import ProbeResultRequestsItem

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        requests = []
        _requests = d.pop("requests")
        for requests_item_data in _requests:
            requests_item = ProbeResultRequestsItem.from_dict(requests_item_data)

            requests.append(requests_item)

        records = []
        _records = d.pop("records")
        for records_item_data in _records:
            records_item = NormalizedRecordPreview.from_dict(records_item_data)

            records.append(records_item)

        def _parse_parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        parse_error = _parse_parse_error(d.pop("parseError", UNSET))

        probe_result = cls(
            source_id=source_id,
            requests=requests,
            records=records,
            parse_error=parse_error,
        )

        probe_result.additional_properties = d
        return probe_result

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
