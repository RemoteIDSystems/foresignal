from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_loop_frames_item_kind import ProductLoopFramesItemKind
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductLoopFramesItem")


@_attrs_define
class ProductLoopFramesItem:
    """Loop frame

    Attributes:
        product_id (str):
        time (datetime.datetime):
        frame_key (str):
        kind (ProductLoopFramesItemKind):
        url (str): Expanded `/image` URL for this frame
        time_key (str | Unset):
        run (None | str | Unset):
        step (int | None | Unset):
        bytes_ (int | None | Unset): Image size when already rendered
        expires_at (datetime.datetime | None | Unset):
    """

    product_id: str
    time: datetime.datetime
    frame_key: str
    kind: ProductLoopFramesItemKind
    url: str
    time_key: str | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    bytes_: int | None | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        time = self.time.isoformat()

        frame_key = self.frame_key

        kind = self.kind.value

        url = self.url

        time_key = self.time_key

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        step: int | None | Unset
        if isinstance(self.step, Unset):
            step = UNSET
        else:
            step = self.step

        bytes_: int | None | Unset
        if isinstance(self.bytes_, Unset):
            bytes_ = UNSET
        else:
            bytes_ = self.bytes_

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "time": time,
                "frameKey": frame_key,
                "kind": kind,
                "url": url,
            }
        )
        if time_key is not UNSET:
            field_dict["timeKey"] = time_key
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        product_id = d.pop("productId")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        frame_key = d.pop("frameKey")

        kind = ProductLoopFramesItemKind(d.pop("kind"))

        url = d.pop("url")

        time_key = d.pop("timeKey", UNSET)

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        step = _parse_step(d.pop("step", UNSET))

        def _parse_bytes_(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        bytes_ = _parse_bytes_(d.pop("bytes", UNSET))

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        product_loop_frames_item = cls(
            product_id=product_id,
            time=time,
            frame_key=frame_key,
            kind=kind,
            url=url,
            time_key=time_key,
            run=run,
            step=step,
            bytes_=bytes_,
            expires_at=expires_at,
        )

        product_loop_frames_item.additional_properties = d
        return product_loop_frames_item

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
