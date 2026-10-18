from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_media_thumbnail_w import GetMediaThumbnailW
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    media_id: str,
    *,
    w: GetMediaThumbnailW,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_w = w.value
    params["w"] = json_w

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/media/{media_id}/thumbnail".format(
            media_id=quote(str(media_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    media_id: str,
    *,
    client: AuthenticatedClient,
    w: GetMediaThumbnailW,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download a media thumbnail

     Serves the 256 or 1024 px long-edge rendition (JPEG q0.85, or PNG when the source has alpha); a
    missing variant is generated once (single-flight) and stored. Images smaller than the target are
    served unscaled. Other widths → 400 VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        media_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        w (GetMediaThumbnailW):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        media_id=media_id,
        w=w,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    media_id: str,
    *,
    client: AuthenticatedClient,
    w: GetMediaThumbnailW,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download a media thumbnail

     Serves the 256 or 1024 px long-edge rendition (JPEG q0.85, or PNG when the source has alpha); a
    missing variant is generated once (single-flight) and stored. Images smaller than the target are
    served unscaled. Other widths → 400 VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        media_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        w (GetMediaThumbnailW):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        media_id=media_id,
        client=client,
        w=w,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    media_id: str,
    *,
    client: AuthenticatedClient,
    w: GetMediaThumbnailW,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download a media thumbnail

     Serves the 256 or 1024 px long-edge rendition (JPEG q0.85, or PNG when the source has alpha); a
    missing variant is generated once (single-flight) and stored. Images smaller than the target are
    served unscaled. Other widths → 400 VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        media_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        w (GetMediaThumbnailW):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        media_id=media_id,
        w=w,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    media_id: str,
    *,
    client: AuthenticatedClient,
    w: GetMediaThumbnailW,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download a media thumbnail

     Serves the 256 or 1024 px long-edge rendition (JPEG q0.85, or PNG when the source has alpha); a
    missing variant is generated once (single-flight) and stored. Images smaller than the target are
    served unscaled. Other widths → 400 VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        media_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        w (GetMediaThumbnailW):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            media_id=media_id,
            client=client,
            w=w,
            if_none_match=if_none_match,
        )
    ).parsed
