from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    base_rate_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/base-rates/{base_rate_id}".format(
            base_rate_id=quote(str(base_rate_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    base_rate_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Delete a custom base rate

     Deletes a custom base rate; automatic rates cannot be deleted (409 `BASE_RATE_NOT_CUSTOM`).

    **Scope:** `agent` or `admin`.

    Args:
        base_rate_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        base_rate_id=base_rate_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    base_rate_id: str,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Delete a custom base rate

     Deletes a custom base rate; automatic rates cannot be deleted (409 `BASE_RATE_NOT_CUSTOM`).

    **Scope:** `agent` or `admin`.

    Args:
        base_rate_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        base_rate_id=base_rate_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    base_rate_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Delete a custom base rate

     Deletes a custom base rate; automatic rates cannot be deleted (409 `BASE_RATE_NOT_CUSTOM`).

    **Scope:** `agent` or `admin`.

    Args:
        base_rate_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        base_rate_id=base_rate_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    base_rate_id: str,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Delete a custom base rate

     Deletes a custom base rate; automatic rates cannot be deleted (409 `BASE_RATE_NOT_CUSTOM`).

    **Scope:** `agent` or `admin`.

    Args:
        base_rate_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            base_rate_id=base_rate_id,
            client=client,
        )
    ).parsed
