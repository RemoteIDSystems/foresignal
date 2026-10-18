import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.raster_frame_page import RasterFramePage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params["run"] = run

    params["includePartial"] = include_partial

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/frames".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RasterFramePage | None:
    if response.status_code == 200:
        response_200 = RasterFramePage.from_dict(response.json())

        return response_200

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RasterFramePage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | RasterFramePage]:
    """List frames of a product

     Cursor page of frames, newest first, each with `time`, `timeKey`, `frameKey`, `expiresAt`, `status`,
    `run`/`step` for forecasts and fully expanded `tiles` and `image` templates, plus a prefetch hint,
    `serverTime` and `earliest`. Partial frames are excluded unless `includePartial=true`. Clients never
    build time strings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):
        include_partial (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RasterFramePage]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        from_=from_,
        to=to,
        run=run,
        include_partial=include_partial,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | RasterFramePage | None:
    """List frames of a product

     Cursor page of frames, newest first, each with `time`, `timeKey`, `frameKey`, `expiresAt`, `status`,
    `run`/`step` for forecasts and fully expanded `tiles` and `image` templates, plus a prefetch hint,
    `serverTime` and `earliest`. Partial frames are excluded unless `includePartial=true`. Clients never
    build time strings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):
        include_partial (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RasterFramePage
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        from_=from_,
        to=to,
        run=run,
        include_partial=include_partial,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | RasterFramePage]:
    """List frames of a product

     Cursor page of frames, newest first, each with `time`, `timeKey`, `frameKey`, `expiresAt`, `status`,
    `run`/`step` for forecasts and fully expanded `tiles` and `image` templates, plus a prefetch hint,
    `serverTime` and `earliest`. Partial frames are excluded unless `includePartial=true`. Clients never
    build time strings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):
        include_partial (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RasterFramePage]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        from_=from_,
        to=to,
        run=run,
        include_partial=include_partial,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | RasterFramePage | None:
    """List frames of a product

     Cursor page of frames, newest first, each with `time`, `timeKey`, `frameKey`, `expiresAt`, `status`,
    `run`/`step` for forecasts and fully expanded `tiles` and `image` templates, plus a prefetch hint,
    `serverTime` and `earliest`. Partial frames are excluded unless `includePartial=true`. Clients never
    build time strings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):
        include_partial (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RasterFramePage
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            from_=from_,
            to=to,
            run=run,
            include_partial=include_partial,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
