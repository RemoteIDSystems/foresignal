from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.alert import Alert
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    alert_id: int,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/alerts/{alert_id}".format(
            alert_id=quote(str(alert_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Alert | Problem | None:
    if response.status_code == 200:
        response_200 = Alert.from_dict(response.json())

        return response_200

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
) -> Response[Alert | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    alert_id: int,
    *,
    client: AuthenticatedClient,
) -> Response[Alert | Problem]:
    """Get an alert

     Alert with its lifecycle; `record` is loaded by seq and falls back to `recordSnapshot` once the
    version is gone. An alert of a private source is 404 for anonymous callers.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        alert_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | Problem]
    """

    kwargs = _get_kwargs(
        alert_id=alert_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    alert_id: int,
    *,
    client: AuthenticatedClient,
) -> Alert | Problem | None:
    """Get an alert

     Alert with its lifecycle; `record` is loaded by seq and falls back to `recordSnapshot` once the
    version is gone. An alert of a private source is 404 for anonymous callers.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        alert_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | Problem
    """

    return sync_detailed(
        alert_id=alert_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    alert_id: int,
    *,
    client: AuthenticatedClient,
) -> Response[Alert | Problem]:
    """Get an alert

     Alert with its lifecycle; `record` is loaded by seq and falls back to `recordSnapshot` once the
    version is gone. An alert of a private source is 404 for anonymous callers.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        alert_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | Problem]
    """

    kwargs = _get_kwargs(
        alert_id=alert_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    alert_id: int,
    *,
    client: AuthenticatedClient,
) -> Alert | Problem | None:
    """Get an alert

     Alert with its lifecycle; `record` is loaded by seq and falls back to `recordSnapshot` once the
    version is gone. An alert of a private source is 404 for anonymous callers.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        alert_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | Problem
    """

    return (
        await asyncio_detailed(
            alert_id=alert_id,
            client=client,
        )
    ).parsed
