from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.series_page import SeriesPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    params["q"] = q

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/series",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SeriesPage | None:
    if response.status_code == 200:
        response_200 = SeriesPage.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SeriesPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SeriesPage]:
    """List time series

     Series catalog built from `series-latest` records (093 energy sources and later series sources): one
    entry per series with its newest non-forecast point. Each entry carries the series metadata of 117
    (family, transform, releaseLagDays, egress, regionCode, topicId, attribution). LOCAL_ONLY sources'
    series are served only to callers allowed local egress (EgressPolicy on API_READ).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesPage]
    """

    kwargs = _get_kwargs(
        source=source,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SeriesPage | None:
    """List time series

     Series catalog built from `series-latest` records (093 energy sources and later series sources): one
    entry per series with its newest non-forecast point. Each entry carries the series metadata of 117
    (family, transform, releaseLagDays, egress, regionCode, topicId, attribution). LOCAL_ONLY sources'
    series are served only to callers allowed local egress (EgressPolicy on API_READ).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesPage
    """

    return sync_detailed(
        client=client,
        source=source,
        q=q,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SeriesPage]:
    """List time series

     Series catalog built from `series-latest` records (093 energy sources and later series sources): one
    entry per series with its newest non-forecast point. Each entry carries the series metadata of 117
    (family, transform, releaseLagDays, egress, regionCode, topicId, attribution). LOCAL_ONLY sources'
    series are served only to callers allowed local egress (EgressPolicy on API_READ).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesPage]
    """

    kwargs = _get_kwargs(
        source=source,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SeriesPage | None:
    """List time series

     Series catalog built from `series-latest` records (093 energy sources and later series sources): one
    entry per series with its newest non-forecast point. Each entry carries the series metadata of 117
    (family, transform, releaseLagDays, egress, regionCode, topicId, attribution). LOCAL_ONLY sources'
    series are served only to callers allowed local egress (EgressPolicy on API_READ).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesPage
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            q=q,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
