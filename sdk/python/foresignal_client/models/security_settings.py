from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="SecuritySettings")


@_attrs_define
class SecuritySettings:
    """
    Attributes:
        open_reads (bool): When false, data GETs require a read token Default: True.
        expose_observer (bool | Unset): Return record observers to read-scope and anonymous callers with
            include=observer Default: False.
        anonymous (ReadRestriction | Unset): The effective read restriction of a principal (035): restricted tokens,
            feed keys and the anonymous profile. Intersected with every request filter by `RestrictionClamp`; out-of-scope
            single reads return 404.
        metrics_open (bool | Unset): When false, /actuator/prometheus requires an admin token (035) Default: True.
        expose_mlat (bool | Unset): When false (the default), records whose `posSource` is `mlat` are stripped from
            anonymous openReads responses of the records API (list, get, versions, history, asOf); a record whose
            `posSource` was projected away by `fields=` is stripped too when its source reports `posSource`. MLAT positions
            are never sent on sinks, federation, share links or feeds regardless (069) Default: False.
    """

    open_reads: bool = True
    expose_observer: bool | Unset = False
    anonymous: ReadRestriction | Unset = UNSET
    metrics_open: bool | Unset = True
    expose_mlat: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        open_reads = self.open_reads

        expose_observer = self.expose_observer

        anonymous: dict[str, Any] | Unset = UNSET
        if not isinstance(self.anonymous, Unset):
            anonymous = self.anonymous.to_dict()

        metrics_open = self.metrics_open

        expose_mlat = self.expose_mlat

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "openReads": open_reads,
            }
        )
        if expose_observer is not UNSET:
            field_dict["exposeObserver"] = expose_observer
        if anonymous is not UNSET:
            field_dict["anonymous"] = anonymous
        if metrics_open is not UNSET:
            field_dict["metricsOpen"] = metrics_open
        if expose_mlat is not UNSET:
            field_dict["exposeMlat"] = expose_mlat

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        open_reads = d.pop("openReads")

        expose_observer = d.pop("exposeObserver", UNSET)

        _anonymous = d.pop("anonymous", UNSET)
        anonymous: ReadRestriction | Unset
        if isinstance(_anonymous, Unset):
            anonymous = UNSET
        else:
            anonymous = ReadRestriction.from_dict(_anonymous)

        metrics_open = d.pop("metricsOpen", UNSET)

        expose_mlat = d.pop("exposeMlat", UNSET)

        security_settings = cls(
            open_reads=open_reads,
            expose_observer=expose_observer,
            anonymous=anonymous,
            metrics_open=metrics_open,
            expose_mlat=expose_mlat,
        )

        security_settings.additional_properties = d
        return security_settings

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
