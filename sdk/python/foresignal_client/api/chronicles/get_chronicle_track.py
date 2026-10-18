import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.track import Track
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    source_id: str,
    external_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    max_points: int | Unset = 2000,
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

    params["maxPoints"] = max_points

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/tracks/{source_id}/{external_id}".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
            source_id=quote(str(source_id), safe=""),
            external_id=quote(str(external_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Track | None:
    if response.status_code == 200:
        response_200 = Track.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Problem | Track]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    max_points: int | Unset = 2000,
) -> Response[Problem | Track]:
    """Track from chronicle storage

     An entity's track from captured versions only (037 track shape).

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        max_points (int | Unset):  Default: 2000.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Track]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        source_id=source_id,
        external_id=external_id,
        from_=from_,
        to=to,
        max_points=max_points,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    max_points: int | Unset = 2000,
) -> Problem | Track | None:
    """Track from chronicle storage

     An entity's track from captured versions only (037 track shape).

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        max_points (int | Unset):  Default: 2000.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Track
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        source_id=source_id,
        external_id=external_id,
        client=client,
        from_=from_,
        to=to,
        max_points=max_points,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    max_points: int | Unset = 2000,
) -> Response[Problem | Track]:
    """Track from chronicle storage

     An entity's track from captured versions only (037 track shape).

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        max_points (int | Unset):  Default: 2000.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Track]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        source_id=source_id,
        external_id=external_id,
        from_=from_,
        to=to,
        max_points=max_points,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    max_points: int | Unset = 2000,
) -> Problem | Track | None:
    """Track from chronicle storage

     An entity's track from captured versions only (037 track shape).

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        max_points (int | Unset):  Default: 2000.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Track
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            source_id=source_id,
            external_id=external_id,
            client=client,
            from_=from_,
            to=to,
            max_points=max_points,
        )
    ).parsed
