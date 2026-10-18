from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_binding import ExternalBinding
from ...models.external_binding_input import ExternalBindingInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    binding_id: str,
    *,
    body: ExternalBindingInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/external/bindings/{binding_id}".format(
            binding_id=quote(str(binding_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExternalBinding | Problem | None:
    if response.status_code == 200:
        response_200 = ExternalBinding.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExternalBinding | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    binding_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> Response[ExternalBinding | Problem]:
    """Replace a binding

     Replaces the stream, target, config, filter or enabled state. A changed or re-enabled binding
    backfills unless `backfill` is false; re-enabling clears `lastError` (the way back after a breaking
    manifest update disabled it).

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBinding | Problem]
    """

    kwargs = _get_kwargs(
        binding_id=binding_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    binding_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> ExternalBinding | Problem | None:
    """Replace a binding

     Replaces the stream, target, config, filter or enabled state. A changed or re-enabled binding
    backfills unless `backfill` is false; re-enabling clears `lastError` (the way back after a breaking
    manifest update disabled it).

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBinding | Problem
    """

    return sync_detailed(
        binding_id=binding_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    binding_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> Response[ExternalBinding | Problem]:
    """Replace a binding

     Replaces the stream, target, config, filter or enabled state. A changed or re-enabled binding
    backfills unless `backfill` is false; re-enabling clears `lastError` (the way back after a breaking
    manifest update disabled it).

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBinding | Problem]
    """

    kwargs = _get_kwargs(
        binding_id=binding_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    binding_id: str,
    *,
    client: AuthenticatedClient,
    body: ExternalBindingInput,
) -> ExternalBinding | Problem | None:
    """Replace a binding

     Replaces the stream, target, config, filter or enabled state. A changed or re-enabled binding
    backfills unless `backfill` is false; re-enabling clears `lastError` (the way back after a breaking
    manifest update disabled it).

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ExternalBindingInput): Bind an atomic stream to a target (138)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBinding | Problem
    """

    return (
        await asyncio_detailed(
            binding_id=binding_id,
            client=client,
            body=body,
        )
    ).parsed
