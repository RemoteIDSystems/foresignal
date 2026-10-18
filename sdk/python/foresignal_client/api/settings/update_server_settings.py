from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.server_settings import ServerSettings
from ...types import Response


def _get_kwargs(
    *,
    body: ServerSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/server",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ServerSettings | None:
    if response.status_code == 200:
        response_200 = ServerSettings.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ServerSettings]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ServerSettings,
) -> Response[Problem | ServerSettings]:
    """Set server settings

     Sets `publicBaseUrl` (required; null means the request's own address), `timezone`, `clientLinks`
    (each template `foresignal://` or `https://`) and `push` (031; refused with 400 until then). The
    response carries `warnings` (PUBLIC_BASE_URL_INSECURE when `publicBaseUrl` is http:// and its host
    is not loopback, private, CGNAT or `.local`; PUBLIC_BASE_URL_MISMATCH when requests arrive over
    https while it is http://). `push.allowedPrivateHosts` entries are exact hostnames or CIDRs (IPv4
    prefix ≥ /8, IPv6 ≥ /32).

    **Scope:** `admin`.

    Args:
        body (ServerSettings):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ServerSettings]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ServerSettings,
) -> Problem | ServerSettings | None:
    """Set server settings

     Sets `publicBaseUrl` (required; null means the request's own address), `timezone`, `clientLinks`
    (each template `foresignal://` or `https://`) and `push` (031; refused with 400 until then). The
    response carries `warnings` (PUBLIC_BASE_URL_INSECURE when `publicBaseUrl` is http:// and its host
    is not loopback, private, CGNAT or `.local`; PUBLIC_BASE_URL_MISMATCH when requests arrive over
    https while it is http://). `push.allowedPrivateHosts` entries are exact hostnames or CIDRs (IPv4
    prefix ≥ /8, IPv6 ≥ /32).

    **Scope:** `admin`.

    Args:
        body (ServerSettings):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ServerSettings
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ServerSettings,
) -> Response[Problem | ServerSettings]:
    """Set server settings

     Sets `publicBaseUrl` (required; null means the request's own address), `timezone`, `clientLinks`
    (each template `foresignal://` or `https://`) and `push` (031; refused with 400 until then). The
    response carries `warnings` (PUBLIC_BASE_URL_INSECURE when `publicBaseUrl` is http:// and its host
    is not loopback, private, CGNAT or `.local`; PUBLIC_BASE_URL_MISMATCH when requests arrive over
    https while it is http://). `push.allowedPrivateHosts` entries are exact hostnames or CIDRs (IPv4
    prefix ≥ /8, IPv6 ≥ /32).

    **Scope:** `admin`.

    Args:
        body (ServerSettings):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ServerSettings]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ServerSettings,
) -> Problem | ServerSettings | None:
    """Set server settings

     Sets `publicBaseUrl` (required; null means the request's own address), `timezone`, `clientLinks`
    (each template `foresignal://` or `https://`) and `push` (031; refused with 400 until then). The
    response carries `warnings` (PUBLIC_BASE_URL_INSECURE when `publicBaseUrl` is http:// and its host
    is not loopback, private, CGNAT or `.local`; PUBLIC_BASE_URL_MISMATCH when requests arrive over
    https while it is http://). `push.allowedPrivateHosts` entries are exact hostnames or CIDRs (IPv4
    prefix ≥ /8, IPv6 ≥ /32).

    **Scope:** `admin`.

    Args:
        body (ServerSettings):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ServerSettings
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
