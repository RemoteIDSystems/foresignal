from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.system_rasters_profile import SystemRastersProfile
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.system_rasters_caches import SystemRastersCaches
    from ..models.system_rasters_glm import SystemRastersGlm
    from ..models.system_rasters_render_queue import SystemRastersRenderQueue


T = TypeVar("T", bound="SystemRasters")


@_attrs_define
class SystemRasters:
    """Raster subsystem block of `/v1/system` (042, extended by 043, 045, 048)

    Attributes:
        profile (SystemRastersProfile):
        products (int | Unset):
        frames (int | Unset):
        store_bytes (int | Unset):
        media_bytes (int | Unset): Media store bytes (043); count against the disk quota
        disk_quota_gb (float | Unset):
        disk_quota_pct (float | Unset): Frame store, media store and GLM archive against the quota; at ≥ 90 %
            `/v1/system` degrades
        caches (SystemRastersCaches | Unset): Per-cache metrics (frame, tile, index)
        render_queue (SystemRastersRenderQueue | Unset): Render queue gauges (045)
        render_busy_last_hour (int | Unset): RENDER_BUSY responses in the last hour
        mb_downloaded_today (float | Unset):
        glm (SystemRastersGlm | Unset): GLM flash ring and archive (048): flashes in the places-AOI ring (last 60
            minutes), archive bytes on disk (counted toward the disk quota) and the coverage end of the newest processed GLM
            file (null before the first one since startup)
    """

    profile: SystemRastersProfile
    products: int | Unset = UNSET
    frames: int | Unset = UNSET
    store_bytes: int | Unset = UNSET
    media_bytes: int | Unset = UNSET
    disk_quota_gb: float | Unset = UNSET
    disk_quota_pct: float | Unset = UNSET
    caches: SystemRastersCaches | Unset = UNSET
    render_queue: SystemRastersRenderQueue | Unset = UNSET
    render_busy_last_hour: int | Unset = UNSET
    mb_downloaded_today: float | Unset = UNSET
    glm: SystemRastersGlm | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        profile = self.profile.value

        products = self.products

        frames = self.frames

        store_bytes = self.store_bytes

        media_bytes = self.media_bytes

        disk_quota_gb = self.disk_quota_gb

        disk_quota_pct = self.disk_quota_pct

        caches: dict[str, Any] | Unset = UNSET
        if not isinstance(self.caches, Unset):
            caches = self.caches.to_dict()

        render_queue: dict[str, Any] | Unset = UNSET
        if not isinstance(self.render_queue, Unset):
            render_queue = self.render_queue.to_dict()

        render_busy_last_hour = self.render_busy_last_hour

        mb_downloaded_today = self.mb_downloaded_today

        glm: dict[str, Any] | Unset = UNSET
        if not isinstance(self.glm, Unset):
            glm = self.glm.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "profile": profile,
            }
        )
        if products is not UNSET:
            field_dict["products"] = products
        if frames is not UNSET:
            field_dict["frames"] = frames
        if store_bytes is not UNSET:
            field_dict["storeBytes"] = store_bytes
        if media_bytes is not UNSET:
            field_dict["mediaBytes"] = media_bytes
        if disk_quota_gb is not UNSET:
            field_dict["diskQuotaGb"] = disk_quota_gb
        if disk_quota_pct is not UNSET:
            field_dict["diskQuotaPct"] = disk_quota_pct
        if caches is not UNSET:
            field_dict["caches"] = caches
        if render_queue is not UNSET:
            field_dict["renderQueue"] = render_queue
        if render_busy_last_hour is not UNSET:
            field_dict["renderBusyLastHour"] = render_busy_last_hour
        if mb_downloaded_today is not UNSET:
            field_dict["mbDownloadedToday"] = mb_downloaded_today
        if glm is not UNSET:
            field_dict["glm"] = glm

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.system_rasters_caches import SystemRastersCaches
        from ..models.system_rasters_glm import SystemRastersGlm
        from ..models.system_rasters_render_queue import SystemRastersRenderQueue

        d = dict(src_dict)
        profile = SystemRastersProfile(d.pop("profile"))

        products = d.pop("products", UNSET)

        frames = d.pop("frames", UNSET)

        store_bytes = d.pop("storeBytes", UNSET)

        media_bytes = d.pop("mediaBytes", UNSET)

        disk_quota_gb = d.pop("diskQuotaGb", UNSET)

        disk_quota_pct = d.pop("diskQuotaPct", UNSET)

        _caches = d.pop("caches", UNSET)
        caches: SystemRastersCaches | Unset
        if isinstance(_caches, Unset):
            caches = UNSET
        else:
            caches = SystemRastersCaches.from_dict(_caches)

        _render_queue = d.pop("renderQueue", UNSET)
        render_queue: SystemRastersRenderQueue | Unset
        if isinstance(_render_queue, Unset):
            render_queue = UNSET
        else:
            render_queue = SystemRastersRenderQueue.from_dict(_render_queue)

        render_busy_last_hour = d.pop("renderBusyLastHour", UNSET)

        mb_downloaded_today = d.pop("mbDownloadedToday", UNSET)

        _glm = d.pop("glm", UNSET)
        glm: SystemRastersGlm | Unset
        if isinstance(_glm, Unset):
            glm = UNSET
        else:
            glm = SystemRastersGlm.from_dict(_glm)

        system_rasters = cls(
            profile=profile,
            products=products,
            frames=frames,
            store_bytes=store_bytes,
            media_bytes=media_bytes,
            disk_quota_gb=disk_quota_gb,
            disk_quota_pct=disk_quota_pct,
            caches=caches,
            render_queue=render_queue,
            render_busy_last_hour=render_busy_last_hour,
            mb_downloaded_today=mb_downloaded_today,
            glm=glm,
        )

        system_rasters.additional_properties = d
        return system_rasters

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
