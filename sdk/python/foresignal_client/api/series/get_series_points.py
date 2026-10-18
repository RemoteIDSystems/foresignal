import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_series_points_format import GetSeriesPointsFormat
from ...models.problem import Problem
from ...models.series_point_page import SeriesPointPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    series_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 1000,
    format_: GetSeriesPointsFormat | Unset = GetSeriesPointsFormat.JSON,
    cursor: str | Unset = UNSET,
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

    params["limit"] = limit

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params["cursor"] = cursor

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/series/{source_id}/{series_id}".format(
            source_id=quote(str(source_id), safe=""),
            series_id=quote(str(series_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SeriesPointPage | None:
    if response.status_code == 200:
        response_200 = SeriesPointPage.from_dict(response.json())

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
) -> Response[Problem | SeriesPointPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    series_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 1000,
    format_: GetSeriesPointsFormat | Unset = GetSeriesPointsFormat.JSON,
    cursor: str | Unset = UNSET,
) -> Response[Problem | SeriesPointPage]:
    """Get points of one series

     `series-point` records of one series ordered by `periodStart` (served from the partial expression
    index on `(source_id, payload->>'series', event_time)`), as JSON or CSV. Revisions are record
    versions; the current value per period is returned. 404 `SERIES_NOT_FOUND`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        series_id (str):  Example: agsi:eu.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        limit (int | Unset):  Default: 1000.
        format_ (GetSeriesPointsFormat | Unset):  Default: GetSeriesPointsFormat.JSON.
        cursor (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesPointPage]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        series_id=series_id,
        from_=from_,
        to=to,
        limit=limit,
        format_=format_,
        cursor=cursor,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    series_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 1000,
    format_: GetSeriesPointsFormat | Unset = GetSeriesPointsFormat.JSON,
    cursor: str | Unset = UNSET,
) -> Problem | SeriesPointPage | None:
    """Get points of one series

     `series-point` records of one series ordered by `periodStart` (served from the partial expression
    index on `(source_id, payload->>'series', event_time)`), as JSON or CSV. Revisions are record
    versions; the current value per period is returned. 404 `SERIES_NOT_FOUND`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        series_id (str):  Example: agsi:eu.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        limit (int | Unset):  Default: 1000.
        format_ (GetSeriesPointsFormat | Unset):  Default: GetSeriesPointsFormat.JSON.
        cursor (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesPointPage
    """

    return sync_detailed(
        source_id=source_id,
        series_id=series_id,
        client=client,
        from_=from_,
        to=to,
        limit=limit,
        format_=format_,
        cursor=cursor,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    series_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 1000,
    format_: GetSeriesPointsFormat | Unset = GetSeriesPointsFormat.JSON,
    cursor: str | Unset = UNSET,
) -> Response[Problem | SeriesPointPage]:
    """Get points of one series

     `series-point` records of one series ordered by `periodStart` (served from the partial expression
    index on `(source_id, payload->>'series', event_time)`), as JSON or CSV. Revisions are record
    versions; the current value per period is returned. 404 `SERIES_NOT_FOUND`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        series_id (str):  Example: agsi:eu.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        limit (int | Unset):  Default: 1000.
        format_ (GetSeriesPointsFormat | Unset):  Default: GetSeriesPointsFormat.JSON.
        cursor (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SeriesPointPage]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        series_id=series_id,
        from_=from_,
        to=to,
        limit=limit,
        format_=format_,
        cursor=cursor,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    series_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 1000,
    format_: GetSeriesPointsFormat | Unset = GetSeriesPointsFormat.JSON,
    cursor: str | Unset = UNSET,
) -> Problem | SeriesPointPage | None:
    """Get points of one series

     `series-point` records of one series ordered by `periodStart` (served from the partial expression
    index on `(source_id, payload->>'series', event_time)`), as JSON or CSV. Revisions are record
    versions; the current value per period is returned. 404 `SERIES_NOT_FOUND`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        series_id (str):  Example: agsi:eu.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        limit (int | Unset):  Default: 1000.
        format_ (GetSeriesPointsFormat | Unset):  Default: GetSeriesPointsFormat.JSON.
        cursor (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SeriesPointPage
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            series_id=series_id,
            client=client,
            from_=from_,
            to=to,
            limit=limit,
            format_=format_,
            cursor=cursor,
        )
    ).parsed
