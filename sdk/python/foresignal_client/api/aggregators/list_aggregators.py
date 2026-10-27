from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/aggregators",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """List first-party aggregators

     Lists the first-party aggregators across RF/sensor domains with status and the public sources each
    supersedes (C3). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** every domain is listed. adsb is implemented, ais since R2-020, datalink since R2-021,
    satellite since R2-022 and ground-sensors since R2-023, so every domain reads `implemented: true`; a
    domain whose backend a server does not have reads `implemented: false` and `status: unconfigured`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """List first-party aggregators

     Lists the first-party aggregators across RF/sensor domains with status and the public sources each
    supersedes (C3). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** every domain is listed. adsb is implemented, ais since R2-020, datalink since R2-021,
    satellite since R2-022 and ground-sensors since R2-023, so every domain reads `implemented: true`; a
    domain whose backend a server does not have reads `implemented: false` and `status: unconfigured`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """List first-party aggregators

     Lists the first-party aggregators across RF/sensor domains with status and the public sources each
    supersedes (C3). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** every domain is listed. adsb is implemented, ais since R2-020, datalink since R2-021,
    satellite since R2-022 and ground-sensors since R2-023, so every domain reads `implemented: true`; a
    domain whose backend a server does not have reads `implemented: false` and `status: unconfigured`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """List first-party aggregators

     Lists the first-party aggregators across RF/sensor domains with status and the public sources each
    supersedes (C3). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** every domain is listed. adsb is implemented, ais since R2-020, datalink since R2-021,
    satellite since R2-022 and ground-sensors since R2-023, so every domain reads `implemented: true`; a
    domain whose backend a server does not have reads `implemented: false` and `status: unconfigured`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
